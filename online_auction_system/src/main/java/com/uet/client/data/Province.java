package com.uet.client.data;

public class Province {
    private String code;
    private String name;

    public String getCode(){
        return this.code;
    }

    public String getName(){
        return this.name == null ? "" : name.replaceAll("\\s+", " ").trim();
    }

    @Override
    public String toString(){
        return getName();
    }
}
