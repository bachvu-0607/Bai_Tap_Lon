package com.uet.server.core;

import java.net.ServerSocket;
import java.net.Socket;

import com.uet.server.repositories.UserRepository;
import com.uet.server.services.AuctionManager;
import com.uet.server.utils.DatabaseConnection;

public class AuctionServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("⏳ Server đang khởi động...");

        DatabaseConnection.createTableUsers();
        DatabaseConnection.createAuctionTables();
        if(!UserRepository.checkCitizenIdExisted("026207002257")){
            UserRepository.register("Vu Ngoc Bach", "0974691975", "026207002257", "Bachdz123", "Vinh Phuc, Phu Tho", "Admin");
        }

        // Seed Bidders
        if(!UserRepository.checkCitizenIdExisted("001234567890")){
            UserRepository.register("Nguyen Van A", "0901234567", "001234567890", "Password1", "Ha Noi", "Bidder");
        }
        if(!UserRepository.checkCitizenIdExisted("002345678901")){
            UserRepository.register("Tran Thi B", "0912345678", "002345678901", "Password1", "Ho Chi Minh", "Bidder");
      }
        if(!UserRepository.checkCitizenIdExisted("001234567890")){
            UserRepository.register("Dung Dam", "0999999999", "001234567890", "Password1", "Ho Chi Minh", "Bidder");
        }
        // Seed Sellers
        if(!UserRepository.checkCitizenIdExisted("003456789012")){
            UserRepository.register("Le Van C", "0923456789", "003456789012", "Password1", "Da Nang", "Seller");
        }
        if(!UserRepository.checkCitizenIdExisted("004567890123")){
            UserRepository.register("Pham Thi D", "0934567890", "004567890123", "Password1", "Can Tho", "Seller");
        }
        AuctionManager.getInstance().loadAuctionsFromDatabase();
        AuctionManager.getInstance().seedDemoAuctions();
        AuctionManager.getInstance().startStatusScheduler();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ Server đã mở tại cổng " + PORT + ". Đang chờ người chơi kết nối...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🎉 Khách mới kết nối: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
