package com.uet.server.services;

import com.uet.domain.result.AuthenticationResult;
import com.uet.domain.entity.user.User;
import com.uet.server.repositories.UserRepository;
import com.uet.server.repositories.UserRepository.RegisterStatus;

public class AuthenticationService {
    private final AuctionManager auctionManager = AuctionManager.getInstance();

    public AuthenticationResult login(String username, String password) {
        User user = UserRepository.checkSignIn(username, password);
        if (user == null) {
            if (UserRepository.isBannedAccount(username, password)) {
                return AuthenticationResult.failed(AuthenticationResult.ACCOUNT_BANNED);
            }
            return AuthenticationResult.failed(AuthenticationResult.INVALID_CREDENTIALS);
        }
        return AuthenticationResult.success(user);
    }

    public AuthenticationResult register(String name, String phone, String citizenId, String password, String address, String role) {
        RegisterStatus status = UserRepository.register(name, phone, citizenId, password, address, role);
        return switch (status) {
            case SUCCESS -> AuthenticationResult.success();
            case DUPLICATE_CITIZEN_ID -> AuthenticationResult.failed(AuthenticationResult.EXISTED_CITIZEN_ID);
            case DUPLICATE_PHONE -> AuthenticationResult.failed(AuthenticationResult.EXIST_PHONE);
            case FAILED -> AuthenticationResult.failed(AuthenticationResult.SERVER_ERROR);
        };
    }

    public void logout(String username) {
        auctionManager.removeUser(username);
    }
}
