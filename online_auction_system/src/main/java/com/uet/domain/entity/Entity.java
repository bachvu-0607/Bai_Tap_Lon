package com.uet.domain.entity;
import java.io.Serializable;
//Chứa các thuộc tính chung nhất cho mọi dữ liệu lưu trong hệ thống
public abstract class Entity implements Serializable{
    protected String id;

    public Entity() {}
    
    public Entity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
