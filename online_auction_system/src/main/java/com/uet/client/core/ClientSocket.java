package com.uet.client.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;

import com.uet.domain.AuctionSummary;
import com.uet.domain.request.AuctionApprovalRequest;
import com.uet.domain.result.AuctionActionResult;
import com.uet.domain.result.AuthenticationResult;
import com.uet.domain.request.BidRequest;
import com.uet.domain.result.BidResult;
import com.uet.domain.request.ProductPostRequest;
import com.uet.domain.result.ProductPostResult;
import com.uet.domain.request.RegisterRequest;
import com.uet.domain.request.SignInRequest;
import com.uet.client.utils.SessionManager;
import com.uet.domain.request.AuctionRequest;

public class ClientSocket{
    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private static String IP_address = "192.168.10.186";

    // Hàm khi người dùng mở app
    public static void connect() throws Exception{
        if(socket == null || socket.isClosed()){
            socket = new Socket(IP_address, 8080);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connect to server successfully!");
        }
    }

    private static void ensureConnected() throws Exception {
        if (socket == null || socket.isClosed() || out == null || in == null) {
            connect();
        }
    }
    
    // Hàm gửi lệnh Đăng nhập
    public static AuthenticationResult sendSignIn(String username, String password) throws Exception{
        ensureConnected();
        SignInRequest signInRequest = new SignInRequest(username, password);
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.SIGN_IN, signInRequest);
        out.writeObject(request);
        out.flush();
        return (AuthenticationResult) in.readObject();
    }

    public static AuthenticationResult sendRegister(String name, String phone, String citizenId, String password, String address, String role) throws Exception{
        ensureConnected();
        RegisterRequest registerRequest = new RegisterRequest(name, phone, citizenId, password, address, role);
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.REGISTER, registerRequest);
        out.writeObject(request);
        out.flush();

        return (AuthenticationResult) in.readObject();
    }

    @SuppressWarnings("unchecked")
    public static List<AuctionSummary> getAuctionList() throws Exception {
        ensureConnected();
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_LIST, null);
        out.writeObject(request);
        out.flush();
        return (List<AuctionSummary>) in.readObject();
    }

    @SuppressWarnings("unchecked")
    public static List<AuctionSummary> getPendingAuctionList() throws Exception {
        ensureConnected();
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.GET_PENDING_AUCTIONS, null);
        out.writeObject(request);
        out.flush();
        return (List<AuctionSummary>) in.readObject();
    }

    public static AuctionActionResult approveAuction(String auctionId) throws Exception {
        ensureConnected();
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.APPROVE_AUCTION, new AuctionApprovalRequest(auctionId));
        out.writeObject(request);
        out.flush();
        return (AuctionActionResult) in.readObject();
    }

    public static AuctionActionResult rejectAuction(String auctionId) throws Exception {
        ensureConnected();
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.REJECT_AUCTION, new AuctionApprovalRequest(auctionId));
        out.writeObject(request);
        out.flush();
        return (AuctionActionResult) in.readObject();
    }

    public static BidResult sendBid(String auctionId, double amount) throws Exception {
        ensureConnected();
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.BID, new BidRequest(auctionId, amount));
        out.writeObject(request);
        out.flush();
        return (BidResult) in.readObject();
    }

    public static ProductPostResult postProduct(String productType, String productName, String description, double openingPrice,
                                                double minIncrement, LocalDateTime startTime, LocalDateTime endTime,
                                                String imageLink) throws Exception {
        ensureConnected();
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
        out.writeObject(request);
        out.flush();
        return (ProductPostResult) in.readObject();
    }

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
    }
}
