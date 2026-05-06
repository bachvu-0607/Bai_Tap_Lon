package com.uet.server.core;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;

import com.uet.domain.entity.user.Admin;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.entity.user.Seller;
import com.uet.domain.entity.user.User;
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
import com.uet.server.services.AuctionManager;
import com.uet.server.services.AuthenticationService;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private final AuthenticationService authenticationService = new AuthenticationService();
    private final AuctionManager auctionManager = AuctionManager.getInstance();
    private User currentUser;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            // Mở ống hút/thổi dữ liệu
            ObjectOutputStream out = new ObjectOutputStream(this.clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(this.clientSocket.getInputStream());

            while (true) {                 
                // Đọc yêu cầu từ Client
                AuctionRequest request = (AuctionRequest) in.readObject();
                System.out.println("📩 [Thread " + Thread.currentThread().getId() + "] Nhận lệnh: " + request.getType());

                switch (request.getType()) {
                    case SIGN_IN:{      //Nếu yêu cầu là sign in
                        SignInRequest signInRequest = (SignInRequest) request.getData();

                        // Vừa check role vừa check xem tồn tại tài khoản chưa
                        AuthenticationResult result = authenticationService.login(signInRequest.getUsername(), signInRequest.getPassword());
                        if (result.isSuccess()) {
                            currentUser = result.getUser();
                        }
                        out.writeObject(result);
                        break;
                    }
                    case REGISTER:{   //Nếu yêu cầu là register
                        RegisterRequest registerRequest = (RegisterRequest) request.getData();
                        
                        AuthenticationResult result = authenticationService.register(
                                registerRequest.getName(),
                                registerRequest.getPhone(),
                                registerRequest.getCitizenId(),
                                registerRequest.getPassword(),
                                registerRequest.getAddress(),
                                registerRequest.getRole());
                        out.writeObject(result);
                        break;
                    }
                    case GET_LIST:{
                        out.writeObject(auctionManager.getActiveAuctionSummaries());
                        break;
                    }
                    case GET_PENDING_AUCTIONS:{
                        if (!(currentUser instanceof Admin)) {
                            out.writeObject(Collections.emptyList());
                            break;
                        }
                        out.writeObject(auctionManager.getPendingAuctionSummaries());
                        break;
                    }
                    case APPROVE_AUCTION:{
                        if (!(currentUser instanceof Admin)) {
                            out.writeObject(AuctionActionResult.failed("Only admins can approve auctions."));
                            break;
                        }

                        AuctionApprovalRequest approvalRequest = (AuctionApprovalRequest) request.getData();
                        try {
                            auctionManager.approveAuction(approvalRequest.getAuctionId());
                            out.writeObject(AuctionActionResult.success("Auction approved."));
                        } catch (Exception e) {
                            out.writeObject(AuctionActionResult.failed(e.getMessage()));
                        }
                        break;
                    }
                    case REJECT_AUCTION:{
                        if (!(currentUser instanceof Admin)) {
                            out.writeObject(AuctionActionResult.failed("Only admins can reject auctions."));
                            break;
                        }

                        AuctionApprovalRequest approvalRequest = (AuctionApprovalRequest) request.getData();
                        try {
                            auctionManager.rejectAuction(approvalRequest.getAuctionId());
                            out.writeObject(AuctionActionResult.success("Auction rejected."));
                        } catch (Exception e) {
                            out.writeObject(AuctionActionResult.failed(e.getMessage()));
                        }
                        break;
                    }
                    case BID:{
                        if (!(currentUser instanceof Bidder)) {
                            out.writeObject(BidResult.failed("Only bidders can place bids."));
                            break;
                        }

                        BidRequest bidRequest = (BidRequest) request.getData();
                        try {
                            auctionManager.placeBid(bidRequest.getAuctionId(), (Bidder) currentUser, bidRequest.getAmount());
                            out.writeObject(BidResult.success("Bid placed successfully."));
                        } catch (Exception e) {
                            out.writeObject(BidResult.failed(e.getMessage()));
                        }
                        break;
                    }
                    case POST_PRODUCT:{
                        if (!(currentUser instanceof Seller)) {
                            out.writeObject(ProductPostResult.failed("Only sellers can post products."));
                            break;
                        }

                        ProductPostRequest postRequest = (ProductPostRequest) request.getData();
                        try {
                            auctionManager.postProduct(postRequest, (Seller) currentUser);
                            out.writeObject(ProductPostResult.success("Product posted. Auction is now visible to bidders."));
                        } catch (Exception e) {
                            out.writeObject(ProductPostResult.failed(e.getMessage()));
                        }
                        break;
                    }
                    case DISCONNECT:{
                        String username = (String) request.getData();
                        authenticationService.logout(username);
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
                if (this.clientSocket != null && !this.clientSocket.isClosed()) {
                    this.clientSocket.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
