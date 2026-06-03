package com.uet.server.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.uet.domain.summary.AuctionSummary;
import com.uet.domain.summary.BidHistoryPoint;
import com.uet.domain.summary.UserSummary;
import com.uet.domain.entity.auction.Auction;
import com.uet.domain.entity.auction.BidTransaction;
import com.uet.domain.entity.item.Art;
import com.uet.domain.entity.item.Electronics;
import com.uet.domain.entity.item.Item;
import com.uet.domain.entity.item.Vehicle;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.entity.user.Seller;
import com.uet.domain.enums.AuctionStatus;
import com.uet.domain.event.ServerEvent;
import com.uet.domain.event.ServerEventType;
import com.uet.domain.exceptions.InsufficientBalanceException;
import com.uet.domain.exceptions.InvalidBidException;
import com.uet.domain.exceptions.InvalidTransactionException;
import com.uet.domain.factory.ArtFactory;
import com.uet.domain.factory.ElectronicsFactory;
import com.uet.domain.factory.ItemFactory;
import com.uet.domain.factory.VehicleFactory;
import com.uet.domain.request.ProductPostRequest;
import com.uet.domain.result.UserActionResult;
import com.uet.server.core.ClientHandler;
import com.uet.server.repositories.AuctionRepository;
import com.uet.server.repositories.UserRepository;

public class AuctionManager {
    private static AuctionManager instance;
    private List<Auction> auctions = new ArrayList<>();
    private final Map<String, ClientHandler> onlineClients = new HashMap<>();
    private final Map<String, Map<String, AutoBidRegistration>> autoBidSettings = new HashMap<>();
    private final RealtimeAuctionNotifier realtimeNotifier = new RealtimeAuctionNotifier(this);
    private ScheduledExecutorService statusScheduler;
    private static final Logger LOGGER = Logger.getLogger(AuctionManager.class.getName());
    private static final long LATE_BID_EXTENSION_WINDOW_SECONDS = 5 * 60;
    private static final long LATE_BID_EXTENSION_SECONDS = 5 * 60;
    private AuctionManager() {}
    

    //Double-Checked Locking
    // Một thằng manager duy nhất xuyên suốt
    public static AuctionManager getInstance() {
        if (instance == null){
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    // Map onlineClients vừa chống đăng nhập trùng, vừa giữ socket để gửi event realtime.
    public synchronized boolean signIn(String systemId, ClientHandler client) {
        if (onlineClients.containsKey(systemId)) {
            LOGGER.warning("Rejected duplicate sign in for user=" + systemId);
            return false;
        }
        onlineClients.put(systemId, client);
        broadcastOnlineUsers();
        LOGGER.info("User signed in: " + systemId + ". Online users=" + getOnlineUsers());
        return true;
    }
    
    //SignOut Disconnect
    public synchronized void removeUser(String systemId) {
        if (systemId == null) {
            return;
        }
        ClientHandler removedClient = onlineClients.remove(systemId);
        removeAutoBidSettings(systemId);
        if (removedClient != null) {
            broadcastOnlineUsers();
            LOGGER.info("User signed out: " + systemId + ". Online users=" + getOnlineUsers());
        }
    }

    public synchronized void removeClient(ClientHandler client){
        String removedSystemId = null;
        for (Map.Entry<String, ClientHandler> entry : onlineClients.entrySet()) {
            if (entry.getValue() == client) {
                removedSystemId = entry.getKey();
                break;
            }
        }
        if (removedSystemId != null) {
            onlineClients.remove(removedSystemId);
            removeAutoBidSettings(removedSystemId);
            broadcastOnlineUsers();
            LOGGER.info("Client handler removed for user=" + removedSystemId
                    + ". Online users=" + getOnlineUsers());
        }
    }

    public synchronized int getOnlineUsers(){
        return onlineClients.size();
    }

    public synchronized List<UserSummary> getUserSummaries() {
        return Collections.unmodifiableList(UserRepository.findAllUserSummaries());
    }

    public synchronized UserActionResult banUser(String targetSystemId, String adminSystemId) {
        if (targetSystemId == null || targetSystemId.isBlank()) {
            return UserActionResult.failed("Please select a user first.");
        }
        if (targetSystemId.equals(adminSystemId)) {
            return UserActionResult.failed("Admin cannot ban the current admin account.");
        }

        UserSummary targetUser = UserRepository.findUserSummaryBySystemId(targetSystemId);
        if (targetUser == null) {
            LOGGER.warning("Ban user failed. User not found: " + targetSystemId);
            return UserActionResult.failed("User not found.");
        }
        if ("Admin".equals(targetUser.getRole())) {
            LOGGER.warning("Ban user failed. Target is admin: " + targetSystemId);
            return UserActionResult.failed("Cannot ban an admin account.");
        }
        if (!targetUser.isActive()) {
            return UserActionResult.failed("This account is already banned.");
        }

        if (!UserRepository.banUser(targetSystemId)) {
            LOGGER.warning("Ban user failed while updating database: " + targetSystemId);
            return UserActionResult.failed("Cannot ban this user.");
        }

        ClientHandler targetClient = onlineClients.remove(targetSystemId);
        removeAutoBidSettings(targetSystemId);
        if (targetClient != null) {
            broadcastOnlineUsers();
            targetClient.disconnectBecauseBanned("Your account has been banned by admin.");
        }

        int affectedAuctions = handleBannedUserAuctionParticipation(targetUser);
        LOGGER.warning("User banned: target=" + targetSystemId
                + ", admin=" + adminSystemId
                + ", affectedAuctions=" + affectedAuctions);
        return UserActionResult.success("User banned successfully. Affected auctions: " + affectedAuctions + ".");
    }

    // Chọn hướng xử lý auction theo role của user bị ban: bidder thì gỡ bid, seller thì hủy phiên của họ.
    private int handleBannedUserAuctionParticipation(UserSummary targetUser) {
        if ("Bidder".equals(targetUser.getRole())) {
            return cancelBannedBidderParticipation(targetUser.getSystemId());
        }
        if ("Seller".equals(targetUser.getRole())) {
            return cancelBannedSellerAuctions(targetUser.getSystemId());
        }
        return 0;
    }

    // Gỡ bidder bị ban khỏi mọi auction trong RAM, cập nhật tiền/bid xuống DB và bắn realtime event cho client.
    private int cancelBannedBidderParticipation(String bidderId) {
        int affectedAuctions = 0;
        Map<String, Bidder> affectedBidders = new HashMap<>();

        for (Auction auction : auctions) {
            try {
                Bidder previousWinner = auction.getWinner();
                boolean targetWasWinner = previousWinner != null && previousWinner.getId().equals(bidderId);
                boolean changed = auction.cancelBidderParticipation(bidderId);
                if (!changed) {
                    continue;
                }
                affectedAuctions++;
                if (targetWasWinner) {
                    affectedBidders.put(previousWinner.getId(), previousWinner);
                }
                if (auction.getWinner() != null) {
                    affectedBidders.put(auction.getWinner().getId(), auction.getWinner());
                }
                AuctionRepository.updateAuction(auction);
                for (BidTransaction bid : auction.getHistoryBids()) {
                    AuctionRepository.updateBid(bid);
                }
            } catch (InvalidTransactionException | InsufficientBalanceException e) {
                LOGGER.warning("Cancel banned bidder participation failed: bidder=" + bidderId
                        + ", auction=" + auction.getId()
                        + ", reason=" + e.getMessage());
            }
        }

        AuctionRepository.cancelUserOpenBids(bidderId);
        affectedBidders.values().forEach(UserRepository::updateBidderFunds);
        if (affectedAuctions > 0) {
            broadcast(new ServerEvent(ServerEventType.AUCTION_UPDATED, "Banned bidder removed from active auctions."));
        }
        return affectedAuctions;
    }

    // Hủy các auction thuộc seller bị ban, hoàn tiền winner nếu có và đồng bộ DB.
    private int cancelBannedSellerAuctions(String sellerId) {
        int affectedAuctions = 0;

        for (Auction auction : auctions) {
            if (!auction.getSeller().getId().equals(sellerId)) {
                continue;
            }
            Bidder previousWinner = auction.getWinner();
            try {
                if (!auction.cancelBecauseSellerBanned()) {
                    continue;
                }
                affectedAuctions++;
                if (previousWinner != null) {
                    UserRepository.updateBidderFunds(previousWinner);
                }
                AuctionRepository.cancelAuctionBids(auction.getId());
                AuctionRepository.updateAuction(auction);
            } catch (InvalidTransactionException | InsufficientBalanceException e) {
                LOGGER.warning("Cancel banned seller auction failed: seller=" + sellerId
                        + ", auction=" + auction.getId()
                        + ", reason=" + e.getMessage());
            }
        }

        if (affectedAuctions > 0) {
            broadcast(new ServerEvent(ServerEventType.AUCTION_UPDATED, "Banned seller auctions canceled."));
        }
        return affectedAuctions;
    }

    public void broadcastOnlineUsers() {
        broadcast(new ServerEvent(ServerEventType.ONLINE_USERS_UPDATED, getOnlineUsers()));
    }

    //Gửi cập nhật cho tất cả các clients hiện đang dùng ứng dụng
    public void broadcast(ServerEvent event){
        List<ClientHandler> curClientHandlers;
        synchronized (this) {
            curClientHandlers = new ArrayList<>(onlineClients.values());
        }
        //đảy event đi thông báo cho các client
        curClientHandlers.forEach(clientHandler -> clientHandler.sendEvent(event));
    }

    public synchronized Auction createAuction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime, double minIncrement){
        Auction auction = new Auction(item, seller, startTime, endTime, minIncrement);
        auction.addObserver(realtimeNotifier);
        auctions.add(auction);
        return auction;
    }

    public synchronized void loadAuctionsFromDatabase() {
        auctions.clear();
        UserRepository.rebuildBidderLockedBalancesFromAuctions();
        auctions.addAll(AuctionRepository.loadAuctions());
        auctions.forEach(auction -> auction.addObserver(realtimeNotifier));
        closeExpiredAuctions();
        LOGGER.info("Loaded " + auctions.size() + " auctions from database.");
    }

    public synchronized Auction postProduct(ProductPostRequest request, Seller seller) {
        Item item = createItem(request);
        Auction auction = createAuction(
                item,
                seller,
                request.getStartTime(),
                request.getEndTime(),
                request.getMinIncrement());
        auction.setStatus(AuctionStatus.PENDING_APPROVAL);
        AuctionRepository.saveAuction(auction, request.getImageLink());
        LOGGER.info("Product posted: auction=" + auction.getId()
                + ", seller=" + seller.getId()
                + ", item=" + item.getName()
                + ", status=" + auction.getStatus());
        return auction;
    }

    private Item createItem(ProductPostRequest request) {
        String type = request.getProductType();
        String name = request.getProductName();
        double openingPrice = request.getOpeningPrice();

         ItemFactory factory;
        if ("Art".equalsIgnoreCase(type)) {
            factory = new ArtFactory();
        } else if ("Vehicle".equalsIgnoreCase(type)) {
            factory = new VehicleFactory();
        } else if ("Electronics".equalsIgnoreCase(type)) {
            factory = new ElectronicsFactory();
        } else {
            LOGGER.warning("Rejected product post with unknown type: " + type);
            throw new IllegalArgumentException("Unknown product type: " + type);
        }
        Item item = factory.createItem(name, openingPrice);
        item.setDescription(request.getDescription());
        return item;
    }

    public synchronized void seedDemoAuctions() {
        if (!auctions.isEmpty()) {
            return;
        }

        Seller demoSeller = new Seller("000000000001", "Demo Seller", "0900000001", "demo", "Ha Noi");
        createAuction(new Electronics("Laptop Dell XPS 13", 1_000, "Dell", "XPS 13"),
                demoSeller,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(2),
                50);

        createAuction(new Art("Sunset Painting", 500, "Unknown Artist", 2024, "Oil"),
                demoSeller,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(3),
                25);

        createAuction(new Vehicle("Honda SH", 2_000, "Honda", 2022),
                demoSeller,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(4),
                100);
        LOGGER.info("Seeded demo auctions. Count=" + auctions.size());
    }

    public synchronized Auction getAuctionById(String auctionId) {
        for (Auction auction : auctions) {
            if (auction.getId().equals(auctionId)) {
                return auction;
            }
        }
        return null;
    }

    public synchronized List<BidHistoryPoint> getBidListByAuctionId(String auctionId){
        Auction auction = getAuctionById(auctionId);
        if(auction == null){
            return Collections.emptyList();
        }
        List<BidTransaction> bidList = auction.getHistoryBids();
        List<BidHistoryPoint> bids = new ArrayList<>();
        bidList.forEach(bid -> bids.add(new BidHistoryPoint(bid)));
        return bids;
    }
    
    public synchronized List<BidHistoryPoint> getBidListFromDatabase(String auctionId){
        return AuctionRepository.loadBidHistory(auctionId);
    }

    public synchronized List<Auction> getActiveAuctions() {
        closeExpiredAuctions();
        List<Auction> activeAuctions = new ArrayList<>();
        for (Auction auction : auctions) {
            if (auction.getStatus() == AuctionStatus.OPEN || auction.getStatus() == AuctionStatus.RUNNING) {
                activeAuctions.add(auction);
            }
        }
        return Collections.unmodifiableList(activeAuctions);
    }

    public synchronized List<AuctionSummary> getActiveAuctionSummaries() {
        List<AuctionSummary> summaries = new ArrayList<>();
        for (Auction auction : getActiveAuctions()) {
            summaries.add(new AuctionSummary(auction));
        }
        return Collections.unmodifiableList(summaries);
    }

    public synchronized List<AuctionSummary> getPendingAuctionSummaries() {
        closeExpiredAuctions();
        List<AuctionSummary> summaries = new ArrayList<>();
        for (Auction auction : auctions) {
            if (auction.getStatus() == AuctionStatus.PENDING_APPROVAL) {
                summaries.add(new AuctionSummary(auction));
            }
        }
        return Collections.unmodifiableList(summaries);
    }

    public synchronized List<AuctionSummary> getSellerAuctionSummaries(Seller seller) {
        closeExpiredAuctions();
        List<AuctionSummary> summaries = new ArrayList<>();
        for (Auction auction : auctions) {
            if (auction.getSeller().getId().equals(seller.getId())) {
                summaries.add(new AuctionSummary(auction));
            }
        }
        return Collections.unmodifiableList(summaries);
    }

    public synchronized void approveAuction(String auctionId) throws InvalidBidException {
        Auction auction = getAuctionById(auctionId);
        if (auction == null) {
            LOGGER.warning("Approve auction failed. Auction not found: " + auctionId);
            throw new InvalidBidException("Không tìm thấy phiên đấu giá!");
        }
        if (auction.getStatus() != AuctionStatus.PENDING_APPROVAL) {
            LOGGER.warning("Approve auction failed. Auction=" + auctionId + ", status=" + auction.getStatus());
            throw new InvalidBidException("Phiên này không ở trạng thái chờ duyệt!");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(auction.getEndTime())) {
            auction.setStatus(AuctionStatus.CANCELED);
            AuctionRepository.updateAuction(auction);
            LOGGER.warning("Approve auction failed because auction already ended. Auction=" + auctionId);
            throw new InvalidBidException("Phiên đã quá thời gian kết thúc, không thể duyệt!");
        }

        if (!now.isBefore(auction.getStartTime())) {
            auction.setStatus(AuctionStatus.RUNNING);
        } else {
            auction.setStatus(AuctionStatus.OPEN);
        }
        AuctionRepository.updateAuction(auction);
        LOGGER.info("Auction approved: auction=" + auctionId + ", status=" + auction.getStatus());
    }

    public synchronized void rejectAuction(String auctionId) throws InvalidBidException {
        Auction auction = getAuctionById(auctionId);
        if (auction == null) {
            LOGGER.warning("Reject auction failed. Auction not found: " + auctionId);
            throw new InvalidBidException("Không tìm thấy phiên đấu giá!");
        }
        if (auction.getStatus() != AuctionStatus.PENDING_APPROVAL) {
            LOGGER.warning("Reject auction failed. Auction=" + auctionId + ", status=" + auction.getStatus());
            throw new InvalidBidException("Phiên này không ở trạng thái chờ duyệt!");
        }
        auction.setStatus(AuctionStatus.REJECTED);
        AuctionRepository.updateAuction(auction);
        LOGGER.info("Auction rejected: auction=" + auctionId);
    }

    // Entry point cho bid tay từ client: kiểm tra trạng thái, đặt bid thật, rồi kích hoạt auto-bid nếu có.
    public synchronized void placeBid(String auctionId, Bidder bidder, double amount) throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
        Auction auction = getAuctionById(auctionId);
        ensureAuctionReadyForBid(auction, auctionId, bidder.getId());

        placeBidInternal(auction, bidder, amount, false);
        processAutoBids(auction);
    }

    // Bật auto-bid cho bidder trong một auction, lưu giới hạn trong RAM và đặt bid ngay nếu bidder chưa dẫn đầu.
    public synchronized void setAutoBid(String auctionId, Bidder bidder, double maxBidLimit) throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
        Auction auction = getAuctionById(auctionId);
        ensureAuctionReadyForBid(auction, auctionId, bidder.getId());

        boolean bidderIsWinner = (auction.getWinner() != null) && (auction.getWinner().getId().equals(bidder.getId()));
        double requiredLimit = bidderIsWinner ? auction.getCurrentMaxPrice() : auction.getMinimumNextBid();
        if (maxBidLimit < requiredLimit) {
            throw new InvalidBidException("Giới hạn auto bid tối thiểu là " + requiredLimit);
        }

        bidder.enableAutoBid(maxBidLimit);
        autoBidSettings
                .computeIfAbsent(auctionId, id -> new HashMap<>())
                .put(bidder.getId(), new AutoBidRegistration(bidder, maxBidLimit));
        LOGGER.info("Auto bid enabled: auction=" + auctionId
                + ", bidder=" + bidder.getId()
                + ", max=" + maxBidLimit);

        if (!bidderIsWinner && auction.getMinimumNextBid() <= maxBidLimit) {
            placeBidInternal(auction, bidder, auction.getMinimumNextBid(), true);
        }
        processAutoBids(auction);
    }

    // Tắt auto-bid của bidder trong một auction và reset trạng thái auto-bid trên object bidder.
    public synchronized void disableAutoBid(String auctionId, Bidder bidder) throws InvalidBidException {
        Auction auction = getAuctionById(auctionId);
        if (auction == null) {
            throw new InvalidBidException("Không tìm thấy phiên đấu giá!");
        }
        removeAutoBidSetting(auctionId, bidder.getId());
        bidder.disableAutoBid();
        LOGGER.info("Auto bid disabled: auction=" + auctionId + ", bidder=" + bidder.getId());
    }

    // Chốt trạng thái mới nhất của auction trước khi bid để không đặt giá vào phiên đã hết hạn/chưa mở.
    private void ensureAuctionReadyForBid(Auction auction, String auctionId, String bidderId) throws InvalidBidException {
        if (auction == null) {
            LOGGER.warning("Bid failed. Auction not found: auction=" + auctionId + ", bidder=" + bidderId);
            throw new InvalidBidException("Không tìm thấy phiên đấu giá!");
        }
        if (auction.updateStatus()) {
            AuctionRepository.updateAuction(auction);
            LOGGER.info("Auction status updated before bid: auction=" + auctionId + ", status=" + auction.getStatus());
        }
    }

    // Hàm đặt giá lõi dùng chung cho bid tay và auto-bid: gọi entity, cập nhật tiền, lưu DB, notify realtime và ghi log.
    private void placeBidInternal(Auction auction, Bidder bidder, double amount, boolean automatic) throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
        String auctionId = auction.getId();
        Bidder previousWinner = auction.getWinner();

        try {
            auction.placeBid(bidder, amount);
            if (auction.extendEndTimeIfCloseToEnd(LATE_BID_EXTENSION_WINDOW_SECONDS, LATE_BID_EXTENSION_SECONDS)) {
                LOGGER.info("Auction extended after late bid: auction=" + auctionId
                        + ", newEndTime=" + auction.getEndTime());
            }
        } catch (InvalidBidException | InvalidTransactionException | InsufficientBalanceException e) {
            LOGGER.warning("Bid failed: auction=" + auctionId
                    + ", bidder=" + bidder.getId()
                    + ", amount=" + amount
                    + ", reason=" + e.getMessage());
            throw e;
        }

        if (previousWinner != null && !previousWinner.getId().equals(bidder.getId())) {
            UserRepository.updateBidderFunds(previousWinner);
        }
        UserRepository.updateBidderFunds(bidder);
        if (!auction.getHistoryBids().isEmpty()) {
            AuctionRepository.saveBid(auctionId, auction.getHistoryBids().get(auction.getHistoryBids().size() - 1));
        }
        for (BidTransaction bid : auction.getHistoryBids()) {
            AuctionRepository.updateBid(bid);
        }
        AuctionRepository.updateAuction(auction);
        auction.notifyUpdated();
        LOGGER.info((automatic ? "Auto bid placed: " : "Bid placed: ")
                + "auction=" + auctionId
                + ", bidder=" + bidder.getId()
                + ", amount=" + amount
                + ", currentPrice=" + auction.getCurrentMaxPrice()
                + ", winner=" + auction.getWinner().getId());
    }

    // Sau mỗi bid, tự tìm các bidder đã bật auto-bid và đặt tiếp từng bước giá cho đến khi không còn ai hợp lệ.
    private void processAutoBids(Auction auction) {
        int guard = 1000;
        while (guard-- > 0) {
            AutoBidRegistration nextAutoBid = findNextAutoBidder(auction);
            if (nextAutoBid == null) {
                return;
            }

            double nextAmount = auction.getMinimumNextBid();
            try {
                placeBidInternal(auction, nextAutoBid.bidder, nextAmount, true);
            } catch (InvalidBidException | InvalidTransactionException | InsufficientBalanceException e) {
                removeAutoBidSetting(auction.getId(), nextAutoBid.bidder.getId());
                nextAutoBid.bidder.disableAutoBid();
                LOGGER.warning("Auto bid disabled after failed bid: auction=" + auction.getId()
                        + ", bidder=" + nextAutoBid.bidder.getId()
                        + ", reason=" + e.getMessage());
            }
        }
        LOGGER.warning("Auto bid loop stopped by guard: auction=" + auction.getId());
    }

    // Chọn auto-bidder hợp lệ tiếp theo: không phải winner hiện tại, đủ limit, đủ tiền và có limit cao nhất.
    private AutoBidRegistration findNextAutoBidder(Auction auction) {
        Map<String, AutoBidRegistration> auctionAutoBids = autoBidSettings.get(auction.getId());
        if (auctionAutoBids == null || auctionAutoBids.isEmpty()) {
            return null;
        }

        String winnerId = auction.getWinner() == null ? null : auction.getWinner().getId();
        double nextBid = auction.getMinimumNextBid();
        AutoBidRegistration bestCandidate = null;

        for (AutoBidRegistration setting : auctionAutoBids.values()) {
            if (setting.bidder.getId().equals(winnerId)) {
                continue;
            }
            if (setting.maxBidLimit < nextBid || !setting.bidder.canAfford(nextBid)) {
                continue;
            }
            if (bestCandidate == null || setting.maxBidLimit > bestCandidate.maxBidLimit) {
                bestCandidate = setting;
            }
        }
        return bestCandidate;
    }

    // Xóa auto-bid của một bidder trong một auction; nếu auction không còn auto-bid nào thì xóa luôn bucket đó.
    private void removeAutoBidSetting(String auctionId, String bidderId) {
        Map<String, AutoBidRegistration> auctionAutoBids = autoBidSettings.get(auctionId);
        if (auctionAutoBids == null) {
            return;
        }
        auctionAutoBids.remove(bidderId);
        if (auctionAutoBids.isEmpty()) {
            autoBidSettings.remove(auctionId);
        }
    }

    // Xóa toàn bộ auto-bid của một bidder khỏi mọi auction khi logout, disconnect hoặc bị ban.
    private void removeAutoBidSettings(String bidderId) {
        for (String auctionId : new ArrayList<>(autoBidSettings.keySet())) {
            removeAutoBidSetting(auctionId, bidderId);
        }
    }

    // Bản ghi cấu hình auto-bid trong RAM: bidder nào và giới hạn tối đa là bao nhiêu.
    private static class AutoBidRegistration {
        private final Bidder bidder;
        private final double maxBidLimit;

        private AutoBidRegistration(Bidder bidder, double maxBidLimit) {
            this.bidder = bidder;
            this.maxBidLimit = maxBidLimit;
        }
    }
    
    // Hàm đóng các phiên đã hết hạn và lưu trạng thái mới xuống DB.
    public synchronized void closeExpiredAuctions() {
        for (Auction auction : auctions) {
            if (auction.updateStatus()) {
                AuctionRepository.updateAuction(auction);
                LOGGER.info("Auction status updated by scheduler: auction=" + auction.getId()
                        + ", status=" + auction.getStatus());
            }
        }
    }
    
    // Tạo thread scheduler tự gọi closeExpiredAuctions sau mỗi 3 giây để trạng thái auction tự chạy theo thời gian.
    public synchronized void startStatusScheduler(){
        if (this.statusScheduler != null && !this.statusScheduler.isShutdown()) {
            return;
        }

        this.statusScheduler = Executors.newSingleThreadScheduledExecutor();
        LOGGER.info("Auction status scheduler started.");

        this.statusScheduler.scheduleAtFixedRate(() ->{
            try {
                closeExpiredAuctions();
            } catch (Exception e) {
                LOGGER.severe("Status scheduler error: " + e.getMessage());
            }
        }, 0, 3, TimeUnit.SECONDS);
    }
}
