package com.uet.server.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import com.uet.domain.enums.BidStatus;
import com.uet.server.core.ClientHandler;
import com.uet.server.repositories.AuctionRepository;
import com.uet.server.repositories.WalletRepository;

public class AuctionManager {
    private static AuctionManager instance;
    private List<String> onlineUsers = new ArrayList<>(); // Sổ ghi tên khách
    private List<Auction> auctions = new ArrayList<>();
    private final List <ClientHandler> clientHandlers = new ArrayList<>();
    private final RealtimeAuctionNotifier realtimeNotifier = new RealtimeAuctionNotifier(this);
    private ScheduledExecutorService statusScheduler;
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
        closeExpiredAuctions();
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

    public synchronized void placeBid(String auctionId, Bidder bidder, double amount) throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
        Auction auction = getAuctionById(auctionId);
        if (auction == null) {
            throw new InvalidBidException("Không tìm thấy phiên đấu giá!");
        }
        if (auction.updateStatus()) {
            AuctionRepository.updateAuction(auction);
        }

        // Capture previous winner before placing bid
        Bidder previousWinner = auction.getWinner();
        double previousAmount = auction.getCurrentMaxPrice();

        auction.placeBid(bidder, amount);

        // Persist balance changes for new bidder (funds locked)
        WalletRepository.updateBalance(bidder.getId(), bidder.getBalance(), bidder.getLockedBalance());
        WalletRepository.saveTransaction(bidder.getId(), "BID_LOCK", amount, "Tạm giữ tiền đặt giá: " + auction.getItem().getName());

        // Persist balance changes for previous winner (funds unlocked/returned)
        if (previousWinner != null) {
            WalletRepository.updateBalance(previousWinner.getId(), previousWinner.getBalance(), previousWinner.getLockedBalance());
            WalletRepository.saveTransaction(previousWinner.getId(), "BID_UNLOCK", previousAmount, "Hoàn tiền - bị đặt giá cao hơn: " + auction.getItem().getName());
        }

        if (!auction.getHistoryBids().isEmpty()) {
            AuctionRepository.saveBid(auctionId, auction.getHistoryBids().get(auction.getHistoryBids().size() - 1));
        }
        if (auction.getHistoryBids().size() > 1) {
            AuctionRepository.updateBid(auction.getHistoryBids().get(auction.getHistoryBids().size() - 2));
        }
        AuctionRepository.updateAuction(auction);
        auction.notifyUpdated();
    }
    
    /**
     * Duyệt qua tất cả phiên đấu giá, tự động cập nhật trạng thái theo thời gian thực.
     * - OPEN → RUNNING khi đến giờ bắt đầu
     * - RUNNING → FINISHED khi hết giờ và có người thắng
     * - RUNNING → CANCELED khi hết giờ nhưng không có ai đặt giá
     *
     * Khi phiên vừa chuyển sang FINISHED: tự động gọi processPayment()
     * để xử lý thanh toán ngay lập tức (không cần chờ thao tác thủ công).
     */
    public synchronized void closeExpiredAuctions() {
        for (Auction auction : auctions) {
            // updateStatus() trả về true nếu trạng thái vừa thay đổi
            if (auction.updateStatus()) {
                AuctionRepository.updateAuction(auction);

                // Nếu phiên vừa chuyển sang FINISHED (có người thắng),
                // thực hiện thanh toán tự động ngay lập tức
                if (auction.getStatus() == AuctionStatus.FINISHED) {
                    processPayment(auction);
                }
            }
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
}
