package com.uet.server.core;

import java.io.EOFException;

import com.uet.models.AuctionRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.uet.server.repositories.UserRepository;

public class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            // Mở ống hút/thổi dữ liệu
            ObjectOutputStream out = new ObjectOutputStream(this.clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(this.clientSocket.getInputStream());

            while (true) {                 
                // Đọc yêu cầu từ Client
                AuctionRequest request = (AuctionRequest) in.readObject();
                System.out.println("📩 [Thread " + Thread.currentThread().getId() + "] Nhận lệnh: " + request.getType());

                //Nếu yêu cầu là sign in
                if(request.getType() == AuctionRequest.RequestType.SIGN_IN){
                    String[] credentials = (String[]) request.getData();
                    String username = credentials[0];
                    String password = credentials[1];

                    // Vừa check role vừa check xem tồn tại tài khoản chưa
                    String role = UserRepository.checkSignIn(username, password);
                    out.writeObject(role);
                    out.flush();
                }

                //Nếu yêu cầu là register
                if(request.getType() == AuctionRequest.RequestType.REGISTER){
                    String[] credentials = (String[]) request.getData();
                    String name = credentials[0];
                    String phone = credentials[1];
                    String ID = credentials[2];
                    String password = credentials[3];
                    String address = credentials[4];
                    String role = credentials[5];
                    
                    boolean checkID = UserRepository.check_ID_existed(ID);
                    boolean check_phone = UserRepository.check_phone_existed(phone);
                    if(checkID){
                        out.writeObject("EXISTED_ID");
                        out.flush();
                    }else if(check_phone){
                        out.writeObject("EXIST_PHONE");
                        out.flush();
                    }else{
                        boolean success = UserRepository.register(name, phone, ID, password, address, role);
                        if (success) {
                            out.writeObject("SUCCESS"); 
                        } else {
                            // Lỗi hệ thống/database (ví dụ: DB sập, sai câu lệnh SQL...)
                            out.writeObject("SERVER_ERROR");
                        }
                        out.flush();
                    }
                }            
                
            }
            // Đóng kết nối sau khi xong việc với khách này   
        }catch (EOFException e) {
            // Lỗi này văng ra khi Client tắt app (cắt đứt kết nối)
            System.out.println("Client đã ngắt kết nối!");
        }catch (Exception e) {
            System.err.println("❌ Lỗi khi xử lý khách: " + e.getMessage());
        }finally{
            try {
                if (this.clientSocket != null && !this.clientSocket.isClosed()) {
                    this.clientSocket.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}