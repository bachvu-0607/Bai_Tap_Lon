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
import com.uet.server.core.ClientHandler;
import com.uet.server.repositories.AuctionRepository;

/**
 * Trình quản lý đấu giá trung tâm (Auction Manager) của phía Server.
 * Áp dụng mẫu thiết kế Singleton để duy trì trạng thái hệ thống duy nhất.
 * Quản lý danh sách kết nối client, danh sách phiên đấu giá hiện hành, danh sách người dùng online,
 * thực hiện điều phối đặt giá và lập lịch tự động cập nhật trạng thái phiên.
 */
public class AuctionManager {
    private static AuctionManager instance;
    private List<String> onlineUsers = new ArrayList<>(); // Sổ ghi tên khách
    private List<Auction> auctions = new ArrayList<>();
    private final List <ClientHandler> clientHandlers = new ArrayList<>();
    private final RealtimeAuctionNotifier realtimeNotifier = new RealtimeAuctionNotifier(this);
    private ScheduledExecutorService statusScheduler;
    
    /**
     * Khởi tạo private cho Singleton.
     */
    private AuctionManager() {}
    

    /**
     * Lấy thực thể duy nhất (Instance) của AuctionManager (Double-Checked Locking).
     * 
     * @return Đối tượng AuctionManager duy nhất.
     */
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

    /**
     * Đăng nhập người dùng vào danh sách online.
     * Ngăn chặn việc đăng nhập đồng thời trên nhiều thiết bị bằng cùng một tài khoản.
     * 
     * @param username Tên đăng nhập (System ID).
     * @return {@code true} nếu đăng nhập thành công; {@code false} nếu tài khoản đang online.
     */
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
    
    /**
     * Đăng xuất/Ngắt kết nối người dùng khỏi hệ thống.
     * Giải phóng tài khoản khỏi danh sách online và phát thông báo cập nhật số lượng người dùng trực tuyến.
     * 
     * @param username Tên đăng nhập (System ID).
     */
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

    /**
     * Thêm kết nối Client Handler mới vào danh sách phát sóng.
     * 
     * @param client Đối tượng {@link ClientHandler} kết nối.
     */
    public synchronized void addClient(ClientHandler client){
        if (!clientHandlers.contains(client)) {
            clientHandlers.add(client);
        }
    }
    
    /**
     * Gỡ bỏ kết nối Client Handler khỏi danh sách phát sóng.
     * 
     * @param client Đối tượng {@link ClientHandler} ngắt kết nối.
     */
    public synchronized void removeClient(ClientHandler client){
        clientHandlers.remove(client);
    }

    /**
     * Lấy số lượng người dùng trực tuyến hiện hành.
     * 
     * @return Số lượng người dùng online.
     */
    public synchronized int getOnlineUsers(){
        return onlineUsers.size();
    }

    /**
     * Phát sự kiện cập nhật số lượng người dùng online tới tất cả Client.
     */
    public void broadcastOnlineUsers() {
        broadcast(new ServerEvent(ServerEventType.ONLINE_USERS_UPDATED, getOnlineUsers()));
    }

    /**
     * Gửi (phát sóng) một sự kiện hệ thống tới tất cả các Client đang kết nối.
     * 
     * @param event Đối tượng {@link ServerEvent} cần gửi.
     */
    public void broadcast(ServerEvent event){
        List<ClientHandler> curClientHandlers;
        synchronized (this) {
            curClientHandlers = new ArrayList<>(clientHandlers);
        }
        //đảy event đi thông báo cho các client
        curClientHandlers.forEach(clientHandler -> clientHandler.sendEvent(event));
    }

    /**
     * Khởi tạo một đối tượng phiên đấu giá mới và đăng ký bộ quan sát cập nhật thời gian thực.
     * 
     * @param item Vật phẩm đem đấu giá.
     * @param seller Người bán sản phẩm.
     * @param startTime Thời gian bắt đầu đấu giá.
     * @param endTime Thời gian kết thúc đấu giá.
     * @param minIncrement Bước nhảy giá tối thiểu.
     * @return Đối tượng {@link Auction} vừa được khởi tạo.
     */
    public synchronized Auction createAuction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime, double minIncrement){
        Auction auction = new Auction(item, seller, startTime, endTime, minIncrement);
        auction.addObserver(realtimeNotifier);
        auctions.add(auction);
        return auction;
    }

    /**
     * Nạp toàn bộ thông tin phiên đấu giá từ cơ sở dữ liệu khi khởi động hệ thống.
     * Đăng ký Observer cập nhật thời gian thực và đóng các phiên đấu giá đã quá hạn.
     */
    public synchronized void loadAuctionsFromDatabase() {
        auctions.clear();
        auctions.addAll(AuctionRepository.loadAuctions());
        auctions.forEach(auction -> auction.addObserver(realtimeNotifier));
        closeExpiredAuctions();
        System.out.println("Loaded " + auctions.size() + " auctions from database.");
    }

    /**
     * Xử lý yêu cầu đăng tải sản phẩm đấu giá mới từ phía người bán (Seller).
     * Sản phẩm mới sẽ được gán trạng thái chờ duyệt (PENDING_APPROVAL) và lưu vào cơ sở dữ liệu.
     * 
     * @param request Dữ liệu yêu cầu đăng sản phẩm {@link ProductPostRequest}.
     * @param seller Đối tượng người bán {@link Seller}.
     * @return Đối tượng {@link Auction} vừa tạo đại diện cho sản phẩm chờ duyệt.
     */
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

    /**
     * Tạo đối tượng vật phẩm cụ thể (Art, Vehicle, Electronics) từ yêu cầu của Seller thông qua các Factory tương ứng.
     * 
     * @param request Dữ liệu yêu cầu đăng sản phẩm.
     * @return Đối tượng {@link Item} kế thừa.
     */
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

    /**
     * Khởi tạo dữ liệu mẫu (Seed) cho các phiên đấu giá thử nghiệm nếu danh sách đấu giá rỗng.
     */
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

    /**
     * Tìm phiên đấu giá theo mã định danh duy nhất (Auction ID).
     * 
     * @param auctionId Mã định danh phiên đấu giá.
     * @return Đối tượng {@link Auction} tìm thấy; {@code null} nếu không tìm thấy.
     */
    public synchronized Auction getAuctionById(String auctionId) {
        for (Auction auction : auctions) {
            if (auction.getId().equals(auctionId)) {
                return auction;
            }
        }
        return null;
    }

    /**
     * Lấy danh sách lịch sử các lượt đặt giá của một phiên từ bộ nhớ tạm Server.
     * 
     * @param auctionId Mã định danh phiên đấu giá.
     * @return Danh sách {@link BidHistoryPoint} lịch sử đặt giá.
     */
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
    
    /**
     * Tải trực tiếp lịch sử đặt giá của một phiên đấu giá từ cơ sở dữ liệu.
     * 
     * @param auctionId Mã định danh phiên đấu giá.
     * @return Danh sách {@link BidHistoryPoint} lịch sử đặt giá.
     */
    public synchronized List<BidHistoryPoint> getBidListFromDatabase(String auctionId){
        return AuctionRepository.loadBidHistory(auctionId);
    }

    /**
     * Lấy danh sách các phiên đấu giá đang hoạt động (OPEN hoặc RUNNING).
     * Tự động quét và đóng các phiên đã hết hạn trước khi trả về.
     * 
     * @return Danh sách các phiên đấu giá đang mở/đang chạy.
     */
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

    /**
     * Lấy danh sách tóm tắt (AuctionSummary DTO) của các phiên đấu giá đang hoạt động.
     * Dùng để truyền dữ liệu nhẹ về phía Client.
     * 
     * @return Danh sách tóm tắt phiên đấu giá.
     */
    public synchronized List<AuctionSummary> getActiveAuctionSummaries() {
        List<AuctionSummary> summaries = new ArrayList<>();
        for (Auction auction : getActiveAuctions()) {
            summaries.add(new AuctionSummary(auction));
        }
        return Collections.unmodifiableList(summaries);
    }

    /**
     * Lấy danh sách tóm tắt các phiên đấu giá đang chờ phê duyệt (cho Quản trị viên).
     * 
     * @return Danh sách tóm tắt phiên đấu giá chờ duyệt.
     */
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

    /**
     * Lấy danh sách tóm tắt các phiên đấu giá mà một Seller cụ thể đã đăng tải.
     * 
     * @param seller Đối tượng người bán {@link Seller}.
     * @return Danh sách tóm tắt phiên đấu giá của người bán đó.
     */
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

    /**
     * Phê duyệt một phiên đấu giá đang chờ duyệt (quyền Admin).
     * Nếu thời gian đã quá hạn kết thúc, phiên sẽ bị hủy (CANCELED).
     * 
     * @param auctionId Mã định danh phiên đấu giá cần duyệt.
     * @throws InvalidBidException Nếu không tìm thấy phiên hoặc phiên không ở trạng thái chờ duyệt hoặc đã quá hạn.
     */
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

    /**
     * Từ chối phê duyệt một phiên đấu giá đang chờ duyệt (quyền Admin).
     * 
     * @param auctionId Mã định danh phiên đấu giá bị từ chối.
     * @throws InvalidBidException Nếu không tìm thấy phiên hoặc phiên không ở trạng thái chờ duyệt.
     */
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
     * Thực hiện xử lý nghiệp vụ đặt giá (Bid) của người mua cho một phiên đấu giá.
     * Đồng bộ ghi nhận lượt đặt, cập nhật số dư bị khóa của người mua cũ/mới,
     * lưu trữ lịch sử đặt giá vào DB và thông báo tới các observer thời gian thực.
     * 
     * @param auctionId Mã định danh phiên đấu giá.
     * @param bidder Đối tượng người đặt giá {@link Bidder}.
     * @param amount Số tiền đặt giá.
     * @throws InvalidBidException Nếu lượt đặt giá vi phạm luật chơi.
     * @throws InvalidTransactionException Lỗi logic quỹ tiền hoặc giao dịch.
     * @throws InsufficientBalanceException Nếu số dư khả dụng không đủ.
     */
    public synchronized void placeBid(String auctionId, Bidder bidder, double amount) throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
        Auction auction = getAuctionById(auctionId);
        if (auction == null) {
            throw new InvalidBidException("Không tìm thấy phiên đấu giá!");
        }
        //update lại thời gian thực của phiên trước khi cho đấu giá
        if (auction.updateStatus()) {
            AuctionRepository.updateAuction(auction);
        }

        auction.placeBid(bidder, amount);
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
     * Quét qua danh sách các phiên đấu giá và cập nhật trạng thái vòng đời của chúng.
     * Đóng các phiên hết hạn, kích hoạt chạy các phiên đến giờ.
     */
    public synchronized void closeExpiredAuctions() {
        for (Auction auction : auctions) {
            if (auction.updateStatus()) {
                AuctionRepository.updateAuction(auction);
            }
        }
    }
    
    /**
     * Bắt đầu một tiến trình chạy ngầm (scheduler) định kỳ cập nhật trạng thái các phiên đấu giá
     * sau mỗi 3 giây để đảm bảo tính thời gian thực cho hệ thống.
     */
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
