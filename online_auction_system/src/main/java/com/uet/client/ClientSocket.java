package com.uet.client;

import com.uet.models.AuctionRequest;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

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
    public static String sendSignIn(String username, String password) throws Exception{
        String [] SignIn_pack = new String[]{username, password};
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.SIGN_IN, SignIn_pack);
        out.writeObject(request);
        out.flush();
        return (String) in.readObject();
    }

    public static String sendRegister(String name, String phone, String ID, String password, String address, String role) throws Exception{
        String [] Register_pack = new String[]{name, phone, ID, password, address, role};
        AuctionRequest request = new AuctionRequest(AuctionRequest.RequestType.REGISTER, Register_pack);
        out.writeObject(request);
        out.flush();

        return (String) in.readObject();
    }
}