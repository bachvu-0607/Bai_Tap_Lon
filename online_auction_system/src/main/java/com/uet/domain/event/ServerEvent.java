package com.uet.domain.event;
import java.io.Serializable;

/**
 * Lớp đại diện cho một sự kiện (Event) được phát sinh từ phía Server để gửi cho Client.
 * Cho phép Client biết hệ thống đã có những thay đổi bất đồng bộ gì (ví dụ: có người vừa trả giá,
 * hoặc có sự thay đổi về tổng số người dùng).
 */
public class ServerEvent implements Serializable{
    private static final long serialVersionUID = 1L;

    /** Loại sự kiện (Ví dụ: cập nhật phiên đấu giá, người dùng đăng xuất...) */
    private final ServerEventType type;
    
    /** Dữ liệu đính kèm theo sự kiện. Có thể là đối tượng phiên đấu giá, hoặc là String/Int... */
    private final Object data;

    /**
     * Khởi tạo một đối tượng sự kiện kèm theo thông điệp/dữ liệu tương ứng.
     * 
     * @param type Kiểu sự kiện.
     * @param data Dữ liệu đính kèm liên quan đến sự kiện.
     */
    public ServerEvent(ServerEventType type, Object data){
        this.type = type;
        this.data = data;
    }

    /**
     * Lấy loại sự kiện.
     * 
     * @return Kiểu sự kiện ServerEventType.
     */
    public ServerEventType getType(){
        return this.type;
    }

    /**
     * Lấy dữ liệu đi kèm sự kiện. (Client sẽ cần ép kiểu/cast về đúng định dạng).
     * 
     * @return Đối tượng dữ liệu.
     */
    public Object getData(){
        return this.data;
    }
}
