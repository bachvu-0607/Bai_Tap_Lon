package com.uet.domain.factory;

import com.uet.domain.entity.item.Electronics;
import com.uet.domain.entity.item.Item;

/**
 * Lớp Factory chuyên trách việc khởi tạo các đối tượng sản phẩm thuộc danh mục Điện tử (Electronics).
 * Giúp tạo lập chuẩn xác đối tượng mà không để lộ logic khởi tạo ra bên ngoài.
 */
public class ElectronicsFactory extends ItemFactory{
    
    /**
     * Tạo mới một thiết bị điện tử với ID sinh ngẫu nhiên.
     * 
     * @param name Tên thiết bị.
     * @param startingPrice Giá khởi điểm.
     * @return Đối tượng Electronics.
     */
    @Override
    public Item createItem(String name, double startingPrice){
        return new Electronics(name, startingPrice);
    }
    
    /**
     * Tạo một thiết bị điện tử từ dữ liệu có sẵn ID (thường là lấy từ CSDL).
     * 
     * @param id Mã định danh của thiết bị.
     * @param name Tên thiết bị.
     * @param startingPrice Giá khởi điểm.
     * @return Đối tượng Electronics.
     */
    @Override
    public Item createItembyId(String id, String name, double startingPrice){
        return new Electronics(id, name, startingPrice);
    }
}