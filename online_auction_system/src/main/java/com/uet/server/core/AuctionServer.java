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
        DatabaseConnection.createWalletTransactionsTable();
        if(!UserRepository.checkCitizenIdExisted("026207002257")){
            UserRepository.register("Vu Ngoc Bach", "0974691975", "026207002257", "Bachdz123", "Vinh Phuc, Phu Tho", "Admin");
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
