package com.uet.client.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.uet.client.utils.SessionManager;
import com.uet.domain.summary.AuctionSummary;
import com.uet.domain.summary.BidHistoryPoint;
import com.uet.domain.summary.UserSummary;
import com.uet.domain.event.ServerEvent;
import com.uet.domain.request.AuctionApprovalRequest;
import com.uet.domain.request.AuctionRequest;
import com.uet.domain.request.AutoBidRequest;
import com.uet.domain.request.BidRequest;
import com.uet.domain.request.ProductPostRequest;
import com.uet.domain.request.RegisterRequest;
import com.uet.domain.request.SignInRequest;
import com.uet.domain.result.AuctionActionResult;
import com.uet.domain.result.AuthenticationResult;
import com.uet.domain.result.BidResult;
import com.uet.domain.result.ProductPostResult;
import com.uet.domain.result.UserActionResult;

public class ClientSocket{
    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static final BlockingQueue<Object> responseQueue = new LinkedBlockingQueue<>();  //Thread - safe
    private static final Set<Consumer<ServerEvent>> eventListeners = Collections.synchronizedSet(new LinkedHashSet<>());
    private static Thread listenerThread;                   //Thread nhận nhiệm vụ nghe ngóng response từ server
    private static String IP_address = "localhost";


    // Hàm khi người dùng mở app
    public static void connect() throws Exception{
        if(socket == null || socket.isClosed()){
            socket = new Socket(IP_address, 8080);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            startListenerThread();
            System.out.println("Connect to server successfully!");
        }
    }

    //Hàm đảm bảo đã kết nối
    private static void ensureConnected() throws Exception {
        if (socket == null || socket.isClosed() || out == null || in == null) {
            connect();
        }
    }
    
    public static void addEventListener(Consumer<ServerEvent> listener){
        if (listener != null) {
            eventListeners.add(listener);
        }
    }

    public static void removeEventListener(Consumer<ServerEvent> listener){
        eventListeners.remove(listener);
    }

    public static void setEventListener(Consumer<ServerEvent> listener){
        addEventListener(listener);
    }

    public static void setGlobalEventListener(Consumer<ServerEvent> listener){
        addEventListener(listener);
    }

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

    private static void listenToServer(){
        try {
            while(socket != null && !socket.isClosed()){
                Object object = in.readObject();

                if(object instanceof ServerEvent event){
                    notifyEventListeners(event);
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

    private static void notifyEventListeners(ServerEvent event) {
        List<Consumer<ServerEvent>> listenersSnapshot;
        synchronized (eventListeners) {
            listenersSnapshot = new ArrayList<>(eventListeners);
        }
        listenersSnapshot.forEach(listener -> listener.accept(event));
    }

    private static Object readResponse() throws IOException, InterruptedException {
        Object response = responseQueue.poll(10, TimeUnit.SECONDS);
        if (response == null) {
            throw new IOException("Server response timeout");
        }
        return response;
    }

    //Hàm đẩy yêu cầu cho server và chờ nhận tín hiệu (mỗi lần chỉ đẩy được một yêu cầu với một client)
    private static synchronized Object sendRequestAndWait(AuctionRequest request) throws Exception {
        ensureConnected();
        out.writeObject(request);
        out.flush();
        return readResponse();
    }

    // Hàm gửi lệnh Đăng nhập
    public static AuthenticationResult sendSignIn(String username, String password) throws Exception{
        SignInRequest signInRequest = new SignInRequest(username, password);
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.SIGN_IN, signInRequest);
        return (AuthenticationResult) sendRequestAndWait(request);
    }
    
    // Hàm gửi lệnh Đăng ký
    public static AuthenticationResult sendRegister(String name, String phone, String citizenId, String password, String address, String role) throws Exception{
        RegisterRequest registerRequest = new RegisterRequest(name, phone, citizenId, password, address, role);
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.REGISTER, registerRequest);
        return (AuthenticationResult) sendRequestAndWait(request);
    }
    
    // Hàm lấy bản rút gọn của auction list từ database (cho seller và bidder nhìn qua UI)
    @SuppressWarnings("unchecked")
    public static List<AuctionSummary> getAuctionList() throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_LIST, null);
        return (List<AuctionSummary>) sendRequestAndWait(request);
    }

    public static int getOnlineUsers() throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_ONLINE_USERS, null);
        return (int) sendRequestAndWait(request);
    }

    @SuppressWarnings("unchecked")
    public static List<UserSummary> getUserList() throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_USERS, null);
        return (List<UserSummary>) sendRequestAndWait(request);
    }

    //Hàm lấy pending auction từ database (cho Admin)
    @SuppressWarnings("unchecked")
    public static List<AuctionSummary> getPendingAuctionList() throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_PENDING_AUCTIONS, null);
        return (List<AuctionSummary>) sendRequestAndWait(request);
    } 

    // Hàm lấy các sản phẩm/phiên đấu giá do seller hiện tại đã đăng.
    @SuppressWarnings("unchecked")
    public static List<AuctionSummary> getSellerProductList() throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_SELLER_PRODUCTS, null);
        return (List<AuctionSummary>) sendRequestAndWait(request);
    }

    @SuppressWarnings("unchecked")
    public static List<BidHistoryPoint> getHistoryBidList(String auctionId) throws Exception{
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_BID_HISTORY, auctionId);
        return (List<BidHistoryPoint>) sendRequestAndWait(request);
    }
    
    //Hàm gửi yêu cầu chấp nhận phiên đấu giá của Admin
    public static AuctionActionResult approveAuction(String auctionId) throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.APPROVE_AUCTION, new AuctionApprovalRequest(auctionId));
        return (AuctionActionResult) sendRequestAndWait(request);
    }
    
    //Hàm gửi yêu cầu từ chối phiên đấu giá của Admin
    public static AuctionActionResult rejectAuction(String auctionId) throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.REJECT_AUCTION, new AuctionApprovalRequest(auctionId));
        return (AuctionActionResult) sendRequestAndWait(request);
    }

    ///Hàm gửi yêu cầu đấu giá của Bidder
    public static BidResult sendBid(String auctionId, double amount) throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.BID, new BidRequest(auctionId, amount));
        return (BidResult) sendRequestAndWait(request);
    }

    public static BidResult setAutoBid(String auctionId, double maxBidLimit) throws Exception {
        AuctionRequest request = new AuctionRequest(
                AuctionRequest.RequestType.SET_AUTO_BID,
                new AutoBidRequest(auctionId, maxBidLimit));
        return (BidResult) sendRequestAndWait(request);
    }

    public static BidResult disableAutoBid(String auctionId) throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.DISABLE_AUTO_BID, auctionId);
        return (BidResult) sendRequestAndWait(request);
    }
   
    // Hàm gửi yêu cầu đăng sản phẩm đấu giá của Seller
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
    
    //Hàm gửi yêu cầu ngắt kết nối của User (sau khi đã làm xong việc và bấm cửa sổ, ngắt để Server biết tài khoản đã đăng xuất)
    public static void sendDisconnect() {
        try {
            if (SessionManager.currentUser != null && out != null) {
                String systemId = SessionManager.currentUser.getId();
                AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.DISCONNECT,systemId);
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
    public static UserActionResult banUser(String systemId) throws Exception {
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.BAN_USER, systemId);
        return (UserActionResult) sendRequestAndWait(request);
    }

    public static UserActionResult sendBanning(String systemId) throws Exception{
        return banUser(systemId);
    }

    public static void closeSessionFromServerEvent() {
        closeConnection();
        SessionManager.clearSession();
    }

    //Hàm hỗ trợ ngắt kết nối
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
        eventListeners.clear();
        listenerThread = null;
        responseQueue.clear();
    }
}
