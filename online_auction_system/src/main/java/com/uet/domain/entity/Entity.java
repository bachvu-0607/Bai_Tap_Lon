package com.uet.domain.entity;
import java.io.Serializable;
import java.time.LocalDateTime;
//Chứa các thuộc tính chung nhất cho mọi dữ liệu lưu trong hệ thống
public abstract class Entity implements Serializable{
    protected String id;
    protected LocalDateTime time;

    public Entity() {}
    
    public Entity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
