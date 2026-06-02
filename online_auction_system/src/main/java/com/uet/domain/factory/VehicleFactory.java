package com.uet.domain.factory;

import com.uet.domain.entity.item.Item;
import com.uet.domain.entity.item.Vehicle;

/**
 * Lớp Factory chịu trách nhiệm khởi tạo các đối tượng sản phẩm thuộc nhóm Phương tiện giao thông.
 */
public class VehicleFactory extends ItemFactory{
    
    /**
     * Tạo mới một phương tiện với ID sinh tự động ngẫu nhiên.
     * 
     * @param name Tên phương tiện.
     * @param startingPrice Giá khởi điểm.
     * @return Đối tượng Vehicle.
     */
    @Override
    public Item createItem(String name, double startingPrice){
        return new Vehicle(name, startingPrice);
    }
    
    /**
     * Khôi phục hoặc tạo phương tiện với ID có sẵn (từ Database).
     * 
     * @param id Mã định danh đã biết.
     * @param name Tên phương tiện.
     * @param startingPrice Giá khởi điểm.
     * @return Đối tượng Vehicle tương ứng.
     */
    @Override
    public Item createItembyId(String id, String name, double startingPrice){
        return new Vehicle(id, name, startingPrice);
    }

}