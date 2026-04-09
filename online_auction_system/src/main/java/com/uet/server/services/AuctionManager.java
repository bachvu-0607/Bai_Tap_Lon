package com.uet.server.services;

import java.util.ArrayList;
import java.util.List;

public class AuctionManager {
    private static AuctionManager instance;
    private List<String> onlineUsers = new ArrayList<>(); // Sổ ghi tên khách

    private AuctionManager() {}
    

    //Double-Checked Locking
    // Một thằng manager duy nhất xuyên suốt
    public static AuctionManager getInstance() {
        if (instance == null){
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
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
            System.out.println("🚶 [AuctionManager] has removed: " + username + ". The number of guest using the system: " + onlineUsers.size());
        }
    }
}