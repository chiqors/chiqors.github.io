package com.gii.midapp.model.request.partner;

import com.gii.midapp.model.request.partner.CreatePartnerRequest;
import lombok.Data;

@Data
public class UpdatePartnerRequest extends CreatePartnerRequest {
    private Long id;
}
