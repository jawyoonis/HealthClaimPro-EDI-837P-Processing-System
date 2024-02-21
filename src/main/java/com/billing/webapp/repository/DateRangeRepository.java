package com.billing.webapp.repository;

import com.billing.webapp.entity.DateRange;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DateRangeRepository extends CrudRepository<DateRange, Integer> {
    Optional<DateRange> findByStartDateAndEndDate(String startDate, String endDate);
}
