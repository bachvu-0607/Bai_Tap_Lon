package com.uet.domain.entity;
import java.util.ArrayList;

public class Admin extends User{
    //private ArrayList <Auction> waitingAuctions = new ArrayList<>();

    public Admin() {}
    public Admin(String Name, String Phone_Number,  String ID, String Password, String Address) {
        super(Name,Phone_Number, ID, Password, Address);
        System.out.println("Account created successfully!");
    }
    /*
    public void add_waitingAuctions(Auction a){this.waitingAuctions.add(a);}
    
    //Chấp nhận cấp phép mở phiên
    public void permit(Auction a) {
        a.setStatus("APPROVED");
        System.out.println("Đã cấp phép thành công!");
    }

    //Từ chối cấp phép mở phiên
    public void denie(Auction a) {
        a.setStatus("DENIED");
        System.out.println("Từ chối cấp phép!");
        
    }
    */
}