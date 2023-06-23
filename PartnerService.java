package com.gii.midapp.service;

import com.gii.midapp.exception.ValidationException;
import com.gii.midapp.model.request.partner.CreatePartnerRequest;
import com.gii.midapp.model.request.partner.UpdatePartnerRequest;
import com.gii.midapp.xmlrpc.OdooClientApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gii.midapp.model.response.PartnerResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static java.util.Arrays.asList;

@Service
public class PartnerService {
    @Autowired
    private OdooClientApi odooClientApi;
    private final String model = "res.partner";

    /**
     * Search partner by name
     * @param name  Partner name
     * @return List of partner
     */
    public List<PartnerResponse> searchPartner(String name) {
        List<Object> searchParams = asList(
                asList(
                        asList("name", "ilike", name)
                ),
                asList(
                        "name",
                        "phone",
                        "mobile",
                        "email",
                        "website"
                )
        );
        List<Object> resp = odooClientApi.searchAndRead(model, searchParams);
        return PartnerResponse.fromOdooResponse(resp);
    }

    /**
     * Create partner
     *
     * @param request   Partner input data
     * @param file      File to be uploaded
     * @return Partner  created id
     */
    public Integer createPartner(CreatePartnerRequest request, MultipartFile file) throws ValidationException {
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new ValidationException("Name is required");
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (request.getPhone() == null || request.getPhone().isEmpty()) {
            throw new ValidationException("Phone is required");
        }
        if (request.getMobile() == null) {
            throw new ValidationException("Mobile is required");
        }
        if (file != null) {
            if (!file.getContentType().equals("image/png") && !file.getContentType().equals("image/jpeg")) {
                throw new ValidationException("File type must be png or jpg");
            }
            if (file.getSize() > 2097152) {
                throw new ValidationException("File size must be less than 2MB");
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("name", request.getName());
        params.put("email", request.getEmail());
        params.put("phone", request.getPhone());
        params.put("mobile", request.getMobile());
        params.put("website", request.getWebsite());
        return odooClientApi.create(model, params, file);
    }

    /**
     * Update partner
     *
     * @param request   Partner input data
     * @param file      File to be uploaded
     * @return Partner  created data
     */
    public PartnerResponse updatePartner(UpdatePartnerRequest request, MultipartFile file) throws ValidationException {
        if (request.getId() == null) {
            throw new ValidationException("Id is required");
        }
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new ValidationException("Name is required");
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (request.getPhone() == null || request.getPhone().isEmpty()) {
            throw new ValidationException("Phone is required");
        }
        if (request.getMobile() == null) {
            throw new ValidationException("Mobile is required");
        }
        if (file != null) {
            if (!file.getContentType().equals("image/png") && !file.getContentType().equals("image/jpeg")) {
                throw new ValidationException("File type must be png or jpg");
            }
            if (file.getSize() > 2097152) {
                throw new ValidationException("File size must be less than 2MB");
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put("name", request.getName());
        params.put("email", request.getEmail());
        params.put("phone", request.getPhone());
        params.put("mobile", request.getMobile());
        params.put("website", request.getWebsite());
        Integer idInt = request.getId().intValue();
        Object[] resp = odooClientApi.update(model, idInt, params, file);

        Object[] innerArray = (Object[]) resp[0]; // Accessing the inner array at index 0
        String stringValue = (String) innerArray[1]; // Accessing the string value at index 1
        PartnerResponse partnerResponse = new PartnerResponse();
        partnerResponse.setName(stringValue);

        return partnerResponse;
    }

    /**
     * Delete partner
     * @param id    Partner id
     * @return message info
     */
    public String deletePartner(Long id) throws ValidationException {
        if (id == null) {
            throw new ValidationException("Id is required");
        }
        Integer idInt = id.intValue();
        return odooClientApi.delete(model, idInt);
    }
}