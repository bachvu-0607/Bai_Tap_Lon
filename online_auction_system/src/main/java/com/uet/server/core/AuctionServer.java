package com.uet.server.core;

import java.net.ServerSocket;
import java.net.Socket;
import io.github.cdimascio.dotenv.Dotenv;

import com.uet.server.services.AuctionManager;
import com.uet.server.utils.DatabaseConnection;

public class AuctionServer {
    private static int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("⏳ Server đang khởi động...");

        // Load configuration from .env
        try {
            String envDir = new java.io.File(".env").exists() ? "." : "./online_auction_system";
            Dotenv dotenv = Dotenv.configure()
                    .directory(envDir)
                    .ignoreIfMissing()
                    .ignoreIfMalformed()
                    .load();
            String portStr = dotenv.get("SERVER_PORT");
            if (portStr != null && !portStr.isEmpty()) {
                PORT = Integer.parseInt(portStr);
            }
            System.out.println("✅ Đã lấy cổng từ file .env: " + PORT);
        } catch (Exception e) {
            System.err.println("Note: .env file not found or SERVER_PORT not set. Using default port 8080.");
        }

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
