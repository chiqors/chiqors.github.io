package com.gii.midapp.model.request.partner;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CreatePartnerRequest {
    private String name;
    private String email;
    private String phone;
    private Boolean mobile;
    private String website;
}
