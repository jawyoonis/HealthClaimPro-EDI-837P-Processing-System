package com.billing.webapp.runner;
import com.billing.webapp.service.LegacyDataService;
import com.billing.webapp.controller.LegacyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class TestRunner implements CommandLineRunner {

    @Autowired
    private LegacyDataService legacyDataService;

    @Override
    public void run(String... args) throws Exception {
        createBillingHistory();
    }

    private void createBillingHistory() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 1; i <= 10; i++) {
            LegacyRequest legacyRequest = new LegacyRequest();
            legacyRequest.setFirstName("User " + i + " FN");
            legacyRequest.setLastName("User " + i + " LN");
            legacyRequest.setIdNumber("ID " + i);
            legacyRequest.setAddress("Address " + i);
            legacyRequest.setBirthday(LocalDate.now().minusYears(20 + i).format(formatter));
            legacyRequest.setZipcode("1234 " + i);
            legacyRequest.setStartDate(LocalDate.now().minusDays(i * 10));
            legacyRequest.setEndDate(LocalDate.now().minusDays((i * 10) - 5));
            legacyRequest.setRate(10.0 * i);
            //Initialize The list of Days and hours of service per day
            List<String> serviceDays = new ArrayList<>();
            List<Double> hoursPerDay = new ArrayList<>();
            serviceDays.add(DayOfWeek.MONDAY.name()); // "MONDAY"
            hoursPerDay.add(8.0*i); // 8 hours for Monday

            serviceDays.add(DayOfWeek.TUESDAY.name()); // "TUESDAY"
            hoursPerDay.add(1.0*i); // 1 hour for Tuesday

            serviceDays.add(DayOfWeek.WEDNESDAY.name()); // "WEDNESDAY"
            hoursPerDay.add(2.0*i); // 2 hours for Wednesday

            serviceDays.add(DayOfWeek.THURSDAY.name()); // "THURSDAY"
            hoursPerDay.add(3.0*i); // 3 hours for Thursday

            serviceDays.add(DayOfWeek.FRIDAY.name()); // "FRIDAY"
            hoursPerDay.add(4.0*i); // 4 hours for Friday
            serviceDays.add(DayOfWeek.SATURDAY.name()); // "SATURDAY"
            hoursPerDay.add(5.0 *i); // 4 hours for Saturday

            serviceDays.add(DayOfWeek.SUNDAY.name()); // "FRIDAY"
            hoursPerDay.add(6.0*i); // 2 hours for Sunday


            legacyRequest.setServiceDays(serviceDays);
            legacyRequest.setHoursPerDay(hoursPerDay); // Assuming you have a mechanism to align days with hours

            List<String> datesToSkip = new ArrayList<>();
            datesToSkip.add(LocalDate.of(2024, Month.JANUARY, 1).toString()); // New Year's Day
            datesToSkip.add(LocalDate.of(2024, Month.DECEMBER, 25).toString()); // Christmas
            legacyRequest.setDatesToSkip(datesToSkip);

            // Call the service method to save or process the LegacyRequest
            legacyDataService.saveOrUpdateLegacyData(legacyRequest, 100.0 * i);

            // For demonstration purposes, let's just print out what we have
            System.out.println("Created billing history for: " + legacyRequest.getFirstName() + " " + legacyRequest.getLastName());
            System.out.println("StartDate: " + legacyRequest.getStartDate() + " endDate: " + legacyRequest.getEndDate());
        }
    }
}
