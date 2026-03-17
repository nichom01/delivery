package com.deliverysystem.dto;

import java.util.List;

public class DayPlanDto {

    private String date;
    private String depotId;
    private String depotName;
    private int totalOrdersOnDay;
    private int totalBoxesOnDay;
    private List<DayPlanRouteDto> routes;

    public DayPlanDto() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDepotId() {
        return depotId;
    }

    public void setDepotId(String depotId) {
        this.depotId = depotId;
    }

    public String getDepotName() {
        return depotName;
    }

    public void setDepotName(String depotName) {
        this.depotName = depotName;
    }

    public int getTotalOrdersOnDay() {
        return totalOrdersOnDay;
    }

    public void setTotalOrdersOnDay(int totalOrdersOnDay) {
        this.totalOrdersOnDay = totalOrdersOnDay;
    }

    public int getTotalBoxesOnDay() {
        return totalBoxesOnDay;
    }

    public void setTotalBoxesOnDay(int totalBoxesOnDay) {
        this.totalBoxesOnDay = totalBoxesOnDay;
    }

    public List<DayPlanRouteDto> getRoutes() {
        return routes;
    }

    public void setRoutes(List<DayPlanRouteDto> routes) {
        this.routes = routes;
    }
}
