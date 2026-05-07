package com.uet.server.core;

import java.net.ServerSocket;
import java.net.Socket;

import com.uet.server.services.AuctionManager;
import com.uet.server.utils.DatabaseConnection;

public class AuctionServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("⏳ Server đang khởi động...");

        DatabaseConnection.createTableUsers();
        DatabaseConnection.createAuctionTables();
        AuctionManager.getInstance().loadAuctionsFromDatabase();
        AuctionManager.getInstance().seedDemoAuctions();

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
