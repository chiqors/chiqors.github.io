package com.gii.midapp.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerResponse {
    private String name;
    private String phone;
    private Boolean mobile;
    private String email;
    private String website;

    public static List<PartnerResponse> fromOdooResponse(List<Object> resp) {
        List<PartnerResponse> result = new ArrayList<>();
        for (Object obj : resp) {
            HashMap<String, Object> map = (HashMap<String, Object>) obj;
            PartnerResponse partnerResponse = new PartnerResponse(
                    map.get("name").toString(),
                    map.get("phone").toString(),
                    map.get("mobile").toString().equals("true"),
                    map.get("email").toString(),
                    map.get("website").toString()
            );
            result.add(partnerResponse);
        }
        return result;
    }

    @Override
    public String toString() {
        return "SearchPartnerResponse{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", mobile=" + mobile +
                ", email='" + email + '\'' +
                ", website='" + website + '\'' +
                '}';
    }
}
