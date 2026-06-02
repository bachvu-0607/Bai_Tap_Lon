package com.uet.client.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;

/**
 * Trình tải dữ liệu địa chỉ (Address Data Loader) từ các tệp JSON tài nguyên.
 * Sử dụng thư viện Gson để chuyển đổi dữ liệu từ tệp JSON chứa thông tin các tỉnh thành và xã phường ở Việt Nam.
 */
public class AddressDataLoader {
    private final Gson gson = new Gson();

    /**
     * Tải danh sách tất cả các Tỉnh/Thành phố từ tệp JSON tài nguyên.
     * 
     * @return Danh sách {@link Province} tải được; {@link Collections#emptyList()} nếu có lỗi xảy ra.
     */
    public List<Province> getProvinces(){
        InputStream inputStream = getClass().getResourceAsStream("/com/uet/data/addressdata/provinces_2025.json");

        if(inputStream == null){
            System.out.println("Cannot find provinces_2025.json");
            return Collections.emptyList();
        }
        //Chuyển dữ liệu từ byte sang text UTF-8, vì dữ liệu có tiếng Việt.
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

        //Gson đọc JSON và đổ vào object ProvinceResponse.
        ProvinceResponse response = gson.fromJson(reader, ProvinceResponse.class);

        return (response == null || response.provinces == null) ? Collections.emptyList() : response.provinces;
    }

    /**
     * Tải danh sách tất cả các Xã/Phường từ tệp JSON tài nguyên.
     * 
     * @return Danh sách {@link Commune} tải được; {@link Collections#emptyList()} nếu có lỗi xảy ra.
     */
    public List<Commune> getCommunes() {
        InputStream inputStream = getClass().getResourceAsStream("/com/uet/data/addressdata/communes_2025.json");

        if (inputStream == null) {
            System.out.println("Cannot find communes_2025.json");
            return Collections.emptyList();
        }

        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

        CommuneResponse response = gson.fromJson(reader, CommuneResponse.class);

        if (response == null || response.communes == null) {
            return Collections.emptyList();
        }

        return response.communes;
    }

    /**
     * Lọc danh sách Xã/Phường theo mã Tỉnh/Thành phố cụ thể.
     * 
     * @param provinceCode Mã của Tỉnh/Thành phố cần lọc.
     * @return Danh sách {@link Commune} thuộc tỉnh thành đó.
     */
    public List<Commune> getCommunesByProvince(String provinceCode) {
        List<Commune> allCommunes = getCommunes();
        List<Commune> result = new ArrayList<>();

        for (Commune commune : allCommunes) {
            if (provinceCode != null && provinceCode.equals(commune.getProvinceCode())) {
                result.add(commune);
            }
        }

        return result;
    }


    private static class ProvinceResponse {
        private List<Province> provinces;
    }

    private static class CommuneResponse {
    private List<Commune> communes;
}
}
