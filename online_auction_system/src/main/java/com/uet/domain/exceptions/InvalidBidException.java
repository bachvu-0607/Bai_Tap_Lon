package com.uet.domain.exceptions;

/**
 * Ngoại lệ xảy ra khi người dùng đặt mức giá không hợp lệ.
 * Được ném ra trong các trường hợp: 
 * - Mức giá đặt thấp hơn giá hiện tại
 * - Mức giá đặt thấp hơn mức giá tối thiểu
 */
public class InvalidBidException extends Exception {
    public InvalidBidException(String message) {
        super(message);
    }
}

