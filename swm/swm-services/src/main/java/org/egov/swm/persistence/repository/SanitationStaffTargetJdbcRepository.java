package org.egov.swm.persistence.repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swm.domain.model.CollectionPoint;
import org.egov.swm.domain.model.CollectionPointSearch;
import org.egov.swm.domain.model.DumpingGround;
import org.egov.swm.domain.model.Pagination;
import org.egov.swm.domain.model.Route;
import org.egov.swm.domain.model.RouteCollectionPointMap;
import org.egov.swm.domain.model.RouteSearch;
import org.egov.swm.domain.model.SanitationStaffTarget;
import org.egov.swm.domain.model.SanitationStaffTargetMap;
import org.egov.swm.domain.model.SanitationStaffTargetSearch;
import org.egov.swm.domain.model.SwmProcess;
import org.egov.swm.domain.service.DumpingGroundService;
import org.egov.swm.domain.service.RouteService;
import org.egov.swm.domain.service.SwmProcessService;
import org.egov.swm.persistence.entity.SanitationStaffTargetEntity;
import org.egov.swm.web.contract.Employee;
import org.egov.swm.web.contract.EmployeeResponse;
import org.egov.swm.web.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

@Service
public class SanitationStaffTargetJdbcRepository extends JdbcRepository {

    public static final String TABLE_NAME = "egswm_sanitationstafftarget";

    @Autowired
    public SanitationStaffTargetMapJdbcRepository sanitationStaffTargetMapJdbcRepository;

    @Autowired
    public CollectionPointJdbcRepository collectionPointJdbcRepository;

    @Autowired
    private DumpingGroundService dumpingGroundService;

    @Autowired
    private SwmProcessService swmProcessService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RouteService routeService;

    public Pagination<SanitationStaffTarget> search(final SanitationStaffTargetSearch searchRequest) {

        String searchQuery = "select * from " + TABLE_NAME + " :condition  :orderby ";

        final Map<String, Object> paramValues = new HashMap<>();
        final StringBuffer params = new StringBuffer();

        if (searchRequest.getSortBy() != null && !searchRequest.getSortBy().isEmpty()) {
            validateSortByOrder(searchRequest.getSortBy());
            validateEntityFieldName(searchRequest.getSortBy(), SanitationStaffTargetSearch.class);
        }

        String orderBy = "order by targetNo";
        if (searchRequest.getSortBy() != null && !searchRequest.getSortBy().isEmpty())
            orderBy = "order by " + searchRequest.getSortBy();

        if (searchRequest.getTargetNo() != null) {
            addAnd(params);
            params.append("targetNo in (:targetNo)");
            paramValues.put("targetNo", searchRequest.getTargetNo());
        }

        if (searchRequest.getTargetNos() != null) {
            addAnd(params);
            params.append("targetNo in (:targetNos)");
            paramValues.put("targetNos", new ArrayList<>(Arrays.asList(searchRequest.getTargetNos().split(","))));
        }
        if (searchRequest.getTenantId() != null) {
            addAnd(params);
            params.append("tenantId =:tenantId");
            paramValues.put("tenantId", searchRequest.getTenantId());
        }

        if (searchRequest.getRouteCode() != null) {
            addAnd(params);
            params.append("route =:route");
            paramValues.put("route", searchRequest.getRouteCode());
        }
        final DateFormat validationDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        validationDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

        if (searchRequest.getValidate() == null || !searchRequest.getValidate()) {
            if (searchRequest.getTargetFrom() != null) {
                addAnd(params);
                params.append(
                        "to_char((to_timestamp(targetFrom/1000) AT TIME ZONE 'Asia/Kolkata')::date,'yyyy-mm-dd') >=:targetFrom");
                paramValues.put("targetFrom", validationDateFormat.format(searchRequest.getTargetFrom()));
            }

            if (searchRequest.getTargetTo() != null) {
                addAnd(params);
                params.append(
                        "to_char((to_timestamp(targetTo/1000) AT TIME ZONE 'Asia/Kolkata')::date,'yyyy-mm-dd') <=:targetTo");
                paramValues.put("targetTo", validationDateFormat.format(searchRequest.getTargetTo()));
            }
        }

        if (searchRequest.getTargetFrom() != null && searchRequest.getTargetTo() != null &&
                searchRequest.getValidate() != null && searchRequest.getValidate()) {
            addAnd(params);
            params.append("((to_timestamp(targetfrom/1000) AT TIME ZONE 'Asia/Kolkata') BETWEEN" +
                    " (to_timestamp(:targetFrom/1000) AT TIME ZONE 'Asia/Kolkata') AND (to_timestamp(:targetTo/1000) AT TIME ZONE 'Asia/Kolkata')"
                    +
                    " or (to_timestamp(targetto/1000) AT TIME ZONE 'Asia/Kolkata') BETWEEN" +
                    " (to_timestamp(:targetFrom/1000) AT TIME ZONE 'Asia/Kolkata') AND (to_timestamp(:targetTo/1000) AT TIME ZONE 'Asia/Kolkata')"
                    +
                    " or (to_timestamp(:targetFrom/1000) AT TIME ZONE 'Asia/Kolkata') BETWEEN" +
                    " (to_timestamp(targetfrom/1000) AT TIME ZONE 'Asia/Kolkata') AND (to_timestamp(targetto/1000) AT TIME ZONE 'Asia/Kolkata')"
                    +
                    " or (to_timestamp(:targetTo/1000) AT TIME ZONE 'Asia/Kolkata') BETWEEN" +
                    " (to_timestamp(targetfrom/1000) AT TIME ZONE 'Asia/Kolkata') AND (to_timestamp(targetto/1000) AT TIME ZONE 'Asia/Kolkata'))");
            paramValues.put("targetFrom", searchRequest.getTargetFrom());
            paramValues.put("targetTo", searchRequest.getTargetTo());
        }

        if (searchRequest.getSwmProcessCode() != null) {
            addAnd(params);
            params.append("swmProcess =:swmProcess");
            paramValues.put("swmProcess", searchRequest.getSwmProcessCode());
        }

        if (searchRequest.getEmployeeCode() != null) {
            addAnd(params);
            params.append("employee =:employee");
            paramValues.put("employee", searchRequest.getEmployeeCode());
        }

        if (searchRequest.getDumpingGroundCode() != null) {
            addAnd(params);
            params.append("dumpingGround =:dumpingGround");
            paramValues.put("dumpingGround", searchRequest.getDumpingGroundCode());
        }

        Pagination<SanitationStaffTarget> page = new Pagination<>();
        if (searchRequest.getOffset() != null)
            page.setOffset(searchRequest.getOffset());
        if (searchRequest.getPageSize() != null)
            page.setPageSize(searchRequest.getPageSize());

        if (params.length() > 0)
            searchQuery = searchQuery.replace(":condition", " where " + params.toString());
        else

            searchQuery = searchQuery.replace(":condition", "");

        searchQuery = searchQuery.replace(":orderby", orderBy);

        page = (Pagination<SanitationStaffTarget>) getPagination(searchQuery, page, paramValues);
        searchQuery = searchQuery + " :pagination";

        searchQuery = searchQuery.replace(":pagination",
                "limit " + page.getPageSize() + " offset " + page.getOffset() * page.getPageSize());

        final BeanPropertyRowMapper row = new BeanPropertyRowMapper(SanitationStaffTargetEntity.class);

        final List<SanitationStaffTarget> sanitationStaffTargetList = new ArrayList<>();

        final List<SanitationStaffTargetEntity> sanitationStaffTargetEntities = namedParameterJdbcTemplate
                .query(searchQuery.toString(), paramValues, row);

        for (final SanitationStaffTargetEntity sanitationStaffTargetEntity : sanitationStaffTargetEntities)
            sanitationStaffTargetList.add(sanitationStaffTargetEntity.toDomain());

        if (sanitationStaffTargetList != null && !sanitationStaffTargetList.isEmpty()) {

            populateCollectionPoints(sanitationStaffTargetList);

            populateDumpingGrounds(sanitationStaffTargetList);

            populateSwmProcesses(sanitationStaffTargetList);

            populateEmployees(sanitationStaffTargetList);

            populateRoutes(sanitationStaffTargetList);

            populateSelectedCollectionPoints(sanitationStaffTargetList);

        }
        page.setPagedData(sanitationStaffTargetList);

        return page;
    }

    private void populateSelectedCollectionPoints(final List<SanitationStaffTarget> sanitationStaffTargetList) {

        Map<String, CollectionPoint> selectedCollectionPointMap;
        List<CollectionPoint> routeCollectionPoints;
        CollectionPoint cp;
        for (final SanitationStaffTarget sst : sanitationStaffTargetList) {

            routeCollectionPoints = new ArrayList<>();
            selectedCollectionPointMap = new HashMap<String, CollectionPoint>();
            if (sst.getCollectionPoints() != null && sst.getRoute() != null && sst.getRoute().getCollectionPoints() != null) {

                for (final CollectionPoint scp : sst.getCollectionPoints())
                    selectedCollectionPointMap.put(scp.getCode(), scp);
                for (final RouteCollectionPointMap rcpm : sst.getRoute().getCollectionPoints())
                    if (rcpm.getCollectionPoint() != null && rcpm.getCollectionPoint().getCode() != null) {
                        cp = rcpm.getCollectionPoint();
                        if (selectedCollectionPointMap.get(cp.getCode()) == null)
                            cp.setIsSelected(false);
                        else
                            cp.setIsSelected(true);
                        routeCollectionPoints.add(cp);
                    }
                sst.setCollectionPoints(routeCollectionPoints);
            }
        }

    }

    private void populateRoutes(final List<SanitationStaffTarget> sanitationStaffTargetList) {

        final StringBuffer routeCodes = new StringBuffer();
        final Set<String> routeCodesSet = new HashSet<>();
        final RouteSearch routeSearch = new RouteSearch();
        Pagination<Route> routes;

        for (final SanitationStaffTarget sst : sanitationStaffTargetList)
            if (sst.getRoute() != null && sst.getRoute().getCode() != null
                    && !sst.getRoute().getCode().isEmpty())
                routeCodesSet.add(sst.getRoute().getCode());

        final List<String> routeCodeList = new ArrayList(routeCodesSet);

        for (final String code : routeCodeList) {

            if (routeCodes.length() >= 1)
                routeCodes.append(",");

            routeCodes.append(code);

        }

        String tenantId = null;
        final Map<String, Route> routeMap = new HashMap<>();

        if (sanitationStaffTargetList != null && !sanitationStaffTargetList.isEmpty())
            tenantId = sanitationStaffTargetList.get(0).getTenantId();

        routeSearch.setTenantId(tenantId);
        routeSearch.setCodes(routeCodes.toString());
        routes = routeService.search(routeSearch);

        if (routes != null && routes.getPagedData() != null)
            for (final Route bd : routes.getPagedData())
                routeMap.put(bd.getCode(), bd);

        for (final SanitationStaffTarget sanitationStaffTarget : sanitationStaffTargetList)
            if (sanitationStaffTarget.getRoute() != null && sanitationStaffTarget.getRoute().getCode() != null
                    && !sanitationStaffTarget.getRoute().getCode().isEmpty())
                sanitationStaffTarget.setRoute(routeMap.get(sanitationStaffTarget.getRoute().getCode()));

    }

    private void populateCollectionPoints(final List<SanitationStaffTarget> sanitationStaffTargetList) {

        SanitationStaffTargetMap sstm;
        CollectionPointSearch cps;
        Pagination<CollectionPoint> collectionPointList;
        final Map<String, CollectionPoint> collectionPointMap = new HashMap<>();
        final Map<String, List<SanitationStaffTargetMap>> sanitationStaffTargetMap = new HashMap<>();
        final Map<String, List<CollectionPoint>> collectionPointsMap = new HashMap<>();
        final StringBuffer targetNos = new StringBuffer();
        final StringBuffer collectionPointCodes = new StringBuffer();
        final Set<String> collectionPointCodeSet = new HashSet<>();
        List<SanitationStaffTargetMap> targetCollectionPoints;
        String tenantId = null;

        if (sanitationStaffTargetList != null && !sanitationStaffTargetList.isEmpty()) {

            tenantId = sanitationStaffTargetList.get(0).getTenantId();

            for (final SanitationStaffTarget sanitationStaffTarget : sanitationStaffTargetList) {

                if (targetNos.length() > 0)
                    targetNos.append(",");

                targetNos.append(sanitationStaffTarget.getTargetNo());

            }

            sstm = new SanitationStaffTargetMap();

            sstm.setTenantId(tenantId);
            sstm.setTargetNos(targetNos.toString());

            targetCollectionPoints = sanitationStaffTargetMapJdbcRepository.search(sstm);
            List<SanitationStaffTargetMap> mapList;
            for (final SanitationStaffTargetMap map : targetCollectionPoints) {

                if (map.getCollectionPoint() != null && !map.getCollectionPoint().isEmpty())
                    collectionPointCodeSet.add(map.getCollectionPoint());

                if (sanitationStaffTargetMap.get(map.getSanitationStaffTarget()) == null)
                    sanitationStaffTargetMap.put(map.getSanitationStaffTarget(), Collections.singletonList(map));
                else {

                    mapList = new ArrayList<>(sanitationStaffTargetMap.get(map.getSanitationStaffTarget()));

                    mapList.add(map);

                    sanitationStaffTargetMap.put(map.getSanitationStaffTarget(), mapList);

                }
            }

            final List<String> cpcs = new ArrayList(collectionPointCodeSet);

            for (final String code : cpcs) {

                if (collectionPointCodes.length() > 0)
                    collectionPointCodes.append(",");

                collectionPointCodes.append(code);

            }

            if (collectionPointCodes != null && collectionPointCodes.length() > 0) {

                cps = new CollectionPointSearch();
                cps.setTenantId(tenantId);
                cps.setCodes(collectionPointCodes.toString());

                collectionPointList = collectionPointJdbcRepository.search(cps);

                if (collectionPointList != null && collectionPointList.getPagedData() != null
                        && !collectionPointList.getPagedData().isEmpty())
                    for (final CollectionPoint cp : collectionPointList.getPagedData())
                        collectionPointMap.put(cp.getCode(), cp);

                List<CollectionPoint> cpList;

                for (final SanitationStaffTargetMap map : targetCollectionPoints)
                    if (collectionPointsMap.get(map.getSanitationStaffTarget()) == null)
                        collectionPointsMap.put(map.getSanitationStaffTarget(),
                                Collections.singletonList(collectionPointMap.get(map.getCollectionPoint())));
                    else {

                        cpList = new ArrayList<>(collectionPointsMap.get(map.getSanitationStaffTarget()));

                        cpList.add(collectionPointMap.get(map.getCollectionPoint()));

                        collectionPointsMap.put(map.getSanitationStaffTarget(), cpList);

                    }

                for (final SanitationStaffTarget sanitationStaffTarget : sanitationStaffTargetList)
                    sanitationStaffTarget.setCollectionPoints(collectionPointsMap.get(sanitationStaffTarget.getTargetNo()));
            }
        }

    }

    private void populateEmployees(final List<SanitationStaffTarget> sanitationStaffTargetList) {

        final StringBuffer employeeCodes = new StringBuffer();
        final Set<String> employeeCodesSet = new HashSet<>();

        for (final SanitationStaffTarget sst : sanitationStaffTargetList)
            if (sst.getEmployee() != null && sst.getEmployee().getCode() != null
                    && !sst.getEmployee().getCode().isEmpty())
                employeeCodesSet.add(sst.getEmployee().getCode());

        final List<String> employeeCodeList = new ArrayList(employeeCodesSet);

        for (final String code : employeeCodeList) {

            if (employeeCodes.length() >= 1)
                employeeCodes.append(",");

            employeeCodes.append(code);

        }
        if (employeeCodes != null && employeeCodes.length() > 0) {

            String tenantId = null;
            final Map<String, Employee> employeeMap = new HashMap<>();

            if (sanitationStaffTargetList != null && !sanitationStaffTargetList.isEmpty())
                tenantId = sanitationStaffTargetList.get(0).getTenantId();

            final EmployeeResponse response = employeeRepository.getEmployeeByCodes(employeeCodes.toString(), tenantId,
                    new RequestInfo());

            if (response != null && response.getEmployees() != null)
                for (final Employee e : response.getEmployees())
                    employeeMap.put(e.getCode(), e);

            for (final SanitationStaffTarget sanitationStaffTarget : sanitationStaffTargetList)
                if (sanitationStaffTarget.getEmployee() != null && sanitationStaffTarget.getEmployee().getCode() != null
                        && !sanitationStaffTarget.getEmployee().getCode().isEmpty())
                    sanitationStaffTarget.setEmployee(employeeMap.get(sanitationStaffTarget.getEmployee().getCode()));

        }

    }

    private void populateSwmProcesses(final List<SanitationStaffTarget> sanitationStaffTargetList) {
        final Map<String, SwmProcess> swmProcessMap = new HashMap<>();
        String tenantId = null;

        if (sanitationStaffTargetList != null && !sanitationStaffTargetList.isEmpty())
            tenantId = sanitationStaffTargetList.get(0).getTenantId();

        final List<SwmProcess> swmProcesses = swmProcessService.getAll(tenantId, new RequestInfo());

        for (final SwmProcess sp : swmProcesses)
            swmProcessMap.put(sp.getCode(), sp);

        for (final SanitationStaffTarget sanitationStaffTarget : sanitationStaffTargetList)
            if (sanitationStaffTarget.getSwmProcess() != null && sanitationStaffTarget.getSwmProcess().getCode() != null
                    && !sanitationStaffTarget.getSwmProcess().getCode().isEmpty())
                sanitationStaffTarget.setSwmProcess(swmProcessMap.get(sanitationStaffTarget.getSwmProcess().getCode()));
    }

    private void populateDumpingGrounds(final List<SanitationStaffTarget> sanitationStaffTargetList) {

        final Map<String, DumpingGround> dumpingGroundMap = new HashMap<>();
        String tenantId = null;

        if (sanitationStaffTargetList != null && !sanitationStaffTargetList.isEmpty())
            tenantId = sanitationStaffTargetList.get(0).getTenantId();

        final List<DumpingGround> dumpingGrounds = dumpingGroundService.getAll(tenantId, new RequestInfo());

        for (final DumpingGround dg : dumpingGrounds)
            dumpingGroundMap.put(dg.getCode(), dg);

        for (final SanitationStaffTarget sanitationStaffTarget : sanitationStaffTargetList)
            if (sanitationStaffTarget.getDumpingGround() != null && sanitationStaffTarget.getDumpingGround().getCode() != null
                    && !sanitationStaffTarget.getDumpingGround().getCode().isEmpty())
                sanitationStaffTarget.setDumpingGround(dumpingGroundMap.get(sanitationStaffTarget.getDumpingGround().getCode()));
    }

}