package com.uet.client.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;

public class AddressDataLoader {
    private final Gson gson = new Gson();

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
