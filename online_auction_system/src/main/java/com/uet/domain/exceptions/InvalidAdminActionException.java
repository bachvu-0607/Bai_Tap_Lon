package com.uet.domain.exceptions;

import java.io.Serializable;

/**
 * Ngoại lệ xảy ra khi Quản trị viên thực hiện một thao tác không hợp lệ.
 * Ví dụ: Admin cố gắng tự khóa tài khoản của chính mình hoặc khóa một Admin khác.
 */
public class InvalidAdminActionException extends Exception implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Khởi tạo ngoại lệ với một thông điệp lỗi cụ thể.
     * 
     * @param message Chuỗi thông báo lỗi chi tiết.
     */
    public InvalidAdminActionException(String message) {
        super(message);
    }
}