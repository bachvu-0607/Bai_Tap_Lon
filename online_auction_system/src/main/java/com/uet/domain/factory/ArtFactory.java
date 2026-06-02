package com.uet.domain.factory;

import com.uet.domain.entity.item.Art;
import com.uet.domain.entity.item.Item;

/**
 * Lớp Factory chuyên trách việc khởi tạo các đối tượng sản phẩm thuộc danh mục Nghệ thuật (Art).
 * Triển khai các phương thức được định nghĩa ở lớp cha ItemFactory.
 */
public class ArtFactory extends ItemFactory{
    
    /**
     * Khởi tạo một Tác phẩm nghệ thuật mới tinh (ID được sinh tự động).
     * 
     * @param name Tên tác phẩm.
     * @param startingPrice Giá khởi điểm.
     * @return Đối tượng Art.
     */
    @Override
    public Item createItem(String name, double startingPrice){
        return new Art(name, startingPrice);
    }

    /**
     * Khôi phục một Tác phẩm nghệ thuật từ dữ liệu đã lưu trữ (đã có sẵn ID).
     * 
     * @param id Mã định danh.
     * @param name Tên tác phẩm.
     * @param startingPrice Giá khởi điểm.
     * @return Đối tượng Art đã được thiết lập ID.
     */
    @Override
    public Item createItembyId(String id, String name, double startingPrice){
        return new Art(id, name, startingPrice);
    }

}