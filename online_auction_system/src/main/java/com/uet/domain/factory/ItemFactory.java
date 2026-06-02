package com.uet.domain.factory;

import com.uet.domain.entity.item.Item;

/**
 * Lớp trừu tượng (Abstract Factory) quy định cách thức để tạo ra các loại Vật phẩm (Item).
 * Áp dụng mẫu thiết kế Factory Method giúp ẩn đi logic khởi tạo đối tượng phức tạp.
 */
public abstract class ItemFactory {
    
    /**
     * Tạo ra một vật phẩm mới hoàn toàn (thường được gọi khi Seller tạo sản phẩm mới).
     * ID của vật phẩm sẽ tự động được sinh ngẫu nhiên.
     * 
     * @param name Tên sản phẩm.
     * @param startingPrice Giá khởi điểm.
     * @return Một đối tượng kế thừa từ lớp Item (Ví dụ: Art, Electronics).
     */
    public abstract Item createItem(String name, double startingPrice);
    
    /**
     * Tạo lại một vật phẩm bằng dữ liệu cũ (đã có ID), thường dùng để khôi phục từ Database.
     * 
     * @param id Mã định danh có sẵn.
     * @param name Tên sản phẩm.
     * @param startingPrice Giá khởi điểm.
     * @return Một đối tượng kế thừa từ lớp Item tương ứng.
     */
    public abstract Item createItembyId(String id, String name, double startingPrice);
}