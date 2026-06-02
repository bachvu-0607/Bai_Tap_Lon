package com.uet.domain.enums;

/**
 * Liệt kê các trạng thái vòng đời của một phiên đấu giá.
 */
public enum AuctionStatus {
    /** Người bán vừa tạo xong phiên, đang chờ hệ thống (hoặc Admin) phê duyệt */
    PENDING_APPROVAL, 
    
    /** Hệ thống hoặc Admin đã từ chối duyệt phiên đấu giá này */
    REJECTED, 
    
    /** Phiên đấu giá đã được phê duyệt và lên lịch nhưng thời gian bắt đầu vẫn ở tương lai */
    OPEN, 
    
    /** Đang trong thời gian đấu giá chính thức, hệ thống mở cửa cho phép người dùng đặt giá */
    RUNNING, 
    
    /** Thời gian đấu giá đã kết thúc, đang chờ người chiến thắng thanh toán */
    FINISHED, 
    
    /** Người thắng đã hoàn tất việc thanh toán, giao dịch thành công */
    PAID, 
    
    /** Phiên bị hủy bỏ giữa chừng hoặc kết thúc thất bại (do không có ai mua, hoặc người thắng bùng tiền) */
    CANCELED 
}
