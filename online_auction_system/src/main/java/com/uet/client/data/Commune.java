package com.uet.client.data;

public class Commune {
    private String code;
    private String name;
    private String provinceCode;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name == null ? "" : name.replaceAll("\\s+", " ").trim();
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    @Override
    public String toString() {
        return getName();
    }
}
