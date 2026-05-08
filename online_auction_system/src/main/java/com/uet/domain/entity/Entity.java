package com.uet.domain.entity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
//Chứa các thuộc tính chung nhất cho mọi dữ liệu lưu trong hệ thống
public abstract class Entity implements Serializable{
    protected String id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;


    public Entity() {
        this.id = generateId();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }
    
    public Entity(String id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    private String generateId() {
        return getClass().getSimpleName().toUpperCase() + "-" + UUID.randomUUID();
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
