package com.uet.models;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Seller extends User{
    private ArrayList<Item> Sold_Products = new ArrayList<>();
    private double Balance;

    public Seller() {}
    public Seller(String Name, String Phone_Number,  String ID, String Password, String Address) {
        super(Name,Phone_Number, ID, Password, Address);
        this.Balance = 0;
        System.out.println("Account created successfully!");
    }
    public Seller(Seller other){
        this(other.Name, other.Phone_Number, other.id, other.Password, other.Address);
    }

    /*
    public double getBalance(){return this.Balance;}
    public ArrayList<Product> getSold_Products(){
        return new ArrayList<>(this.Sold_Products);
    }

    // Nạp tiền
    void deposit(Auction a){
        this.Balance += a.getBest_Bid_Amount();
    }

    public void add(String Name, double Price){
        Product x = new Product(Name, Price);
        Sold_Products.add(x);
        System.out.println("product added successfully!");
        //apply_for_a_permit(admin, x, startTime, endTime, extension);
    }

    public void update(int x, String Name, double Price){
        if(x > this.Sold_Products.size()){
            System.out.println("Product" + x + "th not found");
            return;
        }
        this.Sold_Products.set(x - 1, new Product(Name, Price));
        System.out.println("Product updated successfully!");
    }

    public void remove(int x){
        this.Sold_Products.remove(x - 1);
    }
    public void remove(String Name){
        for(int i = 0; i < this.Sold_Products.size(); i++){
            if(this.Sold_Products.get(i).getName().equals(Name)){
                remove(i + 1);
                return;
            }
        }
        System.out.println("Product not found");
    }
    
    //Chờ admin cấp phép mở phiên
    public void apply_for_a_permit(Product product, LocalDateTime startTime, LocalDateTime endTime, boolean extension){
        Auction a = new Auction(this, product, startTime, endTime, extension);
        //admin.add_waitingAuctions(a);
    }
    */
}