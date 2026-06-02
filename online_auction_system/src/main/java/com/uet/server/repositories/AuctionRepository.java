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

public class AuctionRepository {
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

    public static void markWinningBidsOutbid(String auctionId) {
        String sql = "UPDATE bids SET status = ? WHERE auction_id = ? AND status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "OUTBID");
            pstmt.setString(2, auctionId);
            pstmt.setString(3, "WINNING");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Update previous winning bids error: " + e.getMessage());
        }
    }

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
