package com.uet.client.core;

import java.io.IOException;

import com.uet.models.AuctionRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.uet.client.utils.SessionManager;

public class ClientSocket{
    private static Socket socket;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    // Hàm khi người dùng mở app
    public static void connect() throws Exception{
        if(socket == null || socket.isClosed()){
            socket = new Socket("localhost", 8080);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connect to server successfully!");
        }
    }
    
    // Hàm gửi lệnh Đăng nhập
    public static Object sendSignIn(String username, String password) throws Exception{
        String [] SignIn_pack = new String[]{username, password};
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.SIGN_IN, SignIn_pack);
        out.writeObject(request);
        out.flush();
        return in.readObject();
    }

    public static String sendRegister(String name, String phone, String ID, String password, String address, String role) throws Exception{
        String [] Register_pack = new String[]{name, phone, ID, password, address, role};
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.REGISTER, Register_pack);
        out.writeObject(request);
        out.flush();

        return (String) in.readObject();
    }

    public static void sendDisconnect() {
        try {
            if (SessionManager.currentUser != null && out != null) {
                String username = SessionManager.currentUser.getID();
                AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.DISCONNECT,username);
                out.writeObject(request);
                out.flush();
                System.out.println(username + "  disconnected from server");
                SessionManager.clearSession();
            }
        } catch (IOException e) {
            System.err.println("Can not send a disconnected require: " + e.getMessage());
        }
    }
}