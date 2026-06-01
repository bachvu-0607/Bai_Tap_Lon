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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uet.server.repositories.WalletRepository;
import com.uet.server.services.AuctionManager;
import com.uet.server.services.AuthenticationService;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

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
                logger.debug("Nhận lệnh [thread={}]: {}", Thread.currentThread().getId(), request.getType());
                
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
                        // ── Đặt / cập nhật Auto Bid ──────────────────────────────────
                        // Kiểm tra quyền: chỉ Bidder mới được sử dụng tính năng này
                        // (Seller và Admin không tham gia đấu giá)
                        if (!(currentUser instanceof Bidder)) {
                            sendObject(AutoBidResult.failed("Chỉ Bidder mới có thể sử dụng auto-bid!"));
                            break;
                        }
                        // Giải mã dữ liệu: lấy AutoBidRequest từ gói tin
                        // (chứa auctionId, maxBid, increment do Bidder nhập trên UI)
                        AutoBidRequest autoBidReq = (AutoBidRequest) request.getData();

                        // Chuyển sang AuctionManager để xử lý logic nghiệp vụ:
                        //   · Kiểm tra điều kiện (phiên đang chạy, số dư đủ, maxBid hợp lệ)
                        //   · Lưu vào PriorityQueue
                        //   · Kích hoạt triggerAutoBids() ngay lập tức
                        AutoBidResult autoBidResult = auctionManager.setAutoBid(
                                autoBidReq.getAuctionId(),
                                (Bidder) currentUser,     // Cast an toàn vì đã kiểm tra instanceof
                                autoBidReq.getMaxBid(),
                                autoBidReq.getIncrement());

                        // Gửi kết quả về client (success/failed + thông tin auto-bid hiện tại)
                        sendObject(autoBidResult);
                        break;
                    }
                    case CANCEL_AUTO_BID: {
                        // ── Huỷ Auto Bid đang chạy ───────────────────────────────────
                        // Kiểm tra quyền: chỉ Bidder mới được dùng
                        if (!(currentUser instanceof Bidder)) {
                            sendObject(AutoBidResult.failed("Chỉ Bidder mới có thể sử dụng auto-bid!"));
                            break;
                        }
                        // Dữ liệu gửi lên là auctionId (String) — phiên cần huỷ auto-bid
                        String cancelAuctionId = (String) request.getData();

                        // AuctionManager xoá entry của bidder này khỏi PriorityQueue của phiên
                        // Lưu ý: tiền đã tạm giữ KHÔNG bị hoàn lại ở đây
                        // (tiền sẽ được hoàn khi có người đặt giá cao hơn)
                        AutoBidResult cancelResult = auctionManager.cancelAutoBid(
                                cancelAuctionId, currentUser.getId());
                        sendObject(cancelResult);
                        break;
                    }
                    case GET_AUTO_BID: {
                        // ── Lấy trạng thái Auto Bid ──────────────────────────────────
                        // Dùng khi UI mở popup AutoBid: cần biết bidder này có
                        // đang auto-bid cho phiên này không, và nếu có thì với cấu hình gì
                        if (!(currentUser instanceof Bidder)) {
                            sendObject(AutoBidResult.failed("Chỉ Bidder mới sử dụng được tính năng này!"));
                            break;
                        }
                        // Dữ liệu gửi lên là auctionId cần kiểm tra
                        String queryAuctionId = (String) request.getData();

                        // Tra cứu trong autoBidMap: có entry nào của bidder này không?
                        // Trả về AutoBidResult với hasActiveBid=true/false, kèm maxBid và increment nếu có
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
                        logger.info("Client ngắt kết nối bình thường: {}", username);
                        return; // Thoát khỏi vòng lặp và kết thúc Thread này
                    }
                    default:{
                        logger.warn("Nhận được lệnh không hợp lệ từ client: {}", request.getType());
                        break;
                    }
                }
                
                out.flush();
            }
            // Đóng kết nối sau khi xong việc với khách này   
        } catch (EOFException e) {
            // EOFException xảy ra khi client tắt app đột ngột (không gửi DISCONNECT)
            logger.info("Client ngắt kết nối đột ngột (EOF).");
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý yêu cầu từ client", e);
        } finally {
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
                logger.error("Lỗi khi đóng kết nối client", ex);
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
            logger.error("Lỗi khi gửi sự kiện tới client: {}", e.getMessage());
        }
    }
}
