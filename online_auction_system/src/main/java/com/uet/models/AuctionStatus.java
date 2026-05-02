package com.uet.models;

public enum AuctionStatus {
    OPEN,       // Vừa đăng, chưa tới giờ bắt đầu
    RUNNING,    // Đang diễn ra
    FINISHED,   // Đã kết thúc (chờ thanh toán)
    PAID,       // Đã thanh toán thành công
    CANCELED    // Bị hủy (do lỗi hoặc không ai mua)
}
