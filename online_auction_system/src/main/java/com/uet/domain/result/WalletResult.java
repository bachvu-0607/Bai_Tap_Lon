package com.uet.domain.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.uet.domain.WalletTransaction;
import com.uet.domain.contract.Payable;
import com.uet.domain.entity.user.User;

public class WalletResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private double balance;
    private double lockedBalance;
    private List<WalletTransaction> transactions;

    private WalletResult() {
        this.transactions = new ArrayList<>();
    }

    public static WalletResult success(User user, List<WalletTransaction> transactions) {
        WalletResult result = new WalletResult();
        result.success = true;
        result.message = "Thành công";
        if (user instanceof Payable p) {
            result.balance = p.getBalance();
            result.lockedBalance = p.getBalance() - p.getAvailableBalance();
        }
        result.transactions = transactions != null ? transactions : new ArrayList<>();
        return result;
    }

    public static WalletResult success(User user) {
        return success(user, new ArrayList<>());
    }

    public static WalletResult failed(String message) {
        WalletResult result = new WalletResult();
        result.success = false;
        result.message = message;
        return result;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public double getBalance() { return balance; }
    public double getLockedBalance() { return lockedBalance; }
    public double getAvailableBalance() { return balance - lockedBalance; }
    public List<WalletTransaction> getTransactions() { return transactions; }
}
