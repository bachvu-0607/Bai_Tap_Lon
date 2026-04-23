package com.uet.server.core;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.uet.server.utils.DatabaseConnection;

public class AuctionServer {
    private static final int PORT = 8080;

    public static List<ClientHandler> activeClients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Server đang khởi động...");

        DatabaseConnection.createTableUsers();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println(" Server đã mở tại cổng " + PORT + ". Đang chờ khách mời kết nối...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(" Khách mới kết nối: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);//thêm nv cskh
                activeClients.add(handler);//đky nv vào dsach

                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  dùng để gửi 1 thông báo đến mn trong phòng đấu giá
    // Dùng 'synchronized' để tránh việc 2 người giành mic nói đthoi
    public static synchronized void broadcast(Object message) {
        for (ClientHandler client : activeClients) {
            client.sendMessage(message); 
        }
    }

    // HỦY ĐĂNG KÝ: Xóa nv khỏ dsach khi khách rời web
    public static synchronized void removeClient(ClientHandler handler) {
        activeClients.remove(handler);
        System.out.println(" Một khách đã rời đi. Tổng số khách hiện tại: " + activeClients.size());
    }
}