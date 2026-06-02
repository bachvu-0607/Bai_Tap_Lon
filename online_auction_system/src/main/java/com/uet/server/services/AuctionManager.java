package com.uet.server.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.uet.domain.AuctionSummary;
import com.uet.domain.BidHistoryPoint;
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
import com.uet.server.core.ClientHandler;
import com.uet.server.repositories.AuctionRepository;
import com.uet.server.repositories.UserRepository;

public class AuctionManager {
    private static AuctionManager instance;
    private List<String> onlineUsers = new ArrayList<>(); // Sổ ghi tên khách
    private List<Auction> auctions = new ArrayList<>();
    private final List <ClientHandler> clientHandlers = new ArrayList<>();
    private final RealtimeAuctionNotifier realtimeNotifier = new RealtimeAuctionNotifier(this);
    private ScheduledExecutorService statusScheduler;
    private static final Logger LOGGER = Logger.getLogger(AuctionManager.class.getName());
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

    // Logic kiểm tra đăng nhập cùng một tên đăng nhập nhưng có hai máy
    public boolean SignIn(String username) {
        synchronized (this) {
            if (onlineUsers.contains(username)) {
                LOGGER.warning("Rejected duplicate sign in for user=" + username);
                return false; 
            }
            onlineUsers.add(username); 
        }
        broadcastOnlineUsers();
        LOGGER.info("User signed in: " + username + ". Online users=" + getOnlineUsers());
        return true;
    }
    
    //SignOut Disconnect
    public void removeUser(String username) {
        boolean removed = false;
        synchronized (this) {
            if (username != null) {
                removed = onlineUsers.remove(username);
            }
        }
        if (removed) {
            broadcastOnlineUsers();
            LOGGER.info("User signed out: " + username + ". Online users=" + getOnlineUsers());
        }
    }

    
    public synchronized void addClient(ClientHandler client){
        if (!clientHandlers.contains(client)) {
            clientHandlers.add(client);
            LOGGER.info("Client handler added. Connected handlers=" + clientHandlers.size());
        }
    }
    
    public synchronized void removeClient(ClientHandler client){
        if (clientHandlers.remove(client)) {
            LOGGER.info("Client handler removed. Connected handlers=" + clientHandlers.size());
        }
    }

    public synchronized int getOnlineUsers(){
        return onlineUsers.size();
    }

    public void broadcastOnlineUsers() {
        broadcast(new ServerEvent(ServerEventType.ONLINE_USERS_UPDATED, getOnlineUsers()));
    }

    //Gửi cập nhật cho tất cả các clients hiện đang dùng ứng dụng
    public void broadcast(ServerEvent event){
        List<ClientHandler> curClientHandlers;
        synchronized (this) {
            curClientHandlers = new ArrayList<>(clientHandlers);
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

    public synchronized void placeBid(String auctionId, Bidder bidder, double amount) throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
        Auction auction = getAuctionById(auctionId);
        if (auction == null) {
            LOGGER.warning("Bid failed. Auction not found: auction=" + auctionId + ", bidder=" + bidder.getId());
            throw new InvalidBidException("Không tìm thấy phiên đấu giá!");
        }
        //update lại thời gian thực của phiên trước khi cho đấu giá
        if (auction.updateStatus()) {
            AuctionRepository.updateAuction(auction);
            LOGGER.info("Auction status updated before bid: auction=" + auctionId + ", status=" + auction.getStatus());
        }

        Bidder previousWinner = auction.getWinner();
        boolean hadWinnerBeforeBid = previousWinner != null;
        int bidCountBeforeBid = auction.getHistoryBids().size();

        try {
            auction.placeBid(bidder, amount);
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
        if (hadWinnerBeforeBid && bidCountBeforeBid == 0) {
            AuctionRepository.markWinningBidsOutbid(auctionId);
        }
        if (!auction.getHistoryBids().isEmpty()) {
            AuctionRepository.saveBid(auctionId, auction.getHistoryBids().get(auction.getHistoryBids().size() - 1));
        }
        if (auction.getHistoryBids().size() > 1) {
            AuctionRepository.updateBid(auction.getHistoryBids().get(auction.getHistoryBids().size() - 2));
        }
        AuctionRepository.updateAuction(auction);
        auction.notifyUpdated();
        LOGGER.info("Bid placed: auction=" + auctionId
                + ", bidder=" + bidder.getId()
                + ", amount=" + amount
                + ", currentPrice=" + auction.getCurrentMaxPrice()
                + ", winner=" + auction.getWinner().getId());
    }
    
    //Hàm đóng các phiên đã hết hạn
    public synchronized void closeExpiredAuctions() {
        for (Auction auction : auctions) {
            if (auction.updateStatus()) {
                AuctionRepository.updateAuction(auction);
                LOGGER.info("Auction status updated by scheduler: auction=" + auction.getId()
                        + ", status=" + auction.getStatus());
            }
        }
    }
    
        //Tạo thread tự đóng các phiên đã hết hạn sau mỗi 3s
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
