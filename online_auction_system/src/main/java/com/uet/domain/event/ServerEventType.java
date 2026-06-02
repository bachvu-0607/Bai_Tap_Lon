package com.uet.domain.event;

/**
 * Liệt kê các loại sự kiện (Event Type) có thể được Server gửi chủ động (asynchronous) xuống Client.
 */
public enum ServerEventType {
    /** Báo hiệu một phiên đấu giá nào đó vừa được cập nhật (đổi trạng thái, đổi giá, có người đấu mới) */
    AUCTION_UPDATED,
    
    /** Báo hiệu danh sách hoặc số lượng người dùng đang online có sự thay đổi */
    ONLINE_USERS_UPDATED,
    
    /** Báo hiệu một người dùng vừa bị Admin khóa (ban) khỏi hệ thống */
    USER_BANNED     
}
