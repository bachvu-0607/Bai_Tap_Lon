package com.uet.server.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;

import com.uet.domain.entity.user.Admin;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.entity.user.Seller;
import com.uet.domain.entity.user.User;
import com.uet.domain.event.ServerEvent;
import com.uet.domain.request.AuctionApprovalRequest;
import com.uet.domain.request.AuctionRequest;
import com.uet.domain.request.BidRequest;
import com.uet.domain.request.ProductPostRequest;
import com.uet.domain.request.RegisterRequest;
import com.uet.domain.request.SignInRequest;
import com.uet.domain.request.AutoBidRequest;
import com.uet.domain.result.AuctionActionResult;
import com.uet.domain.result.AuthenticationResult;
import com.uet.domain.result.AutoBidResult;
import com.uet.domain.result.BidResult;
import com.uet.domain.result.ProductPostResult;
import com.uet.domain.result.WalletResult;
import com.uet.server.repositories.WalletRepository;
import com.uet.server.services.AuctionManager;
import com.uet.server.services.AuthenticationService;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private final AuthenticationService authenticationService = new AuthenticationService();
    private final AuctionManager auctionManager = AuctionManager.getInstance();
    private User currentUser;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            // Mở ống hút/thổi dữ liệu
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            auctionManager.addClient(this);
            while (true) {                 
                // Đọc yêu cầu từ Client
                AuctionRequest request = (AuctionRequest) in.readObject();
                System.out.println("📩 [Thread " + Thread.currentThread().getId() + "] Nhận lệnh: " + request.getType());
                
                //Xử lý các loại yêu cầu từ Auction Request
                switch (request.getType()) {
                    case SIGN_IN:{      
                        SignInRequest signInRequest = (SignInRequest) request.getData();

                        // Vừa check role vừa check xem tồn tại tài khoản chưa
                        AuthenticationResult result = authenticationService.login(signInRequest.getUsername(), signInRequest.getPassword());
                        if (result.isSuccess()) {
                            currentUser = result.getUser();
                        }
                        sendObject(result);
                        break;
                    }
                    case REGISTER:{  
                        RegisterRequest registerRequest = (RegisterRequest) request.getData();
                        
                        AuthenticationResult result = authenticationService.register(
                                registerRequest.getName(),
                                registerRequest.getPhone(),
                                registerRequest.getCitizenId(),
                                registerRequest.getPassword(),
                                registerRequest.getAddress(),
                                registerRequest.getRole());
                        sendObject(result);
                        break;
                    }
                    case GET_LIST:{
                        sendObject(auctionManager.getActiveAuctionSummaries());
                        break;
                    }
                    case GET_ONLINE_USERS:{
                        sendObject(auctionManager.getOnlineUsers());
                        break;
                    }
                    case GET_PENDING_AUCTIONS:{
                        if (!(currentUser instanceof Admin)) {
                            sendObject(Collections.emptyList());
                            break;
                        }
                        sendObject(auctionManager.getPendingAuctionSummaries());
                        break;
                    }
                    case GET_SELLER_PRODUCTS:{
                        if (!(currentUser instanceof Seller)) {
                            sendObject(Collections.emptyList());
                            break;
                        }
                        sendObject(auctionManager.getSellerAuctionSummaries((Seller) currentUser));
                        break;
                    }
                    case GET_BID_HISTORY:{
                        String auctionId = (String) request.getData();
                        sendObject(auctionManager.getBidListFromDatabase(auctionId));
                        break;
                    }
                    case APPROVE_AUCTION:{
                        if (!(currentUser instanceof Admin)) {
                            sendObject(AuctionActionResult.failed("Only admins can approve auctions."));
                            break;
                        }

                        AuctionApprovalRequest approvalRequest = (AuctionApprovalRequest) request.getData();
                        try {
                            auctionManager.approveAuction(approvalRequest.getAuctionId());
                            sendObject(AuctionActionResult.success("Auction approved."));
                        } catch (Exception e) {
                            sendObject(AuctionActionResult.failed(e.getMessage()));
                        }
                        break;
                    }
                    case REJECT_AUCTION:{
                        if (!(currentUser instanceof Admin)) {
                            sendObject(AuctionActionResult.failed("Only admins can reject auctions."));
                            break;
                        }

                        AuctionApprovalRequest approvalRequest = (AuctionApprovalRequest) request.getData();
                        try {
                            auctionManager.rejectAuction(approvalRequest.getAuctionId());
                            sendObject(AuctionActionResult.success("Auction rejected."));
                        } catch (Exception e) {
                            sendObject(AuctionActionResult.failed(e.getMessage()));
                        }
                        break;
                    }
                    case BID:{
                        if (!(currentUser instanceof Bidder)) {
                            sendObject(BidResult.failed("Only bidders can place bids."));
                            break;
                        }

                        BidRequest bidRequest = (BidRequest) request.getData();
                        try {
                            auctionManager.placeBid(bidRequest.getAuctionId(), (Bidder) currentUser, bidRequest.getAmount());
                            sendObject(BidResult.success("Bid placed successfully."));
                        } catch (Exception e) {
                            sendObject(BidResult.failed(e.getMessage()));
                        }
                        break;
                    }
                    case POST_PRODUCT:{
                        if (!(currentUser instanceof Seller)) {
                            sendObject(ProductPostResult.failed("Only sellers can post products."));
                            break;
                        }

                        ProductPostRequest postRequest = (ProductPostRequest) request.getData();
                        try {
                            auctionManager.postProduct(postRequest, (Seller) currentUser);
                            sendObject(ProductPostResult.success("Product posted. Waiting for admin approval."));
                        } catch (Exception e) {
                            sendObject(ProductPostResult.failed(e.getMessage()));
                        }
                        break;
                    }
                    case DEPOSIT:{
                        if (currentUser == null) {
                            sendObject(WalletResult.failed("Chưa đăng nhập!"));
                            break;
                        }
                        if (currentUser instanceof Admin) {
                            sendObject(WalletResult.failed("Admin không có ví!"));
                            break;
                        }
                        double depositAmount = (Double) request.getData();
                        try {
                            if (currentUser instanceof Bidder bidder) {
                                bidder.deposit(depositAmount);
                                WalletRepository.updateBalance(bidder.getId(), bidder.getBalance(), bidder.getLockedBalance());
                            } else if (currentUser instanceof Seller seller) {
                                seller.deposit(depositAmount);
                                WalletRepository.updateBalance(seller.getId(), seller.getBalance(), 0);
                            }
                            WalletRepository.saveTransaction(currentUser.getId(), "DEPOSIT", depositAmount, "Nạp tiền vào ví");
                            sendObject(WalletResult.success(currentUser, WalletRepository.getTransactions(currentUser.getId())));
                        } catch (Exception e) {
                            sendObject(WalletResult.failed(e.getMessage()));
                        }
                        break;
                    }
                    case GET_WALLET:{
                        if (currentUser == null) {
                            sendObject(WalletResult.failed("Chưa đăng nhập!"));
                            break;
                        }
                        if (currentUser instanceof Admin) {
                            sendObject(WalletResult.failed("Admin không có ví!"));
                            break;
                        }
                        sendObject(WalletResult.success(currentUser, WalletRepository.getTransactions(currentUser.getId())));
                        break;
                    }
                    case SET_AUTO_BID: {
                        // Chỉ Bidder mới được đặt auto-bid
                        if (!(currentUser instanceof Bidder)) {
                            sendObject(AutoBidResult.failed("Chỉ Bidder mới có thể sử dụng auto-bid!"));
                            break;
                        }
                        AutoBidRequest autoBidReq = (AutoBidRequest) request.getData();
                        AutoBidResult autoBidResult = auctionManager.setAutoBid(
                                autoBidReq.getAuctionId(),
                                (Bidder) currentUser,
                                autoBidReq.getMaxBid(),
                                autoBidReq.getIncrement());
                        sendObject(autoBidResult);
                        break;
                    }
                    case CANCEL_AUTO_BID: {
                        // Chỉ Bidder mới được huỷ auto-bid
                        if (!(currentUser instanceof Bidder)) {
                            sendObject(AutoBidResult.failed("Chỉ Bidder mới có thể sử dụng auto-bid!"));
                            break;
                        }
                        String cancelAuctionId = (String) request.getData();
                        AutoBidResult cancelResult = auctionManager.cancelAutoBid(
                                cancelAuctionId, currentUser.getId());
                        sendObject(cancelResult);
                        break;
                    }
                    case GET_AUTO_BID: {
                        // Lấy trạng thái auto-bid hiện tại của bidder cho phiên này
                        if (!(currentUser instanceof Bidder)) {
                            sendObject(AutoBidResult.failed("Chỉ Bidder mới sử dụng được tính năng này!"));
                            break;
                        }
                        String queryAuctionId = (String) request.getData();
                        AutoBidResult statusResult = auctionManager.getAutoBidStatus(
                                queryAuctionId, currentUser.getId());
                        sendObject(statusResult);
                        break;
                    }
                    case GET_USERS:{
                        if (!(currentUser instanceof Admin)) {
                            sendObject(Collections.emptyList());
                            break;
                        }
                        sendObject(auctionManager.getUserSummaries());
                        break;
                    }
                    case REMOVE_USER:{
                        if (!(currentUser instanceof Admin)) {
                            sendObject(AuctionActionResult.failed("Chỉ có Admin mới có quyền xóa người dùng."));
                            break;
                        }
                        String targetId = (String) request.getData();
                        boolean removed = auctionManager.deleteUserAccount(targetId);
                        if (removed) {
                            sendObject(AuctionActionResult.success("Xóa người dùng thành công."));
                        } else {
                            sendObject(AuctionActionResult.failed("Không tìm thấy người dùng hoặc không thể xóa."));
                        }
                        break;
                    }
                    case DISCONNECT:{
                        String username = (String) request.getData();
                        authenticationService.logout(username);
                        currentUser = null;
                        auctionManager.removeClient(this);
                        System.out.println("🔌 Client ngắt kết nối.");
                        return; // Thoát khỏi vòng lặp và kết thúc Thread này
                    }
                    default:{
                        System.out.println("⚠️ Lệnh không hợp lệ!");
                        break;
                    }
                }
                
                out.flush();
            }
            // Đóng kết nối sau khi xong việc với khách này   
        }catch (EOFException e) {
            // Lỗi này văng ra khi Client tắt app (cắt đứt kết nối)
            System.out.println("Client đã ngắt kết nối!");
        }catch (Exception e) {
            System.err.println("❌ Lỗi khi xử lý khách: " + e.getMessage());
        }finally{
            try {
                if (currentUser != null) {
                    authenticationService.logout(currentUser.getId());
                    currentUser = null;
                }
                auctionManager.removeClient(this);
                if (this.clientSocket != null && !this.clientSocket.isClosed()) {
                    this.clientSocket.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private synchronized void sendObject(Object object) throws IOException {
    if (out != null) {
        out.writeObject(object);
        out.flush();
    }
}
    
    public void sendEvent(ServerEvent event){
        try {
            if(out != null){
                sendObject(event);
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Send event error: " +  e.getMessage());
        }
    }
}
