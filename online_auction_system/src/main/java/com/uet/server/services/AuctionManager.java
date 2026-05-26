package com.uet.server.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.uet.domain.AuctionSummary;
import com.uet.domain.BidHistoryPoint;
import com.uet.domain.UserSummary;
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
import com.uet.domain.result.AutoBidResult;
import com.uet.server.core.ClientHandler;
import com.uet.server.repositories.AuctionRepository;
import com.uet.server.repositories.WalletRepository;
import com.uet.server.repositories.UserRepository;

public class AuctionManager {
    private static AuctionManager instance;
    private List<String> onlineUsers = new ArrayList<>(); // Sổ ghi tên khách
    private List<Auction> auctions = new ArrayList<>();
    private final List <ClientHandler> clientHandlers = new ArrayList<>();
    private final RealtimeAuctionNotifier realtimeNotifier = new RealtimeAuctionNotifier(this);
    private ScheduledExecutorService statusScheduler;

    /**
     * Bản đồ lưu trữ các đăng ký auto-bid theo phiên đấu giá.
     * Key   : auctionId
     * Value : PriorityQueue<AutoBidEntry> — sắp xếp theo ưu tiên (maxBid cao hơn đứng trước,
     *         nếu bằng nhau thì người đăng ký trước đứng trước).
     *
     * ConcurrentHashMap để thread-safe khi thêm/xoá key (nội dung PriorityQueue
     * được truy cập bên trong các method synchronized của AuctionManager).
     */
    private final Map<String, PriorityQueue<AutoBidEntry>> autoBidMap = new ConcurrentHashMap<>();

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
                return false;
            }
            onlineUsers.add(username);
        }
        broadcastOnlineUsers();
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
            System.out.println("🚶 [AuctionManager] has removed: " + username + ". The number of guest using the system: " + getOnlineUsers());
        }
    }

    public synchronized void addClient(ClientHandler client){
        if (!clientHandlers.contains(client)) {
            clientHandlers.add(client);
        }
    }

    public synchronized void removeClient(ClientHandler client){
        clientHandlers.remove(client);
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
        auctions.addAll(AuctionRepository.loadAuctions());
        auctions.forEach(auction -> auction.addObserver(realtimeNotifier));
        for (Auction auction : auctions) {
            if (auction.updateStatusQuietly()) {
                AuctionRepository.updateAuction(auction);
            }
        }
        System.out.println("Loaded " + auctions.size() + " auctions from database.");
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
        for (Auction auction : auctions) {
            if (auction.updateStatusQuietly()) {
                AuctionRepository.updateAuction(auction);
            }
        }
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
        for (Auction auction : auctions) {
            if (auction.updateStatusQuietly()) {
                AuctionRepository.updateAuction(auction);
            }
        }
        List<AuctionSummary> summaries = new ArrayList<>();
        for (Auction auction : auctions) {
            if (auction.getStatus() == AuctionStatus.PENDING_APPROVAL) {
                summaries.add(new AuctionSummary(auction));
            }
        }
        return Collections.unmodifiableList(summaries);
    }

    public synchronized List<AuctionSummary> getSellerAuctionSummaries(Seller seller) {
        for (Auction auction : auctions) {
            if (auction.updateStatusQuietly()) {
                AuctionRepository.updateAuction(auction);
            }
        }
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
            throw new InvalidBidException("Không tìm thấy phiên đấu giá!");
        }
        if (auction.getStatus() != AuctionStatus.PENDING_APPROVAL) {
            throw new InvalidBidException("Phiên này không ở trạng thái chờ duyệt!");
        }

        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(auction.getEndTime())) {
            auction.setStatus(AuctionStatus.CANCELED);
            AuctionRepository.updateAuction(auction);
            throw new InvalidBidException("Phiên đã quá thời gian kết thúc, không thể duyệt!");
        }

        if (!now.isBefore(auction.getStartTime())) {
            auction.setStatus(AuctionStatus.RUNNING);
        } else {
            auction.setStatus(AuctionStatus.OPEN);
        }
        AuctionRepository.updateAuction(auction);
    }

    public synchronized void rejectAuction(String auctionId) throws InvalidBidException {
        Auction auction = getAuctionById(auctionId);
        if (auction == null) {
            throw new InvalidBidException("Không tìm thấy phiên đấu giá!");
        }
        if (auction.getStatus() != AuctionStatus.PENDING_APPROVAL) {
            throw new InvalidBidException("Phiên này không ở trạng thái chờ duyệt!");
        }
        auction.setStatus(AuctionStatus.REJECTED);
        AuctionRepository.updateAuction(auction);
    }

    /**
     * Xử lý lệnh đặt giá thủ công từ Bidder (gọi bởi ClientHandler).
     * Sau khi đặt giá thành công, tự động kích hoạt chuỗi auto-bid
     * để các bidder đang dùng auto-bid có cơ hội phản ứng ngay lập tức.
     */
    public synchronized void placeBid(String auctionId, Bidder bidder, double amount)
            throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
        Auction auction = getAuctionById(auctionId);
        if (auction == null) {
            throw new InvalidBidException("Không tìm thấy phiên đấu giá!");
        }
        // Thực hiện lệnh đặt giá (logic cốt lõi, dùng chung với auto-bid)
        doBidOnAuction(auction, bidder, amount);
        // Kích hoạt chuỗi auto-bid: các bidder đăng ký auto-bid sẽ tự động phản ứng
        triggerAutoBids(auction);
    }

    /**
     * Logic cốt lõi của một lượt đặt giá — dùng chung cho cả đặt giá thủ công và auto-bid.
     *
     * Thực hiện các bước:
     *   1. Cập nhật trạng thái phiên nếu cần (OPEN → RUNNING, v.v.)
     *   2. Gọi auction.placeBid() để kiểm tra và ghi nhận lượt đặt giá
     *   3. Kiểm tra anti-sniping: gia hạn phiên nếu bid rơi vào 60s cuối
     *   4. Tạm giữ tiền của bidder mới (lockFunds) và lưu vào DB
     *   5. Hoàn tiền cho winner cũ (unlockFunds) và lưu vào DB
     *   6. Lưu bid mới và cập nhật phiên đấu giá vào DB, rồi thông báo realtime
     *
     * Phương thức này KHÔNG gọi triggerAutoBids() để tránh đệ quy vô hạn.
     * Chỉ được gọi từ bên trong các method synchronized của AuctionManager.
     */
    private void doBidOnAuction(Auction auction, Bidder bidder, double amount)
            throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
        // Cập nhật trạng thái phiên nếu có thay đổi (VD: đến giờ bắt đầu/kết thúc)
        if (auction.updateStatus()) {
            AuctionRepository.updateAuction(auction);
        }

        // Ghi lại winner và giá hiện tại TRƯỚC khi đặt giá (để hoàn tiền sau)
        Bidder previousWinner = auction.getWinner();
        double previousAmount = auction.getCurrentMaxPrice();

        // Thực hiện lượt đặt giá (kiểm tra tính hợp lệ + cập nhật trạng thái trong bộ nhớ)
        auction.placeBid(bidder, amount);

        // Anti-sniping: gia hạn phiên nếu bid rơi vào cửa sổ cuối
        auction.checkAndExtendIfSniped();

        // Lưu tiền tạm giữ của bidder mới vào DB
        WalletRepository.updateBalance(bidder.getId(), bidder.getBalance(), bidder.getLockedBalance());
        WalletRepository.saveTransaction(bidder.getId(), "BID_LOCK", amount,
                "Tạm giữ tiền đặt giá: " + auction.getItem().getName());

        // Hoàn tiền cho winner cũ (nếu có) và lưu vào DB
        if (previousWinner != null) {
            WalletRepository.updateBalance(previousWinner.getId(),
                    previousWinner.getBalance(), previousWinner.getLockedBalance());
            WalletRepository.saveTransaction(previousWinner.getId(), "BID_UNLOCK", previousAmount,
                    "Hoàn tiền - bị đặt giá cao hơn: " + auction.getItem().getName());
        }

        // Lưu lượt đặt giá mới vào DB
        if (!auction.getHistoryBids().isEmpty()) {
            AuctionRepository.saveBid(auction.getId(),
                    auction.getHistoryBids().get(auction.getHistoryBids().size() - 1));
        }
        // Cập nhật trạng thái lượt đặt giá trước (từ WINNING → OUTBID)
        if (auction.getHistoryBids().size() > 1) {
            AuctionRepository.updateBid(auction.getHistoryBids().get(auction.getHistoryBids().size() - 2));
        }
        // Cập nhật phiên đấu giá vào DB và gửi thông báo realtime đến các client
        AuctionRepository.updateAuction(auction);
        auction.notifyUpdated();
    }

    // ══════════════════════════════════════════════════════
    //  AUTO-BIDDING — Đấu giá tự động
    // ══════════════════════════════════════════════════════

    /**
     * Đăng ký hoặc cập nhật auto-bid cho một Bidder tại một phiên đấu giá.
     *
     * Luồng xử lý:
     *   1. Kiểm tra điều kiện hợp lệ (phiên đang chạy, maxBid >= giá tối thiểu, số dư đủ)
     *   2. Xoá đăng ký cũ của bidder này (nếu đã có) → thêm entry mới vào PriorityQueue
     *   3. Gọi triggerAutoBids() để hệ thống tự động đặt giá ngay nếu bidder chưa đang thắng
     *
     * @param auctionId ID phiên đấu giá
     * @param bidder    Người dùng muốn đặt auto-bid
     * @param maxBid    Giá tối đa bidder chấp nhận
     * @param increment Bước giá mỗi lần hệ thống tự đặt
     */
    public synchronized AutoBidResult setAutoBid(String auctionId, Bidder bidder,
                                                  double maxBid, double increment) {
        // Kiểm tra phiên có tồn tại không
        Auction auction = getAuctionById(auctionId);
        if (auction == null) {
            return AutoBidResult.failed("Không tìm thấy phiên đấu giá!");
        }

        // Cập nhật trạng thái phiên trước khi kiểm tra
        if (auction.updateStatus()) {
            AuctionRepository.updateAuction(auction);
        }

        // Chỉ cho phép auto-bid khi phiên đang chạy
        if (auction.getStatus() != AuctionStatus.RUNNING) {
            return AutoBidResult.failed("Phiên đấu giá chưa bắt đầu hoặc đã kết thúc!");
        }

        // Kiểm tra tham số đầu vào
        if (maxBid <= 0 || increment <= 0) {
            return AutoBidResult.failed("Giá tối đa và bước giá phải lớn hơn 0!");
        }

        // maxBid phải đủ để có thể tham gia ít nhất một lượt đấu giá
        if (maxBid < auction.getMinimumNextBid()) {
            return AutoBidResult.failed(
                    "Giá tối đa phải lớn hơn hoặc bằng giá đặt tối thiểu hiện tại: "
                    + (long) auction.getMinimumNextBid() + " đ");
        }

        // Kiểm tra số dư khả dụng (balance - lockedBalance) có đủ không
        if (!bidder.canAfford(auction.getMinimumNextBid())) {
            return AutoBidResult.failed("Số dư khả dụng không đủ để tham gia đấu giá!");
        }

        // Lấy (hoặc tạo mới) PriorityQueue cho phiên này
        PriorityQueue<AutoBidEntry> queue =
                autoBidMap.computeIfAbsent(auctionId, k -> new PriorityQueue<>());

        // Xoá đăng ký cũ của bidder này (nếu có) → người dùng đang cập nhật auto-bid
        queue.removeIf(e -> e.getBidderId().equals(bidder.getId()));

        // Thêm entry mới với thời điểm đăng ký hiện tại
        queue.add(new AutoBidEntry(bidder, maxBid, increment));

        System.out.printf("🤖 [AutoBid] %s đăng ký auto-bid | Phiên: %s | Max: %.0f đ | Step: %.0f đ%n",
                bidder.getName(), auctionId, maxBid, increment);

        // Kích hoạt ngay: nếu bidder chưa đang thắng, hệ thống sẽ tự đặt giá cho họ
        triggerAutoBids(auction);

        return AutoBidResult.success(
                "Đặt Auto Bid thành công! Hệ thống sẽ tự động đấu giá thay bạn.",
                true, maxBid, increment);
    }

    /**
     * Huỷ auto-bid của một Bidder cho một phiên đấu giá.
     * Lưu ý: Tiền đã được tạm giữ từ các lượt đặt giá trước đó KHÔNG được hoàn lại ở đây
     *        (tiền được hoàn khi có người khác đặt giá cao hơn, không phải khi huỷ auto-bid).
     */
    public synchronized AutoBidResult cancelAutoBid(String auctionId, String bidderId) {
        PriorityQueue<AutoBidEntry> queue = autoBidMap.get(auctionId);
        if (queue == null) {
            return AutoBidResult.failed("Bạn không có auto-bid nào đang chạy cho phiên này.");
        }

        boolean removed = queue.removeIf(e -> e.getBidderId().equals(bidderId));
        if (!removed) {
            return AutoBidResult.failed("Bạn không có auto-bid nào đang chạy cho phiên này.");
        }

        System.out.printf("🛑 [AutoBid] Bidder %s đã huỷ auto-bid cho phiên %s%n", bidderId, auctionId);
        return AutoBidResult.success("Huỷ Auto Bid thành công.", false, 0, 0);
    }

    /**
     * Lấy trạng thái auto-bid hiện tại của một Bidder cho một phiên đấu giá.
     * Dùng để hiển thị lên UI: bidder có đang auto-bid không, maxBid và increment là bao nhiêu.
     */
    public synchronized AutoBidResult getAutoBidStatus(String auctionId, String bidderId) {
        PriorityQueue<AutoBidEntry> queue = autoBidMap.get(auctionId);
        if (queue != null) {
            for (AutoBidEntry entry : queue) {
                if (entry.getBidderId().equals(bidderId)) {
                    return AutoBidResult.success("Đang có auto-bid hoạt động.",
                            true, entry.getMaxBid(), entry.getIncrement());
                }
            }
        }
        return AutoBidResult.success("Chưa đăng ký auto-bid cho phiên này.", false, 0, 0);
    }

    /**
     * Kích hoạt chuỗi phản ứng auto-bid sau mỗi lượt đặt giá (thủ công hoặc auto).
     *
     * Thuật toán:
     *   Lặp tối đa MAX_AUTO_BID_ROUNDS vòng:
     *     1. Lấy winner hiện tại và giá hiện tại của phiên
     *     2. Tìm candidate ưu tiên cao nhất trong PriorityQueue thoả mãn:
     *          - Không phải là winner hiện tại (không cần tự outbid mình)
     *          - maxBid >= giá tối thiểu tiếp theo của phiên
     *          - Có đủ số dư khả dụng
     *     3. Nếu tìm được → đặt giá cho họ (doBidOnAuction), rồi lặp lại
     *     4. Nếu không tìm được → dừng vòng lặp
     *
     * Giá đặt = min(currentPrice + max(candidate.increment, auction.minIncrement), candidate.maxBid)
     * Đảm bảo giá đặt luôn >= giá tối thiểu tiếp theo.
     *
     * Vòng lặp sẽ kết thúc tự nhiên khi không còn ai có thể outbid winner,
     * hoặc khi đạt giới hạn MAX_AUTO_BID_ROUNDS (an toàn).
     */
    private void triggerAutoBids(Auction auction) {
        // Số vòng tối đa để tránh vòng lặp vô hạn trong trường hợp lỗi logic
        final int MAX_AUTO_BID_ROUNDS = 50;

        String auctionId = auction.getId();
        PriorityQueue<AutoBidEntry> queue = autoBidMap.get(auctionId);
        if (queue == null || queue.isEmpty()) return;

        for (int round = 0; round < MAX_AUTO_BID_ROUNDS; round++) {
            // Dừng nếu phiên không còn chạy (đã hết giờ hoặc bị huỷ)
            if (auction.getStatus() != AuctionStatus.RUNNING) break;

            String currentWinnerId = (auction.getWinner() != null)
                    ? auction.getWinner().getId() : null;
            double minNextBid = auction.getMinimumNextBid();

            // Sắp xếp các entry theo ưu tiên để tìm candidate tốt nhất
            // (PriorityQueue không hỗ trợ iteration theo thứ tự, nên phải tạo list tạm)
            List<AutoBidEntry> sortedEntries = new ArrayList<>(queue);
            Collections.sort(sortedEntries); // Theo compareTo: maxBid giảm dần, registeredAt tăng dần

            // Tìm candidate ưu tiên cao nhất thoả điều kiện
            AutoBidEntry bestCandidate = null;
            for (AutoBidEntry entry : sortedEntries) {
                // Bỏ qua nếu đang là winner (không cần outbid chính mình)
                if (entry.getBidderId().equals(currentWinnerId)) continue;
                // Bỏ qua nếu maxBid không đủ để đặt ít nhất một lượt
                if (entry.getMaxBid() < minNextBid) continue;
                bestCandidate = entry;
                break; // Lấy người đầu tiên (ưu tiên cao nhất)
            }

            // Không tìm được ai có thể phản ứng → dừng chuỗi
            if (bestCandidate == null) break;

            // Tính giá sẽ đặt:
            // Dùng bước giá lớn hơn giữa increment của auto-bid và minIncrement của phiên
            double step = Math.max(bestCandidate.getIncrement(), auction.getMinIncrement());
            double bidAmount = auction.getCurrentMaxPrice() + step;

            // Giới hạn không vượt quá maxBid của candidate
            if (bidAmount > bestCandidate.getMaxBid()) {
                bidAmount = bestCandidate.getMaxBid();
            }

            // Đảm bảo giá đặt thoả mãn điều kiện tối thiểu của phiên
            bidAmount = Math.max(bidAmount, minNextBid);

            // Kiểm tra lần cuối: giá đặt có hợp lệ và candidate có đủ tiền không
            if (bidAmount > bestCandidate.getMaxBid()) break;
            if (!bestCandidate.getBidder().canAfford(bidAmount)) break;

            // Thực hiện lượt đặt giá tự động
            try {
                doBidOnAuction(auction, bestCandidate.getBidder(), bidAmount);
                System.out.printf(
                        "🤖 [AutoBid R%d] %s tự động đặt %.0f đ | Phiên: %s%n",
                        round + 1, bestCandidate.getBidder().getName(), bidAmount, auctionId);
            } catch (Exception e) {
                System.err.printf("❌ [AutoBid] Lỗi khi tự động đặt giá: %s%n", e.getMessage());
                break; // Dừng chuỗi nếu có lỗi
            }
        }
    }

    /**
     * Duyệt qua tất cả phiên đấu giá, tự động cập nhật trạng thái theo thời gian thực.
     * - OPEN → RUNNING khi đến giờ bắt đầu
     * - RUNNING → FINISHED khi hết giờ và có người thắng
     * - RUNNING → CANCELED khi hết giờ nhưng không có ai đặt giá
     *
     * Khi phiên vừa chuyển sang FINISHED: tự động gọi processPayment()
     * để xử lý thanh toán ngay lập tức (không cần chờ thao tác thủ công).
     *
     * Chạy ngoài lock để I/O không block AuctionManager.
     */
    public void closeExpiredAuctions() {
        List<Auction> changed = new ArrayList<>();
        List<Auction> finished = new ArrayList<>();
        synchronized (this) {
            for (Auction auction : auctions) {
                if (auction.updateStatusQuietly()) {
                    AuctionRepository.updateAuction(auction);
                    if (auction.getStatus() == AuctionStatus.FINISHED) {
                        finished.add(auction);
                    } else {
                        changed.add(auction);
                    }
                }
            }
        }
        // Xử lý thanh toán ngoài lock (tránh giữ lock trong khi I/O)
        for (Auction auction : finished) {
            processPayment(auction); // processPayment tự gọi notifyUpdated()
        }
        for (Auction auction : changed) {
            auction.notifyUpdated();
        }
    }

    /**
     * Xử lý thanh toán tự động khi phiên đấu giá kết thúc có người thắng.
     *
     * Luồng xử lý:
     * 1. Kiểm tra tiền tạm giữ của winner có đủ không
     *    - Nếu ĐỦ (đặt giá sau khi có wallet): gọi confirmPayment() trừ tiền chính thức
     *    - Nếu THIẾU (dữ liệu cũ trước khi có wallet): chỉ cập nhật trạng thái, không trừ tiền
     * 2. Chuyển tiền vào ví của người bán (seller)
     * 3. Cập nhật trạng thái phiên → PAID trong database
     * 4. Ghi lịch sử giao dịch cho cả winner và seller
     * 5. Thông báo đến tất cả client đang kết nối
     *
     * Phương thức này được thiết kế chịu lỗi (try-catch toàn bộ):
     * nếu thanh toán thất bại, phiên vẫn giữ trạng thái FINISHED và không ảnh hưởng
     * đến các phiên khác đang chạy.
     *
     * @param auction Phiên đấu giá vừa chuyển sang trạng thái FINISHED
     */
    private void processPayment(Auction auction) {
        // Lấy thông tin các bên liên quan
        Bidder winner = auction.getWinner();
        Seller seller = auction.getSeller();
        double amount  = auction.getCurrentMaxPrice();
        String itemName = auction.getItem().getName();

        try {
            if (winner.getLockedBalance() >= amount) {
                // ── TRƯỜNG HỢP BÌNH THƯỜNG (đặt giá sau khi có hệ thống wallet) ──
                // winner đã có đúng số tiền bị tạm giữ → tiến hành trừ tiền chính thức

                // confirmPayment() sẽ:
                //   · gọi winner.commitPayment(amount): balance -= amount, lockedBalance -= amount
                //   · chuyển trạng thái phiên → PAID
                //   · đánh dấu item → SOLD
                auction.confirmPayment();

                // Lưu số dư mới của winner vào database
                WalletRepository.updateBalance(winner.getId(), winner.getBalance(), winner.getLockedBalance());

                // Ghi lịch sử: PAYMENT — tiền bị trừ vĩnh viễn khỏi ví winner
                WalletRepository.saveTransaction(winner.getId(), "PAYMENT", amount,
                        "Thanh toán đấu giá thành công: " + itemName);

            } else {
                // ── TRƯỜNG HỢP DỮ LIỆU CŨ (đặt giá trước khi có hệ thống wallet) ──
                // Không có tiền tạm giữ trong DB → chỉ cập nhật trạng thái, không trừ tiền
                // (Tiền đã được xử lý theo cách cũ bên ngoài hệ thống)

                // Đánh dấu item là đã bán
                auction.getItem().setStatus(com.uet.domain.enums.ItemStatus.SOLD);
                // Chuyển trạng thái phiên sang PAID (dùng setStatus để observer được thông báo)
                auction.setStatus(AuctionStatus.PAID);
            }

            // Cập nhật trạng thái bid cuối cùng từ WINNING → PAID trực tiếp trong DB
            // (dùng SQL vì historyBids trong bộ nhớ thường rỗng với auction được load từ DB)
            AuctionRepository.markWinningBidAsPaid(auction.getId());

            // ── XỬ LÝ PHẦN TIỀN CỦA SELLER (áp dụng cả hai trường hợp) ──

            // Cộng tiền vào ví seller
            seller.deposit(amount);

            // Lưu số dư mới của seller vào database (seller không có lockedBalance → truyền 0)
            WalletRepository.updateBalance(seller.getId(), seller.getBalance(), 0);

            // Ghi lịch sử: SALE_INCOME — seller nhận được tiền từ phiên đấu giá thành công
            WalletRepository.saveTransaction(seller.getId(), "SALE_INCOME", amount,
                    "Thu tiền từ phiên đấu giá thành công: " + itemName);

            // Lưu trạng thái phiên (PAID) vào database
            AuctionRepository.updateAuction(auction);

            // Gửi thông báo AUCTION_UPDATED đến tất cả client đang kết nối
            // để họ làm mới danh sách đấu giá và thấy trạng thái PAID
            auction.notifyUpdated();

            System.out.printf("✅ [Payment] Thanh toán thành công | Phiên: %s | Sản phẩm: %s | Giá: %.0f đ | Winner: %s%n",
                    auction.getId(), itemName, amount, winner.getName());

        } catch (Exception e) {
            // Ghi log lỗi nhưng không ném ngoại lệ ra ngoài để không làm đổ scheduler
            System.err.printf("❌ [Payment] Lỗi xử lý thanh toán phiên %s: %s%n",
                    auction.getId(), e.getMessage());
        }
    }

    //Tạo thread tự đóng các phiên đã hết hạn sau mỗi 3s
    public synchronized void startStatusScheduler(){
        if (this.statusScheduler != null && !this.statusScheduler.isShutdown()) {
            return;
        }

        this.statusScheduler = Executors.newSingleThreadScheduledExecutor();

        this.statusScheduler.scheduleAtFixedRate(() ->{
            try {
                closeExpiredAuctions();
            } catch (Exception e) {
                System.err.println("Status scheduler error: " + e.getMessage());
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    public List<UserSummary> getUserSummaries() {
        return UserRepository.getAllNonAdminUsers();
    }

    public boolean deleteUserAccount(String systemId) {
        return UserRepository.removeUserById(systemId);
    }
}
