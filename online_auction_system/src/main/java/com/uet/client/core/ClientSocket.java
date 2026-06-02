package com.uet.client.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.uet.client.utils.SessionManager;
import com.uet.domain.AuctionSummary;
import com.uet.domain.BidHistoryPoint;
import com.uet.domain.event.ServerEvent;
import com.uet.domain.request.AuctionApprovalRequest;
import com.uet.domain.request.AuctionRequest;
import com.uet.domain.request.BidRequest;
import com.uet.domain.request.ProductPostRequest;
import com.uet.domain.request.RegisterRequest;
import com.uet.domain.request.SignInRequest;
import com.uet.domain.result.AuctionActionResult;
import com.uet.domain.result.AuthenticationResult;
import com.uet.domain.result.BidResult;
import com.uet.domain.result.ProductPostResult;

/**
 * Lớp chịu trách nhiệm quản lý kết nối Socket TCP phía Client.
 * Cung cấp luồng nhận sự kiện thời gian thực từ Server và các phương thức đồng bộ gửi yêu cầu (Request)
 * và chờ phản hồi (Response) từ Server.
 */
public class ClientSocket{
    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static final BlockingQueue<Object> responseQueue = new LinkedBlockingQueue<>();  //Thread - safe
    private static Consumer <ServerEvent> eventListener;     // một hàm nhận vào biến kiểu ServerEvent và return gì cả
    private static Consumer <ServerEvent> globalEventListener;
    private static Thread listenerThread;                   //Thread nhận nhiệm vụ nghe ngóng response từ server
    private static String IP_address = "localhost";


    /**
     * Thực hiện kết nối tới Socket Server tại cổng 8080.
     * Khởi tạo các dòng xuất nhập đối tượng và bắt đầu Thread lắng nghe sự kiện từ Server.
     * 
     * @throws Exception Nếu kết nối Socket thất bại.
     */
    public static void connect() throws Exception{
        if(socket == null || socket.isClosed()){
            socket = new Socket(IP_address, 8080);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            startListenerThread();
            System.out.println("Connect to server successfully!");
        }
    }

    /**
     * Đảm bảo rằng kết nối Socket vẫn hoạt động bình thường, nếu chưa sẽ tự động gọi connect().
     * 
     * @throws Exception Nếu không thể thiết lập kết nối.
     */
    private static void ensureConnected() throws Exception {
        if (socket == null || socket.isClosed() || out == null || in == null) {
            connect();
        }
    }
    
    /**
     * Gán trình lắng nghe sự kiện cục bộ (Local Event Listener) cho màn hình cụ thể.
     * 
     * @param listener Biến Consumer nhận {@link ServerEvent}.
     */
    public static void setEventListener(Consumer <ServerEvent> listener){
        eventListener = listener;
    }

    /**
     * Gán trình lắng nghe sự kiện toàn cục (Global Event Listener) để cập nhật thông tin chung.
     * 
     * @param listener Biến Consumer nhận {@link ServerEvent}.
     */
    public static void setGlobalEventListener(Consumer <ServerEvent> listener){
        globalEventListener = listener;
    }

    /**
     * Bắt đầu một luồng ngầm (Daemon Thread) làm nhiệm vụ liên tục lắng nghe và nhận dữ liệu từ Server.
     */
    private static void startListenerThread(){
        if(listenerThread != null && listenerThread.isAlive()){
            return;
        }

        Runnable listenTask = () -> listenToServer();

        listenerThread = new Thread(listenTask);
        //Tạo Daemon thread để khi app chết tự chết, ko cần cập nhật gì nữa
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Thực hiện vòng lặp đọc dữ liệu từ Socket.
     * Nếu nhận được sự kiện (ServerEvent), phân phối đến các listener tương ứng.
     * Nếu là các đối tượng Response khác, đẩy vào hàng đợi responseQueue để đồng bộ xử lý.
     */
    private static void listenToServer(){
        try {
            while(socket != null && !socket.isClosed()){
                Object object = in.readObject();

                //Đọc object server gửi về 
                //Nếu là event thì -> đẩy cho eventListener (để thông báo cho UI cập nhật)
                if(object instanceof ServerEvent event){
                    Consumer<ServerEvent> globalListener = globalEventListener;
                    if(globalListener != null){
                        globalListener.accept(event);
                    }
                    Consumer<ServerEvent> listener = eventListener;
                    if(listener != null){
                        //truyền event vào cái listener
                        listener.accept(event);
                    }
                //Nếu là response khác thì đẩy vào Queue
                }else{
                    responseQueue.offer(object);
                }
            }
        } catch (Exception e) {
            if (socket != null && !socket.isClosed()) {
                System.err.println("Socket listener stopped: " + e.getMessage());                
            }
        }    
    }

    /**
     * Đọc phản hồi đồng bộ từ responseQueue với thời gian chờ tối đa 10 giây.
     * 
     * @return Đối tượng phản hồi từ Server.
     * @throws IOException Nếu quá thời gian chờ (Timeout).
     * @throws InterruptedException Nếu tiến trình đọc bị gián đoạn.
     */
    private static Object readResponse() throws IOException, InterruptedException {
        Object response = responseQueue.poll(10, TimeUnit.SECONDS);
        if (response == null) {
            throw new IOException("Server response timeout");
        }
        return response;
    }

    /**
     * Gửi yêu cầu lên Server và chặn luồng để chờ nhận phản hồi kết quả một cách đồng bộ.
     * 
     * @param request Đối tượng yêu cầu {@link AuctionRequest}.
     * @return Đối tượng phản hồi tương ứng.
     * @throws Exception Nếu xảy ra lỗi kết nối hoặc truyền dữ liệu.
     */
    private static synchronized Object sendRequestAndWait(AuctionRequest request) throws Exception {
        ensureConnected();
        out.writeObject(request);
        out.flush();
        return readResponse();
    }

    /**
     * Gửi yêu cầu đăng nhập.
     * 
     * @param username Tên đăng nhập (CCCD hoặc SĐT).
     * @param password Mật khẩu.
     * @return Đối tượng kết quả xác thực {@link AuthenticationResult}.
     * @throws Exception Nếu xảy ra lỗi truyền thông.
     */
    public static AuthenticationResult sendSignIn(String username, String password) throws Exception{
        SignInRequest signInRequest = new SignInRequest(username, password);
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.SIGN_IN, signInRequest);
        return (AuthenticationResult) sendRequestAndWait(request);
    }
    
    /**
     * Gửi yêu cầu đăng ký tài khoản mới.
     * 
     * @param name Họ và tên.
     * @param phone Số điện thoại.
     * @param citizenId Số Căn cước công dân.
     * @param password Mật khẩu.
     * @param address Địa chỉ nơi ở.
     * @param role Vai trò đăng ký ("Bidder" hoặc "Seller").
     * @return Đối tượng kết quả xác thực {@link AuthenticationResult}.
     * @throws Exception Nếu xảy ra lỗi truyền thông.
     */
    public static AuthenticationResult sendRegister(String name, String phone, String citizenId, String password, String address, String role) throws Exception{
        RegisterRequest registerRequest = new RegisterRequest(name, phone, citizenId, password, address, role);
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.REGISTER, registerRequest);
        return (AuthenticationResult) sendRequestAndWait(request);
    }
    
    /**
     * Yêu cầu danh sách các phiên đấu giá đang hoạt động.
     * 
     * @return Danh sách các DTO tóm tắt phiên đấu giá {@link AuctionSummary}.
     * @throws Exception Nếu xảy ra lỗi truyền thông.
     */
    @SuppressWarnings("unchecked")
    public static List<AuctionSummary> getAuctionList() throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_LIST, null);
        return (List<AuctionSummary>) sendRequestAndWait(request);
    }

    /**
     * Yêu cầu số lượng người dùng trực tuyến hiện hành.
     * 
     * @return Số lượng người dùng online.
     * @throws Exception Nếu xảy ra lỗi truyền thông.
     */
    public static int getOnlineUsers() throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_ONLINE_USERS, null);
        return (int) sendRequestAndWait(request);
    }

    /**
     * Yêu cầu danh sách các phiên đấu giá đang chờ phê duyệt (quyền Admin).
     * 
     * @return Danh sách tóm tắt các phiên chờ duyệt.
     * @throws Exception Nếu xảy ra lỗi truyền thông.
     */
    @SuppressWarnings("unchecked")
    public static List<AuctionSummary> getPendingAuctionList() throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_PENDING_AUCTIONS, null);
        return (List<AuctionSummary>) sendRequestAndWait(request);
    } 

    /**
     * Yêu cầu danh sách các sản phẩm/phiên đấu giá do Seller hiện tại đăng tải.
     * 
     * @return Danh sách phiên đấu giá của Seller.
     * @throws Exception Nếu xảy ra lỗi truyền thông.
     */
    @SuppressWarnings("unchecked")
    public static List<AuctionSummary> getSellerProductList() throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_SELLER_PRODUCTS, null);
        return (List<AuctionSummary>) sendRequestAndWait(request);
    }

    /**
     * Yêu cầu lịch sử đặt giá của một phiên đấu giá.
     * 
     * @param auctionId Mã định danh phiên đấu giá.
     * @return Danh sách các mốc đặt giá.
     * @throws Exception Nếu xảy ra lỗi truyền thông.
     */
    @SuppressWarnings("unchecked")
    public static List<BidHistoryPoint> getHistoryBidList(String auctionId) throws Exception{
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_BID_HISTORY, auctionId);
        return (List<BidHistoryPoint>) sendRequestAndWait(request);
    }
    
    /**
     * Yêu cầu duyệt một phiên đấu giá (quyền Admin).
     * 
     * @param auctionId Mã định danh phiên đấu giá.
     * @return Kết quả thao tác {@link AuctionActionResult}.
     * @throws Exception Nếu xảy ra lỗi truyền thông.
     */
    public static AuctionActionResult approveAuction(String auctionId) throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.APPROVE_AUCTION, new AuctionApprovalRequest(auctionId));
        return (AuctionActionResult) sendRequestAndWait(request);
    }
    
    /**
     * Yêu cầu từ chối một phiên đấu giá (quyền Admin).
     * 
     * @param auctionId Mã định danh phiên đấu giá.
     * @return Kết quả thao tác {@link AuctionActionResult}.
     * @throws Exception Nếu xảy ra lỗi truyền thông.
     */
    public static AuctionActionResult rejectAuction(String auctionId) throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.REJECT_AUCTION, new AuctionApprovalRequest(auctionId));
        return (AuctionActionResult) sendRequestAndWait(request);
    }

    /**
     * Thực hiện đặt giá cho một sản phẩm (quyền Bidder).
     * 
     * @param auctionId Mã phiên đấu giá.
     * @param amount Số tiền đặt giá.
     * @return Kết quả đặt giá {@link BidResult}.
     * @throws Exception Nếu xảy ra lỗi truyền thông.
     */
    public static BidResult sendBid(String auctionId, double amount) throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.BID, new BidRequest(auctionId, amount));
        return (BidResult) sendRequestAndWait(request);
    }
   
    /**
     * Gửi yêu cầu đăng bán sản phẩm đấu giá mới (quyền Seller).
     * 
     * @param productType Loại sản phẩm ("Art", "Vehicle", "Electronics").
     * @param productName Tên sản phẩm.
     * @param description Mô tả chi tiết.
     * @param openingPrice Giá khởi điểm.
     * @param minIncrement Bước giá tối thiểu.
     * @param startTime Thời điểm bắt đầu.
     * @param endTime Thời điểm kết thúc.
     * @param imageLink Link ảnh sản phẩm.
     * @return Kết quả đăng sản phẩm {@link ProductPostResult}.
     * @throws Exception Nếu xảy ra lỗi truyền thông.
     */
    public static ProductPostResult postProduct(String productType, String productName, String description, double openingPrice,
                                                double minIncrement, LocalDateTime startTime, LocalDateTime endTime,
                                                String imageLink) throws Exception {
        ProductPostRequest postRequest = new ProductPostRequest(
                productType,
                productName,
                description,
                openingPrice,
                minIncrement,
                startTime,
                endTime,
                imageLink);
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.POST_PRODUCT, postRequest);
        return (ProductPostResult) sendRequestAndWait(request);
    }
    
    /**
     * Gửi yêu cầu ngắt kết nối/đăng xuất lên Server và giải phóng tài nguyên Socket cục bộ.
     */
    public static void sendDisconnect() {
        try {
            if (SessionManager.currentUser != null && out != null) {
                String username = SessionManager.currentUser.getId();
                AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.DISCONNECT,username);
                out.writeObject(request);
                out.flush();
                System.out.println("Đã gửi yêu cầu đăng xuất lên Server.");
            }
        } catch (IOException e) {
            System.err.println("Không thể gửi yêu cầu đăng xuất: " + e.getMessage());
        } finally {
            closeConnection();
            SessionManager.clearSession();
        }
    }
    
    /**
     * Phương thức giải phóng Socket, dòng xuất nhập và dọn dẹp hàng đợi.
     */
    private static void closeConnection() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            System.err.println("Không thể đóng input stream: " + e.getMessage());
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            System.err.println("Không thể đóng output stream: " + e.getMessage());
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Không thể đóng socket: " + e.getMessage());
        }

        in = null;
        out = null;
        socket = null;
        eventListener = null;
        globalEventListener = null;
        listenerThread = null;
        responseQueue.clear();
    }
}
