package com.uet.domain.enums;

public enum AuctionStatus {
    OPEN, // phiên đấu giá đã được tạo nhưng chưa diễn ra
    RUNNING, // Đang trong thời gian đấu giá, người dùng có thể đặt giá
    FINISHED, // Hết thời gian đấu giá nhưng đang chờ xác nhận thanh toán.
    PAID, // Người thắng đã thanh toán thành công
    CANCELED // Phiên bị hủy (ví dụ: không có ai đặt giá hoặc người thắng không thanh toán).
}
