package com.gii.midapp.model.response;

import com.gii.midapp.base.BaseOdooResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class LocationResponse extends BaseOdooResponse {
    private Long companyId;
    private String companyName;
    private List<DivisionResponse> divisions;

    public LocationResponse(Long id, String name, Date createdAt, Date updatedAt, Long companyId, String companyName, List<DivisionResponse> divisions) {
        super(id, name, createdAt, updatedAt);
        this.companyId = companyId;
        this.companyName = companyName;
        this.divisions = divisions;
    }

    public static LocationResponse fromOdooResponseMapper(List<Object> odooResponse, List<Object> odooResponseDivisions) {
        LocationResponse locationResponse = null;
        for (Object obj : odooResponse) {
            Map<String, Object> map = (Map<String, Object>) obj;

            Long companyId = null;
            String companyName = null;
            // Need Boolean check because sometimes the company_id is false from the response
            if (!(map.get("company_id") instanceof Boolean)) {
                Object[] companyIdObj = (Object[]) map.get("company_id");
                companyId = Long.parseLong(companyIdObj[0].toString());
                companyName = companyIdObj[1].toString();
            }

            List<DivisionResponse> divisions = new ArrayList<>();
            for (Object objDivision : odooResponseDivisions) {
                Map<String, Object> mapDivision = (Map<String, Object>) objDivision;

                DivisionResponse divisionResponse = new DivisionResponse(
                        Long.parseLong(mapDivision.get("id").toString()),
                        mapDivision.get("name").toString(),
                        stringToDate(mapDivision.get("create_date").toString()),
                        stringToDate(mapDivision.get("write_date").toString())
                );
                divisions.add(divisionResponse);
            }

            locationResponse = new LocationResponse(
                    Long.parseLong(map.get("id").toString()),
                    map.get("name").toString(),
                    stringToDate(map.get("create_date").toString()),
                    stringToDate(map.get("write_date").toString()),
                    companyId,
                    companyName,
                    divisions
            );
        }
        return locationResponse;
    }
}
