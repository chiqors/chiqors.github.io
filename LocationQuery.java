package com.gii.midapp.xmlrpc.query;

import com.gii.midapp.model.response.DivisionResponse;
import com.gii.midapp.model.response.LocationListResponse;
import com.gii.midapp.model.response.LocationResponse;
import com.gii.midapp.xmlrpc.OdooClientApi;
import com.gii.midapp.xmlrpc.OdooCondition;
import com.gii.midapp.xmlrpc.OdooConfig;
import com.gii.midapp.xmlrpc.OdooPagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class LocationQuery {
    @Autowired
    private OdooClientApi odooClientApi;

    public Page<LocationListResponse> getPagedLocations(String search, Integer page, Integer limit, String orderBy, Boolean asc) {
        Sort sortBy = asc ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending();
        Pageable pageableRequest = PageRequest.of(page, limit, sortBy);

        // Create the conditions
        List<OdooCondition> conditions = new ArrayList<>();
        conditions.add(new OdooCondition("id", ">", 0));

        // Apply the search term if it is not empty
        if (search != null && !search.isEmpty()) {
            conditions.add(new OdooCondition("name", "ilike", search));
        }

        // Create the pagination configuration
        OdooPagination odooPagination = new OdooPagination(
                Arrays.asList("id", "name", "company_id", "division_ids", "create_date", "write_date"),
                page * limit,
                limit,
                orderBy + (asc ? " asc" : " desc")
        );

        // Get the data from Odoo
        List<Object> resp = odooClientApi.searchAndRead(OdooConfig.RES_LOCATION, conditions, odooPagination);
        Integer totalRecord = odooClientApi.searchCount(OdooConfig.RES_LOCATION, conditions);

        // Convert to List<LocationResponse>
        List<LocationListResponse> locationList = LocationListResponse.fromOdooResponseMapper(resp);

        // Turn into Page<LocationResponse>
        return new PageImpl<>(locationList, pageableRequest, totalRecord);
    }

    public List<LocationListResponse> getAllLocations() {
        // Create the conditions
        List<OdooCondition> conditions = new ArrayList<>();
        conditions.add(new OdooCondition("id", ">", 0));

        // Create the pagination configuration
        OdooPagination odooPagination = new OdooPagination(
                Arrays.asList("id", "name", "company_id", "division_ids", "create_date", "write_date"),
                null,
                null,
                "id desc"
        );

        // Get the data from Odoo
        List<Object> resp = odooClientApi.searchAndRead(OdooConfig.RES_LOCATION, conditions, odooPagination);
        return LocationListResponse.fromOdooResponseMapper(resp);
    }

    public List<LocationListResponse> searchLocation(String name) {
        // Create the conditions
        List<OdooCondition> conditions = new ArrayList<>();
        conditions.add(new OdooCondition("id", ">", 0));

        // Apply the search term if it is not empty
        if (name != null && !name.isEmpty()) {
            conditions.add(new OdooCondition("name", "ilike", name));
        }

        // Create the pagination configuration
        OdooPagination odooPagination = new OdooPagination(
                Arrays.asList("id", "name", "company_id", "division_ids", "create_date", "write_date"),
                null,
                null,
                "id desc"
        );

        // Get the data from Odoo
        List<Object> resp = odooClientApi.searchAndRead(OdooConfig.RES_LOCATION, conditions, odooPagination);
        return LocationListResponse.fromOdooResponseMapper(resp);
    }

    public List<LocationListResponse> getLocationsByCompanyId(Long companyId) {
        // Create the conditions
        List<OdooCondition> conditions = new ArrayList<>();
        // Long to Integer because Odoo only accept Integer
        Integer idInt = Math.toIntExact(companyId);
        conditions.add(new OdooCondition("company_id", "=", idInt));

        // Create the pagination configuration
        OdooPagination odooPagination = new OdooPagination(
                Arrays.asList("id", "name", "company_id", "division_ids", "create_date", "write_date"),
                null,
                null,
                "id desc"
        );

        // Get the data from Odoo
        List<Object> resp = odooClientApi.searchAndRead(OdooConfig.RES_LOCATION, conditions, odooPagination);
        return LocationListResponse.fromOdooResponseMapper(resp);
    }

    public LocationResponse getLocationById(Long id) {
        // Create the conditions
        List<OdooCondition> conditions = new ArrayList<>();
        // Long to Integer because Odoo only accepts Integer
        Integer idInt = Math.toIntExact(id);
        conditions.add(new OdooCondition("id", "=", idInt));

        // Create the pagination configuration
        OdooPagination odooPagination = new OdooPagination(
                Arrays.asList("id", "name", "company_id", "division_ids", "create_date", "write_date"),
                null,
                null,
                null
        );

        // Start the first query to retrieve the location data
        CompletableFuture<List<Object>> locationFuture = CompletableFuture.supplyAsync(() ->
                odooClientApi.searchAndRead(OdooConfig.RES_LOCATION, conditions, odooPagination));

        // Retrieve the division IDs from the first query response
        CompletableFuture<List<Object>> divisionIdsFuture = locationFuture.thenApply(resp -> {
            List<Object> divisionIds = new ArrayList<>();
            for (Object obj : resp) {
                Map<String, Object> map = (Map<String, Object>) obj;
                Object[] divisionIdsArray = (Object[]) map.get("division_ids");
                divisionIds.addAll(Arrays.asList(divisionIdsArray));
            }
            return divisionIds;
        });

        // Create the conditions for the divisions
        CompletableFuture<List<OdooCondition>> divisionConditionsFuture = divisionIdsFuture.thenApply(divisionIds -> {
            List<OdooCondition> divisionConditions = new ArrayList<>();
            divisionConditions.add(new OdooCondition("id", "in", divisionIds));
            return divisionConditions;
        });

        // Create the pagination configuration for the divisions
        OdooPagination divisionOdooPagination = new OdooPagination(
                Arrays.asList("id", "name", "create_date", "write_date"),
                null,
                null,
                null
        );

        // Start the second query to retrieve the division data
        CompletableFuture<List<Object>> divisionFuture = divisionConditionsFuture.thenCompose(divisionConditions ->
                CompletableFuture.supplyAsync(() ->
                        odooClientApi.searchAndRead(OdooConfig.RES_DIVISION, divisionConditions, divisionOdooPagination)));

        // Combine the results of the location and division queries
        CompletableFuture<LocationResponse> combinedFuture = locationFuture.thenCombine(
                divisionFuture,
                (locationResp, divisionResp) -> LocationResponse.fromOdooResponseMapper(locationResp, divisionResp)
        );

        // Wait for the combined future to complete and return the result
        try {
            return combinedFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            // Handle any exceptions that occurred during the execution
            e.printStackTrace();
            return null;
        }
    }
}
