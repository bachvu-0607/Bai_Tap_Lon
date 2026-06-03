package com.uet.server.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.uet.server.repositories.UserRepository;
import com.uet.server.services.AuctionManager;
import com.uet.server.utils.DatabaseConnection;

public class AuctionServer {
    private static final int PORT = 8080;
    private static final Logger LOGGER = Logger.getLogger("AuctionServer");

    public static void main(String[] args) {
        System.out.println("⏳ Server đang khởi động...");

        DatabaseConnection.createTableUsers();
        DatabaseConnection.createAuctionTables();
        
        try {
            setupLogging();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.severe("Cannot open logging file");
        }

        if(!UserRepository.checkCitizenIdExisted("026207002258")){
            UserRepository.register("ADMIN", "0974691980", "026207002258", "Bach123", "Vinh Phuc, Phu Tho", "Admin");
        }

        if(!UserRepository.checkCitizenIdExisted("026207002259")){
            UserRepository.register("Seller1", "0974691985", "026207002259", "Bach123", "Vinh Phuc, Phu Tho", "Seller");
        }

        if(!UserRepository.checkCitizenIdExisted("026207002260")){
            UserRepository.register("Bidder1", "0974691990", "026207002260", "Bach123", "Vinh Phuc, Phu Tho", "Bidder");
        }

        if(!UserRepository.checkCitizenIdExisted("026207002261")){
            UserRepository.register("Bidder2", "0974691995", "026207002261", "Bach123", "Vinh Phuc, Phu Tho", "Bidder");
        }
        AuctionManager.getInstance().loadAuctionsFromDatabase();
        AuctionManager.getInstance().seedDemoAuctions();
        AuctionManager.getInstance().startStatusScheduler();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ Server started at " + PORT + ". Waitting for users...");
            LOGGER.info("Server started at " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🎉 New client at: " + clientSocket.getInetAddress());
                LOGGER.info("New client at " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.severe("Cannot started server");
        }
    }

    private static void setupLogging() throws IOException{
        Files.createDirectories(Path.of("logs"));
        Logger rootLogger = Logger.getLogger("");

        FileHandler fileHandler = new FileHandler("logs/auction-system.log", true);
        fileHandler.setFormatter(new SimpleFormatter());

        rootLogger.addHandler(fileHandler);
    }
}
