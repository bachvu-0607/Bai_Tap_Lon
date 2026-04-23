package com.uet.domain.exceptions;

/**
 * Ngoại lệ xảy ra khi người dùng không hợp lệ.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
