package com.deliverysystem.dto;

import java.util.List;

public class DashboardDto {
    private String date;
    private DashboardSummaryDto summary;
    private List<RouteSummaryDto> routeSummary;
    private List<ExceptionDto> openExceptions;
    private List<OrderAwaitingGoodsDto> awaitingGoods;
    
    public DashboardDto() {
    }
    
    public DashboardDto(String date, DashboardSummaryDto summary, List<RouteSummaryDto> routeSummary, List<ExceptionDto> openExceptions, List<OrderAwaitingGoodsDto> awaitingGoods) {
        this.date = date;
        this.summary = summary;
        this.routeSummary = routeSummary;
        this.openExceptions = openExceptions;
        this.awaitingGoods = awaitingGoods;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public DashboardSummaryDto getSummary() {
        return summary;
    }
    
    public void setSummary(DashboardSummaryDto summary) {
        this.summary = summary;
    }
    
    public List<RouteSummaryDto> getRouteSummary() {
        return routeSummary;
    }
    
    public void setRouteSummary(List<RouteSummaryDto> routeSummary) {
        this.routeSummary = routeSummary;
    }
    
    public List<ExceptionDto> getOpenExceptions() {
        return openExceptions;
    }
    
    public void setOpenExceptions(List<ExceptionDto> openExceptions) {
        this.openExceptions = openExceptions;
    }
    
    public List<OrderAwaitingGoodsDto> getAwaitingGoods() {
        return awaitingGoods;
    }
    
    public void setAwaitingGoods(List<OrderAwaitingGoodsDto> awaitingGoods) {
        this.awaitingGoods = awaitingGoods;
    }
}
