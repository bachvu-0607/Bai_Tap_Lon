package com.uet.server.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.uet.domain.BidHistoryPoint;
import com.uet.domain.entity.auction.Auction;
import com.uet.domain.entity.auction.BidTransaction;
import com.uet.domain.entity.item.Item;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.entity.user.Seller;
import com.uet.domain.enums.AuctionStatus;
import com.uet.domain.enums.ItemStatus;
import com.uet.domain.factory.ArtFactory;
import com.uet.domain.factory.ElectronicsFactory;
import com.uet.domain.factory.ItemFactory;
import com.uet.domain.factory.VehicleFactory;
import com.uet.server.utils.DatabaseConnection;

/**
 * Lớp Repository quản lý việc lưu trữ, cập nhật và truy xuất dữ liệu liên quan đến phiên đấu giá,
 * vật phẩm (Item) và lịch sử giao dịch đặt giá (Bid) trong cơ sở dữ liệu.
 */
public class AuctionRepository {

    /**
     * Lưu thông tin một phiên đấu giá mới và vật phẩm đi kèm vào cơ sở dữ liệu.
     * Sử dụng chiến lược INSERT OR REPLACE để cập nhật nếu đã tồn tại.
     * 
     * @param auction Đối tượng phiên đấu giá cần lưu.
     * @param imageLink Đường dẫn hình ảnh của vật phẩm.
     */
    public static void saveAuction(Auction auction, String imageLink) {
        saveItem(auction.getItem(), imageLink);
        String sql = "INSERT OR REPLACE INTO auctions "
                + "(id, item_id, seller_id, start_time, end_time, current_price, min_increment, status, winner_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, auction.getId());
            pstmt.setString(2, auction.getItem().getId());
            pstmt.setString(3, auction.getSeller().getId());
            pstmt.setString(4, auction.getStartTime().toString());
            pstmt.setString(5, auction.getEndTime().toString());
            pstmt.setDouble(6, auction.getCurrentMaxPrice());
            pstmt.setDouble(7, auction.getMinIncrement());
            pstmt.setString(8, auction.getStatus().name());
            pstmt.setString(9, auction.getWinner() == null ? null : auction.getWinner().getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Save auction error: " + e.getMessage());
        }
    }

    /**
     * Tải toàn bộ danh sách phiên đấu giá hiện có trong cơ sở dữ liệu kèm theo thông tin chi tiết
     * của vật phẩm đấu giá và người bán tương ứng.
     * 
     * @return Danh sách các đối tượng {@link Auction} được khôi phục từ cơ sở dữ liệu.
     */
    public static List<Auction> loadAuctions() {
        List<Auction> auctions = new ArrayList<>();
        String sql = "SELECT a.*, i.name, i.category, i.description, i.starting_price, i.image_link, i.status AS item_status "
                + "FROM auctions a JOIN items i ON a.item_id = i.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Seller seller = UserRepository.findSellerBySystemId(rs.getString("seller_id"));
                if (seller == null) {
                    continue;
                }

                Item item = mapItem(rs);
                Auction auction = new Auction(
                        rs.getString("id"),
                        item,
                        seller,
                        LocalDateTime.parse(rs.getString("start_time")),
                        LocalDateTime.parse(rs.getString("end_time")),
                        rs.getDouble("min_increment"));
                Bidder winner = rs.getString("winner_id") == null
                        ? null
                        : UserRepository.findBidderBySystemId(rs.getString("winner_id"));
                auction.restoreState(
                        AuctionStatus.valueOf(rs.getString("status")),
                        rs.getDouble("current_price"),
                        winner);
                auctions.add(auction);
            }
        } catch (SQLException e) {
            System.out.println("Load auctions error: " + e.getMessage());
        }
        return auctions;
    }

    /**
     * Tải lịch sử đặt giá của một phiên đấu giá cụ thể theo mã phiên đấu giá.
     * 
     * @param auctionId Mã định danh phiên đấu giá.
     * @return Danh sách {@link BidHistoryPoint} biểu diễn các mốc đặt giá.
     */
    public static List<BidHistoryPoint> loadBidHistory(String auctionId){
        List<BidHistoryPoint> bidHistoryList = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY bid_time ASC";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
              
            pstmt.setString(1, auctionId);
            ResultSet rs = pstmt.executeQuery();

             while (rs.next()) {
                String bidder_name = UserRepository.findBidderBySystemId(rs.getString("bidder_id")).getName();
                double amount = rs.getDouble("amount");
                LocalDateTime bidTime = LocalDateTime.parse(rs.getString("bid_time"));
                String status = rs.getString("status");
                bidHistoryList.add(new BidHistoryPoint(bidder_name, amount, bidTime, status));
            }
        }catch(SQLException e){
            System.out.println("Find user error: " + e.getMessage());
        }
        return bidHistoryList;
    }

    /**
     * Cập nhật thông tin động của phiên đấu giá (giá hiện tại, bước giá, trạng thái, người thắng cuộc).
     * 
     * @param auction Đối tượng {@link Auction} cần cập nhật.
     */
    public static void updateAuction(Auction auction) {
        String sql = "UPDATE auctions SET current_price = ?, min_increment = ?, status = ?, winner_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, auction.getCurrentMaxPrice());
            pstmt.setDouble(2, auction.getMinIncrement());
            pstmt.setString(3, auction.getStatus().name());
            pstmt.setString(4, auction.getWinner() == null ? null : auction.getWinner().getId());
            pstmt.setString(5, auction.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Update auction error: " + e.getMessage());
        }
    }

    /**
     * Lưu một lượt đặt giá mới (Bid Transaction) vào cơ sở dữ liệu.
     * 
     * @param auctionId Mã định danh của phiên đấu giá nhận lượt đặt.
     * @param bid Đối tượng {@link BidTransaction} chứa thông tin đặt giá.
     */
    public static void saveBid(String auctionId, BidTransaction bid) {
        String sql = "INSERT OR REPLACE INTO bids (id, auction_id, bidder_id, amount, bid_time, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bid.getId());
            pstmt.setString(2, auctionId);
            pstmt.setString(3, bid.getBidder().getId());
            pstmt.setDouble(4, bid.getBidAmount());
            pstmt.setString(5, bid.getTime().toString());
            pstmt.setString(6, bid.getStatus().name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Save bid error: " + e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái của một lượt đặt giá cụ thể (ví dụ: chuyển từ WINNING sang OUTBID).
     * 
     * @param bid Đối tượng {@link BidTransaction} cần cập nhật.
     */
    public static void updateBid(BidTransaction bid) {
        String sql = "UPDATE bids SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bid.getStatus().name());
            pstmt.setString(2, bid.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Update bid error: " + e.getMessage());
        }
    }

    /**
     * Lưu thông tin vật phẩm (Item) vào cơ sở dữ liệu.
     * 
     * @param item Đối tượng vật phẩm cần lưu.
     * @param imageLink Đường dẫn hình ảnh.
     */
    private static void saveItem(Item item, String imageLink) {
        item.setImageLink(imageLink);
        String sql = "INSERT OR REPLACE INTO items (id, name, category, description, starting_price, status, image_link) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getCategory());
            pstmt.setString(4, item.getDescription());
            pstmt.setDouble(5, item.getStartingPrice());
            pstmt.setString(6, item.getStatus().name());
            pstmt.setString(7, imageLink);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Save item error: " + e.getMessage());
        }
    }

    /**
     * Chuyển đổi dữ liệu từ ResultSet SQL sang đối tượng Item tương ứng thông qua các Factory.
     * 
     * @param rs ResultSet chứa dữ liệu truy vấn từ bảng items.
     * @return Đối tượng {@link Item} đã được ánh xạ thông tin đầy đủ.
     * @throws SQLException Nếu có lỗi truy cập trường trong ResultSet.
     */
    private static Item mapItem(ResultSet rs) throws SQLException {
        String id = rs.getString("item_id");
        String name = rs.getString("name");
        double startingPrice = rs.getDouble("starting_price");
        String category = rs.getString("category");

        ItemFactory factory;

        if ("Nghệ thuật".equals(category)) factory = new ArtFactory();
        else if ("Phương tiện".equals(category)) factory = new VehicleFactory();
        else factory = new ElectronicsFactory();

        Item item = factory.createItembyId(id, name, startingPrice);
        item.setDescription(rs.getString("description"));
        item.setImageLink(rs.getString("image_link"));
        item.setStatus(ItemStatus.valueOf(rs.getString("item_status")));
        return item;
    }
}
