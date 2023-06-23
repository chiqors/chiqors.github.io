package com.gii.midapp.controller;

import com.gii.midapp.base.BaseResponse;
import com.gii.midapp.exception.ValidationException;
import com.gii.midapp.model.request.partner.CreatePartnerRequest;
import com.gii.midapp.model.request.partner.UpdatePartnerRequest;
import com.gii.midapp.model.response.PartnerResponse;
import com.gii.midapp.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/partner")
public class PartnerController {
    @Autowired
    PartnerService partnerService;

    /**
     * Search partner by name
     *
     * @param name
     * @return List of partner
     */
    @GetMapping("/search")
    public ResponseEntity<BaseResponse> searchPartner(@RequestParam("name") String name) {
        BaseResponse resp = new BaseResponse();
        List<PartnerResponse> searchResults = partnerService.searchPartner(name);
        if (searchResults.isEmpty()) {
            resp.setMessage("Data tidak ditemukan");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }
        resp.setMessage("Data ditemukan");
        resp.setData(searchResults);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Create partner
     *
     * @param file          File to be uploaded
     * @param request       Partner input data
     * @return BaseResponse Partner created id
     */
    @PostMapping("/")
    public ResponseEntity<BaseResponse> createPartner(@RequestParam("file") MultipartFile file, @ModelAttribute CreatePartnerRequest request) {
        BaseResponse resp = new BaseResponse();
        try {
            Integer partnerId = partnerService.createPartner(request, file);
            resp.setMessage("Data berhasil dibuat");
            resp.setData(partnerId);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (ValidationException ve) {
            resp.setMessage(ve.getMessage());
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setMessage("Data gagal dibuat");
            return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update partner
     *
     * @param file          File to be uploaded
     * @param request       Partner input data
     * @return BaseResponse Partner updated data
     */
    @PutMapping("/")
    public ResponseEntity<BaseResponse> updatePartner(@RequestParam(value = "file", required = false) MultipartFile file,
                                                      @ModelAttribute UpdatePartnerRequest request) {
        BaseResponse resp = new BaseResponse();
        try {
            PartnerResponse partner = partnerService.updatePartner(request, file);
            resp.setMessage("Data berhasil diupdate");
            resp.setData(partner);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (ValidationException ve) {
            resp.setMessage(ve.getMessage());
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setMessage("Data gagal diupdate");
            return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete partner
     *
     * @param request       Partner input data
     * @return BaseResponse Partner deleted message
     */
    @DeleteMapping("/")
    public ResponseEntity<BaseResponse> deletePartner(@ModelAttribute UpdatePartnerRequest request) {
        BaseResponse resp = new BaseResponse();
        try {
            String message = partnerService.deletePartner(request.getId());
            resp.setMessage(message);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (ValidationException ve) {
            resp.setMessage(ve.getMessage());
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            resp.setMessage("Data gagal dihapus");
            return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
