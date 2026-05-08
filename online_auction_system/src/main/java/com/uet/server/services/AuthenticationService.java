package com.uet.server.services;

import com.uet.domain.result.AuthenticationResult;
import com.uet.domain.entity.user.User;
import com.uet.server.repositories.UserRepository;

public class AuthenticationService {
    private final AuctionManager auctionManager = AuctionManager.getInstance();

    public AuthenticationResult login(String username, String password) {
        User user = UserRepository.checkSignIn(username, password);
        if (user == null) {
            return AuthenticationResult.failed(AuthenticationResult.INVALID_CREDENTIALS);
        }

        boolean canLogin = auctionManager.SignIn(user.getId());
        if (!canLogin) {
            return AuthenticationResult.failed(AuthenticationResult.ALREADY_LOGGED_IN);
        }

        return AuthenticationResult.success(user);
    }

    public AuthenticationResult register(String name, String phone, String citizenId, String password, String address, String role) {
        if (UserRepository.checkCitizenIdExisted(citizenId)) {
            return AuthenticationResult.failed(AuthenticationResult.EXISTED_CITIZEN_ID);
        }

        if (UserRepository.check_phone_existed(phone)) {
            return AuthenticationResult.failed(AuthenticationResult.EXIST_PHONE);
        }

        boolean success = UserRepository.register(name, phone, citizenId, password, address, role);
        return success ? AuthenticationResult.success() : AuthenticationResult.failed(AuthenticationResult.SERVER_ERROR);
    }

    public void logout(String username) {
        auctionManager.removeUser(username);
    }
}
