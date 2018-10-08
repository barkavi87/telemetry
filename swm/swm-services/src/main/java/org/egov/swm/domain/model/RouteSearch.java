package org.egov.swm.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RouteSearch extends Route {
    private String codes;
    private String collectionTypeCode;
    private String dumpingGroundCode;
    private String collectionPointCode;
    private Boolean isEndingDumpingGround;
    private Boolean excludeDumpingGround;
    private String sortBy;
    private Integer pageSize;
    private Integer offset;
}