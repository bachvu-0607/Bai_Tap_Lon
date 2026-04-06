package com.uet.server.services;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.SysexMessage;

public class AuctionManager {
    private static AuctionManager instance;
    private List<String> onlineUsers = new ArrayList<>(); // Sổ ghi tên khách

    private AuctionManager() {}
    
    // Một thằng manager duy nhất xuyên suốt
    public static synchronized AuctionManager getInstance() {
        if (instance == null) instance = new AuctionManager();
        return instance;
    }

    // Logic kiểm tra đăng nhập cùng một tên đăng nhập nhưng có hai máy
    public synchronized boolean SignIn(String username) {
        if (onlineUsers.contains(username)) {
            return false; 
        }
        onlineUsers.add(username); 
        return true;
    }
    
    //SignOut Disconnect
    public synchronized void removeUser(String username) {
        if (username != null) {
            onlineUsers.remove(username);
            System.out.println("🚶 [AuctionManager] Đã gạch tên: " + username + ". Số khách hiện tại: " + onlineUsers.size());
        }
    }
}