package com.uet.server;

import com.uet.models.AuctionRequest;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            // Mở ống hút/thổi dữ liệu
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            // Đọc yêu cầu từ Client
            AuctionRequest request = (AuctionRequest) in.readObject();
            System.out.println("📩 [Thread " + Thread.currentThread().getId() + "] Nhận lệnh: " + request.getType());

            // Trả lời Client
            out.writeObject("Chào bạn, Server đã nhận lệnh " + request.getType());
            out.flush();

            // Đóng kết nối sau khi xong việc với khách này
            clientSocket.close();
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xử lý khách: " + e.getMessage());
        }
    }
}