package com.uet.domain.enums;

/**
 * Liệt kê các trạng thái vòng đời của một sản phẩm/vật phẩm trong hệ thống.
 */
public enum ItemStatus {
    /** Sản phẩm vừa được đăng tải, sẵn sàng để gán vào một phiên đấu giá */
    AVAILABLE,
    
    /** Sản phẩm đang nằm trong một phiên đấu giá hiện hành */
    IN_AUCTION,
    
    /** Sản phẩm đã được bán thành công và đã hoàn tất thanh toán */
    SOLD,
    
    /** Sản phẩm đã bị xóa hoặc gỡ bỏ khỏi hệ thống do vi phạm hoặc người bán rút lại */
    REMOVED
}
