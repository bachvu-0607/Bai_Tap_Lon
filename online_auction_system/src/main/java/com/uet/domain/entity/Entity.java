package com.uet.domain.entity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lớp trừu tượng cơ sở (Base class) cho tất cả các thực thể (Entity) trong hệ thống.
 * Cung cấp các thuộc tính chung nhất như ID định danh, thời gian tạo và thời gian cập nhật.
 * Triển khai interface Serializable để cho phép truyền tải qua mạng (Socket).
 */
public abstract class Entity implements Serializable{
    
    /**
     * Mã định danh duy nhất của thực thể.
     */
    protected String id;
    
    /**
     * Thời điểm thực thể được khởi tạo lần đầu tiên.
     */
    protected LocalDateTime createdAt;
    
    /**
     * Thời điểm thực thể được cập nhật thông tin gần nhất.
     */
    protected LocalDateTime updatedAt;

    /**
     * Khởi tạo một thực thể mới.
     * Tự động sinh ID ngẫu nhiên và gán thời gian tạo, cập nhật là thời điểm hiện tại.
     */
    public Entity() {
        this.id = generateId();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }
    
    /**
     * Khởi tạo một thực thể với ID đã biết (thường dùng khi khôi phục dữ liệu từ Database).
     * 
     * @param id Mã định danh của thực thể.
     */
    public Entity(String id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Tự động sinh mã định danh duy nhất dựa trên tên lớp và UUID.
     * 
     * @return Chuỗi ID được sinh tự động (Ví dụ: "USER-123e4567-e89b-12d3...").
     */
    private String generateId() {
        return getClass().getSimpleName().toUpperCase() + "-" + UUID.randomUUID();
    }

    /**
     * Lấy mã định danh của thực thể.
     * 
     * @return Mã định danh duy nhất (ID).
     */
    public String getId() {
        return id;
    }

    /**
     * Lấy thời điểm thực thể được khởi tạo.
     * 
     * @return Đối tượng LocalDateTime biểu diễn thời gian tạo.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Lấy thời điểm thực thể được cập nhật lần cuối.
     * 
     * @return Đối tượng LocalDateTime biểu diễn thời gian cập nhật.
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
