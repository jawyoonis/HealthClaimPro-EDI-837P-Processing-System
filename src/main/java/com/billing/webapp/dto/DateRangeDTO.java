package com.billing.webapp.dto;

public class DateRangeDTO {
    private String startDate;
    private String endDate;

    // Constructor
    public DateRangeDTO(String startDate, String endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getter for startDate
    public String getStartDate() {
        return startDate;
    }

    // Getter for endDate
    public String getEndDate() {
        return endDate;
    }
}
