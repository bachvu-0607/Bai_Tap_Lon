package com.uet.client.controllers;

import java.text.DecimalFormat;

import com.uet.client.utils.SessionManager;
import com.uet.domain.contract.Payable;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.entity.user.Seller;
import com.uet.domain.entity.user.User;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class WalletController {
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.##");

    @FXML
    private Label lblUserName;
    @FXML
    private Label lblRole;
    @FXML
    private Label lblTotalBalance;
    @FXML
    private Label lblAvailableBalance;
    @FXML
    private Label lblLockedBalance;
    @FXML
    private Label lblAutoBidStatus;
    @FXML
    private Label lblWalletHint;
    @FXML
    private Label lblPrimaryMetricTitle;
    @FXML
    private Label lblPrimaryMetricValue;
    @FXML
    private Label lblSecondaryMetricTitle;
    @FXML
    private Label lblSecondaryMetricValue;
    @FXML
    private Label lblActivityLine1;
    @FXML
    private Label lblActivityLine2;
    @FXML
    private Label lblActivityLine3;
    @FXML
    private ProgressBar prgAvailableBalance;

    @FXML
    private void initialize() {
        User user = SessionManager.currentUser;
        if (user == null) {
            showEmptyWallet();
            return;
        }

        lblUserName.setText(user.getName());
        lblRole.setText(user.getClass().getSimpleName());

        if (user instanceof Bidder bidder) {
            showBidderWallet(bidder);
        } else if (user instanceof Seller seller) {
            showSellerWallet(seller);
        } else if (user instanceof Payable payable) {
            showPayableWallet(payable);
        } else {
            showEmptyWallet();
        }
    }

    private void showBidderWallet(Bidder bidder) {
        double totalBalance = bidder.getBalance();
        double availableBalance = bidder.getAvailableBalance();
        double lockedBalance = bidder.getLockedBalance();

        setBalanceLabels(totalBalance, availableBalance, lockedBalance);
        lblAutoBidStatus.setText(bidder.isAutoBidEnabled()
                ? "Auto bid is on - limit " + formatMoney(bidder.getMaxBidLimit())
                : "Auto bid is off");
        lblWalletHint.setText("Locked balance is money being held while you are leading an auction.");

        lblPrimaryMetricTitle.setText("Available ratio");
        lblPrimaryMetricValue.setText(formatPercent(availableBalance, totalBalance));
        lblSecondaryMetricTitle.setText("Auto bid limit");
        lblSecondaryMetricValue.setText(bidder.isAutoBidEnabled()
                ? formatMoney(bidder.getMaxBidLimit())
                : "Off");

        lblActivityLine1.setText("Available balance can be used for new bids.");
        lblActivityLine2.setText("When you are outbid, held money is returned.");
        lblActivityLine3.setText("When payment is confirmed, held money is committed.");
        updateProgress(availableBalance, totalBalance);
    }

    private void showSellerWallet(Seller seller) {
        double balance = seller.getBalance();

        setBalanceLabels(balance, seller.getAvailableBalance(), 0);
        lblAutoBidStatus.setText("Seller payout wallet");
        lblWalletHint.setText("Revenue from paid auctions will be accumulated here.");

        lblPrimaryMetricTitle.setText("Available ratio");
        lblPrimaryMetricValue.setText(formatPercent(seller.getAvailableBalance(), Math.max(balance, 1)));
        lblSecondaryMetricTitle.setText("Payout status");
        lblSecondaryMetricValue.setText("Ready");

        lblActivityLine1.setText("Sold auction revenue will appear after payment is confirmed.");
        lblActivityLine2.setText("Pending and running auctions are not counted as revenue.");
        lblActivityLine3.setText("Withdraw/deposit flow can be added after server request support.");
        updateProgress(seller.getAvailableBalance(), Math.max(seller.getAvailableBalance(), 1));
    }

    private void showPayableWallet(Payable payable) {
        setBalanceLabels(payable.getBalance(), payable.getAvailableBalance(), 0);
        lblAutoBidStatus.setText("Wallet is available");
        lblWalletHint.setText("This account supports wallet balance tracking.");
        lblPrimaryMetricTitle.setText("Available");
        lblPrimaryMetricValue.setText(formatMoney(payable.getAvailableBalance()));
        lblSecondaryMetricTitle.setText("Locked");
        lblSecondaryMetricValue.setText(formatMoney(0));
        lblActivityLine1.setText("No wallet activity has been implemented for this account yet.");
        lblActivityLine2.setText("Add server requests later if this role needs deposits or withdrawals.");
        lblActivityLine3.setText("Current screen only displays wallet state.");
        updateProgress(payable.getAvailableBalance(), Math.max(payable.getBalance(), 1));
    }

    private void showEmptyWallet() {
        lblUserName.setText("No active session");
        lblRole.setText("Guest");
        setBalanceLabels(0, 0, 0);
        lblAutoBidStatus.setText("Wallet unavailable");
        lblWalletHint.setText("Please sign in to view wallet information.");
        lblPrimaryMetricTitle.setText("Available");
        lblPrimaryMetricValue.setText(formatMoney(0));
        lblSecondaryMetricTitle.setText("Locked");
        lblSecondaryMetricValue.setText(formatMoney(0));
        lblActivityLine1.setText("No account is currently signed in.");
        lblActivityLine2.setText("Wallet data is loaded from the current session.");
        lblActivityLine3.setText("Sign in again if the session has expired.");
        updateProgress(0, 1);
    }

    private void setBalanceLabels(double totalBalance, double availableBalance, double lockedBalance) {
        lblTotalBalance.setText(formatMoney(totalBalance));
        lblAvailableBalance.setText(formatMoney(availableBalance));
        lblLockedBalance.setText(formatMoney(lockedBalance));
    }

    private void updateProgress(double availableBalance, double totalBalance) {
        double progress = totalBalance <= 0 ? 0 : availableBalance / totalBalance;
        prgAvailableBalance.setProgress(Math.max(0, Math.min(1, progress)));
    }

    private String formatMoney(double amount) {
        return MONEY_FORMAT.format(amount) + " credits";
    }

    private String formatPercent(double current, double total) {
        if (total <= 0) {
            return "0%";
        }
        return MONEY_FORMAT.format(Math.max(0, Math.min(1, current / total)) * 100) + "%";
    }
}
