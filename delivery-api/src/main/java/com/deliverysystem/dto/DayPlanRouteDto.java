package com.deliverysystem.dto;

import java.util.List;

public class DayPlanRouteDto {

    private String routeId;
    private String routeCode;
    private String routeName;
    private int totalOrders;
    private int totalBoxes;
    private int ordersFullyReceived;
    private int ordersPartiallyReceived;
    private int ordersNotYetReceived;
    private String manifestStatus;
    private String vehicleRegistration;
    private String driverName;
    private List<DayPlanOrderDto> orders;

    public DayPlanRouteDto() {
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getTotalBoxes() {
        return totalBoxes;
    }

    public void setTotalBoxes(int totalBoxes) {
        this.totalBoxes = totalBoxes;
    }

    public int getOrdersFullyReceived() {
        return ordersFullyReceived;
    }

    public void setOrdersFullyReceived(int ordersFullyReceived) {
        this.ordersFullyReceived = ordersFullyReceived;
    }

    public int getOrdersPartiallyReceived() {
        return ordersPartiallyReceived;
    }

    public void setOrdersPartiallyReceived(int ordersPartiallyReceived) {
        this.ordersPartiallyReceived = ordersPartiallyReceived;
    }

    public int getOrdersNotYetReceived() {
        return ordersNotYetReceived;
    }

    public void setOrdersNotYetReceived(int ordersNotYetReceived) {
        this.ordersNotYetReceived = ordersNotYetReceived;
    }

    public String getManifestStatus() {
        return manifestStatus;
    }

    public void setManifestStatus(String manifestStatus) {
        this.manifestStatus = manifestStatus;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public List<DayPlanOrderDto> getOrders() {
        return orders;
    }

    public void setOrders(List<DayPlanOrderDto> orders) {
        this.orders = orders;
    }
}
