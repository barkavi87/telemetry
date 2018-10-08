package org.egov.swm.domain.model;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RouteCollectionPointMap {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("tenantId")
    @Length(min = 0, max = 256, message = "Value of tenantId shall be between 0 and 256")
    private String tenantId = null;

    @JsonProperty("route")
    private String route = null;

    @JsonProperty("collectionPoint")
    private CollectionPoint collectionPoint = null;

    @Digits(fraction = 2, integer = 3, message = "distance shall be with 2 decimal points")
    @DecimalMax(value = "500", message = "distance shall be between 0 and 500 Kms")
    @JsonProperty("distance")
    private Double distance = null;

    @Digits(fraction = 2, integer = 10, message = "garbageEstimate shall be with 2 decimal points")
    @JsonProperty("garbageEstimate")
    private Double garbageEstimate = null;

    @NotNull
    @JsonProperty("isStartingCollectionPoint")
    private Boolean isStartingCollectionPoint = null;

    @NotNull
    @JsonProperty("isEndingCollectionPoint")
    private Boolean isEndingCollectionPoint = null;

    @JsonProperty("dumpingGround")
    private DumpingGround dumpingGround = null;

}
