package com.uet.domain.enums;

/**
 * Liệt kê các trạng thái có thể có của một lượt đặt giá (BidTransaction).
 */
public enum BidStatus {
    /** Lượt đặt giá đang là mức giá cao nhất, tạm thời dẫn đầu phiên */
    WINNING,
    
    /** Lượt đặt giá đã bị một lượt đặt giá khác cao hơn vượt qua */
    OUTBID,
    
    /** Lượt đặt giá đã chiến thắng chung cuộc và đã được thanh toán thành công */
    PAID,
    
    /** Lượt đặt giá bị hủy bỏ vì lý do kỹ thuật hoặc do phiên bị hủy */
    CANCELED
}
