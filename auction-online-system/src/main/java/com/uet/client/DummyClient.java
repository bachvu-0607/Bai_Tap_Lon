package com.uet.client;

import com.uet.models.AuctionRequest;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class DummyClient {
    public static void main(String[] args) {
        System.out.println("🚀 Client đang cố gắng kết nối đến Server...");

        // Kết nối tới địa chỉ "localhost" (máy hiện tại) và cổng 8080
        try (Socket socket = new Socket("localhost", 8080)) {
            System.out.println("✅ Đã kết nối thành công với Server!");

            // 1. Mở "ống thổi" để gửi dữ liệu đi (Phải mở luồng Output TRƯỚC)
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            // 2. Gói một cái phong bì yêu cầu ĐĂNG NHẬP
            AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.LOGIN, "NguoiChoi_HeVIP");
            
            // 3. Ném phong bì lên Server
            out.writeObject(request);
            out.flush(); // Lệnh này ép dữ liệu chạy đi ngay lập tức
            System.out.println("📤 Đã gửi yêu cầu LOGIN lên Server.");

            // 4. Mở "ống hút" để chờ nghe Server phản hồi lại
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            String response = (String) in.readObject(); // Ép kiểu về String vì Server đang gửi về 1 câu chữ
            
            System.out.println("📩 Server trả lời: " + response);

        } catch (Exception e) {
            System.err.println("❌ Không thể kết nối: " + e.getMessage());
            System.err.println("💡 Gợi ý: Ông đã bật file AuctionServer.java lên chưa đấy?");
        }
    }
}