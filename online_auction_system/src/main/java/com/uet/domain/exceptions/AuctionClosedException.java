package com.uet.domain.exceptions;

import java.io.Serializable;

/**
 * Ngoại lệ xảy ra khi phiên đấu giá đã kết thúc.
 */
public class AuctionClosedException extends Exception implements Serializable{
    public AuctionClosedException(String message) {
        super(message);
    }
    
}
