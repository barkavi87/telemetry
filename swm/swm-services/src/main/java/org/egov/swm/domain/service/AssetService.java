package org.egov.swm.domain.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swm.constants.Constants;
import org.egov.swm.domain.model.Asset;
import org.egov.swm.web.contract.AssetResponse;
import org.egov.swm.web.contract.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional(readOnly = true)
public class AssetService {

    private final RestTemplate restTemplate;

    private final String assetsBySearchCriteriaUrlByCode;
    private final String assetsBySearchCriteriaUrlByCategory;

    @Autowired
    public AssetService(final RestTemplate restTemplate,
            @Value("${egov.services.egov_asset.hostname}") final String assetServiceHostname,
            @Value("${egov.services.egov_asset.searchpath_by_code}") final String assetsBySearchCriteriaUrlByCode,
            @Value("${egov.services.egov_asset.searchpath_by_categoryname}") final String assetsBySearchCriteriaUrlByCategory) {

        this.restTemplate = restTemplate;
        this.assetsBySearchCriteriaUrlByCode = assetServiceHostname + assetsBySearchCriteriaUrlByCode;
        this.assetsBySearchCriteriaUrlByCategory = assetServiceHostname + assetsBySearchCriteriaUrlByCategory;
    }

    public Asset getByCode(final String code, final String tenantId, final RequestInfo requestInfo) {

        final RequestInfoWrapper wrapper = new RequestInfoWrapper();
        wrapper.setRequestInfo(requestInfo);

        final AssetResponse response = restTemplate.postForObject(assetsBySearchCriteriaUrlByCode, wrapper, AssetResponse.class,
                Constants.BIN_ASSET_CATEGORY,
                code, tenantId);

        if (response != null && !response.getAssets().isEmpty())
            return response.getAssets().get(0);
        else
            return null;

    }

    public List<Asset> getAll(final String tenantId, final RequestInfo requestInfo) {

        final RequestInfoWrapper wrapper = new RequestInfoWrapper();
        wrapper.setRequestInfo(requestInfo);

        final AssetResponse response = restTemplate.postForObject(assetsBySearchCriteriaUrlByCategory, wrapper,
                AssetResponse.class,
                Constants.BIN_ASSET_CATEGORY,
                tenantId);

        if (response != null && !response.getAssets().isEmpty())
            return response.getAssets();
        else
            return null;

    }

}