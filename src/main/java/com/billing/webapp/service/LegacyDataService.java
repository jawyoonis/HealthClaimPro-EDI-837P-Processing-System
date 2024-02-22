package com.billing.webapp.service;

import com.billing.webapp.controller.LegacyRequest;
import com.billing.webapp.dto.BillingHistoryDTO;
import com.billing.webapp.dto.DateRangeDTO;
import com.billing.webapp.entity.DateRange;
import com.billing.webapp.entity.LegacyData;
import com.billing.webapp.entity.TotalClaimChargePerUser;
import com.billing.webapp.repository.DateRangeRepository;
import com.billing.webapp.repository.LegacyDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@CrossOrigin
//@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LegacyDataService {
    /*
        * This class is responsible for handling the business logic of the application.
        * It is used to save and retrieve data from the database.
        * The @Service annotation is used to indicate that this class is a service.
        * The @Autowired annotation is used to inject the dependencies.
        * The @CrossOrigin annotation is used to allow the frontend to access the backend.
        * The saveOrUpdateLegacyData method is used to save or update the data in the database.
        * The searchByFirstName method is used to search the database by first name.
        * The findByFirstName method is used to search the database by first name.
        * The getBillingHistory method is used to retrieve the billing history.
        * The convertToBillingHistoryDTO method is used to convert the LegacyData entity to BillingHistoryDTO.
        * The findExistingDateRange method is used to check if the DateRange with the same start and end dates already exists.
     */

    private final LegacyDataRepository legacyDataRepository;
    private final LegacyService legacyService;
    private final DateRangeRepository dateRangeRepository;

    /*
    *Constructor injection is used here to inject the dependencies into the class using the constructor instead of the setter methods
    * This is the recommended way to inject dependencies
    * The classes injected here are the LegacyDataRepository and LegacyService classes because they are dependencies of this class and are needed to perform the business logic of the application
     */
    @Autowired
    public LegacyDataService(@Lazy LegacyService legacyService, LegacyDataRepository legacyDataRepository, DateRangeRepository dateRangeRepository) {
        this.legacyDataRepository = legacyDataRepository;
        this.legacyService = legacyService;
        this.dateRangeRepository = dateRangeRepository;
    }

    /*
    * The saveOrUpdateLegacyData method is used to save or update the data in the database.
    * It takes a LegacyRequest object and the total claim amount as parameters.
    * The LegacyRequest object contains the data that is sent from the frontend.
    * The total claim amount is calculated in the LegacyService class.
    * The method first checks if the user already exists in the database.
    * If the user exists, the method uses the existing record.
    * If the user does not exist, the method creates a new LegacyData entity from the request data.
    * The method then checks if the DateRange with the same start and end dates already exists.
    * If the DateRange does not exist, the method creates a new DateRange entity from the request data.
    * The method then creates and links a TotalClaimChargePerUser entity to the LegacyData and DateRange entities.
    * The method then saves the LegacyData entity using the repository.
    * The method returns the LegacyData entity.
    * The @CrossOrigin annotation is used to allow the frontend to access the backend.
     */
   @CrossOrigin
    public LegacyData saveOrUpdateLegacyData(LegacyRequest request, Double totalClaimAmount) {
        LegacyData data = legacyDataRepository.findByIdNumber(request.getIdNumber())
                .orElseGet(() -> new LegacyData());

        mapRequestToLegacyData(request, data);

        // Handle DateRange creation or update
        DateRange dateRange = createDateRangeForLegacyData(data, request);
        // Check if dateRanges is null and initialize it if necessary
        if (data.getDateRanges() == null) {
            data.setDateRanges(new ArrayList<>());
        }
        data.getDateRanges().add(dateRange);

        // Handle TotalClaimCharge
        createTotalClaimCharge(data, totalClaimAmount, dateRange);

        // Handle skipped service dates
        Set<LocalDate> skippedDates = request.getDatesToSkip().stream()
                .map(LocalDate::parse)
                .collect(Collectors.toSet());
        data.setSkippedDates(skippedDates);

        // Save the LegacyData entity with all related entities
        return legacyDataRepository.save(data);
    }

    /**
     * This maps the legacy Request to the legacy data entity
     * @param request request for legacy requested data
     * @param data for the lgacydata entity
     */
    private void mapRequestToLegacyData(LegacyRequest request, LegacyData data) {
        // Mapping fields from request to LegacyData entity
        data.setFirstName(request.getFirstName());
        data.setLastName(request.getLastName());
        data.setIdNumber(request.getIdNumber());
        data.setAddress(request.getAddress());
        data.setBirthday(String.valueOf(LocalDate.parse(request.getBirthday()))); // Ensure this is correctly parsed
        data.setZipcode(request.getZipcode());
        data.setRate(request.getRate());
        // Assuming datesToSkip are handled separately as shown later
    }

    /**
     *Create and map  date range for the legacy data
     * @param data
     * @param request
     * @return data range  for legacy data
     */
    private DateRange createDateRangeForLegacyData(LegacyData data, LegacyRequest request) {
        DateRange dateRange = new DateRange();
        dateRange.setLegacyData(data); // Establishing the bidirectional relationship
        // Set hours per day for each day of the week from the request data (0.0 if not provided)
        // and convertToHoursPerDayMap() provides a correct mapping
        dateRange.setStartDate(request.getStartDate()); // Adjusted for direct access
        dateRange.setEndDate(request.getEndDate());
        Map<DayOfWeek, Double> serviceHours = request.convertToHoursPerDayMap();
        dateRange.setMondayHours(serviceHours.getOrDefault(DayOfWeek.MONDAY, 0.0));
        dateRange.setTuesdayHours(serviceHours.getOrDefault(DayOfWeek.TUESDAY, 0.0));
        dateRange.setWednesdayHours(serviceHours.getOrDefault(DayOfWeek.WEDNESDAY, 0.0));
        dateRange.setThursdayHours(serviceHours.getOrDefault(DayOfWeek.THURSDAY, 0.0));
        dateRange.setFridayHours(serviceHours.getOrDefault(DayOfWeek.FRIDAY, 0.0));
        dateRange.setSaturdayHours(serviceHours.getOrDefault(DayOfWeek.SATURDAY, 0.0));
        dateRange.setSundayHours(serviceHours.getOrDefault(DayOfWeek.SUNDAY, 0.0));
        dateRange.setServiceHours(serviceHours); // Directly setting the map assuming DateRange supports it
        return dateRange;
    }

    /**
     * This method generates and calculates the totla charges and then maps to the legacyData
     * @param data
     * @param totalClaimAmount the total charges for a particular date range
     * @param dateRange Date range for a particular total charges
     */
    private void createTotalClaimCharge(LegacyData data, Double totalClaimAmount, DateRange dateRange) {
        TotalClaimChargePerUser totalCharge = new TotalClaimChargePerUser();
        totalCharge.setTotalClaimCharge(totalClaimAmount);
        totalCharge.setLegacyData(data);
        totalCharge.setDateRange(dateRange);
        if(data.getTotalClaimCharges()==null){
            data.setTotalClaimCharges(new ArrayList<>());
        }
        data.getTotalClaimCharges().add(totalCharge);
    }

//    @CrossOrigin
//    @Transactional
//    public LegacyData saveOrUpdateLegacyData(LegacyRequest request, Double totalClaimAmount) {
//
//        // First, check if the user already exists
//        Optional<LegacyData> existingUser = legacyDataRepository.findByIdNumber(request.getIdNumber());
//
//        final LegacyData data; // This is the LegacyData entity that will be saved
//
//        if (existingUser.isPresent()) {
//            // User exists, use the existing record
//            data = existingUser.get();
//        } else {
//            // Create a new LegacyData entity from the request data
//            // This is a new user, create a new record
//            data = new LegacyData();
//            // Map fields from request to entity
//            data.setFirstName(request.getFirstName());
//            data.setLastName(request.getLastName());
//            data.setIdNumber(request.getIdNumber());
//            data.setAddress(request.getAddress());
//            data.setBirthday(request.getBirthday());
//            data.setZipcode(request.getZipcode());
//            data.setRate(request.getRate());
//            data.setDatesToSkip(request.getDatesToSkip());
//        }
//        // Ensure totalClaimCharges list is initialized
//        // This is needed to avoid null pointer exceptions
//        if (data.getTotalClaimCharges() == null) {
//            data.setTotalClaimCharges(new ArrayList<>());
//        }
//
//        // Check if the DateRange with the same start and end dates already exists
//        DateRange dateRange = findExistingDateRange(data, legacyService.getActualStart(), legacyService.getActualEnd());
//        if (dateRange == null) {
//            // DateRange does not exist, create a new DateRange entity from the request data
//            dateRange = new DateRange();
//            dateRange.setStartDate(legacyService.getActualStart()); // Set actual start date
//            dateRange.setEndDate(legacyService.getActualEnd()); // Set actual end date
//            // Set hours per day for each day of the week from the request data (0.0 if not provided)
//            Map<DayOfWeek, Double> serviceHours = request.convertToHoursPerDayMap();
//            dateRange.setMondayHours(serviceHours.getOrDefault(DayOfWeek.MONDAY, 0.0));
//            dateRange.setTuesdayHours(serviceHours.getOrDefault(DayOfWeek.TUESDAY, 0.0));
//            dateRange.setWednesdayHours(serviceHours.getOrDefault(DayOfWeek.WEDNESDAY, 0.0));
//            dateRange.setThursdayHours(serviceHours.getOrDefault(DayOfWeek.THURSDAY, 0.0));
//            dateRange.setFridayHours(serviceHours.getOrDefault(DayOfWeek.FRIDAY, 0.0));
//            dateRange.setSaturdayHours(serviceHours.getOrDefault(DayOfWeek.SATURDAY, 0.0));
//            dateRange.setSundayHours(serviceHours.getOrDefault(DayOfWeek.SUNDAY, 0.0));
//
//            // If it's an existing user, add to the existing date ranges
//            List<DateRange> dateRanges = data.getDateRanges();
//            if (dateRanges == null) {
//                // Ensure dateRanges list is initialized
//                dateRanges = new ArrayList<>();
//                // Add the new DateRange to the list
//                dateRanges.add(dateRange);
//                // Set the list to the entity
//                data.setDateRanges(dateRanges);
//            } else {
//                // Add the new DateRange to the list of existing DateRanges for the user
//                dateRanges.add(dateRange);
//            }
//            // Link DateRange with LegacyData entity (bidirectional) - this is needed for the JSON response to work properly (otherwise, it will cause an infinite loop)
//            dateRange.setLegacyData(data);
//        }
//        // Create and link TotalClaimChargePerUser
//        TotalClaimChargePerUser totalCharge = new TotalClaimChargePerUser();
//        // Set the total claim charge
//        totalCharge.setTotalClaimCharge(totalClaimAmount);
//        // Link TotalClaimChargePerUser with LegacyData and DateRange (bidirectional) - this is needed for the JSON response to work properly (otherwise, it will cause an infinite loop)
//        totalCharge.setLegacyData(data);
//        totalCharge.setDateRange(dateRange);
//
//        // Add the TotalClaimChargePerUser to the list of TotalClaimChargePerUser for the user (bidirectional) - this is needed for the JSON response to work properly (otherwise, it will cause an infinite loop)
//        data.getTotalClaimCharges().add(totalCharge);
//
//        // Handling skipped service dates
//        // Convert the list of dates to skip from the request to a set of LocalDate objects
//        Set<LocalDate> skippedDates = request.getDatesToSkip().stream()
//                .map(LocalDate::parse) // Converts String to LocalDate
//                .collect(Collectors.toSet()); // Converts the stream to a set of LocalDate objects (removes duplicates) - this is needed to avoid duplicate entries in the database
//        data.setSkippedDates(skippedDates); // Set the skipped dates to the entity
//
//        // Save the entity using the repository
//        return legacyDataRepository.save(data);
//    }

    /*
    * The findExistingDateRange method is used to check if the DateRange with the same start and end dates already exists. This is used to avoid duplicate claim entries for each user in the database.
    * It takes a LegacyData entity, a start date, and an end date as parameters.
    * The method returns the DateRange if it exists, otherwise it returns null.
    * If the DateRange with the same start and end dates exists, the method returns it.
    * Otherwise, the method returns null.
     */
//    private DateRange findExistingDateRange(LegacyData data, LocalDate startDate, LocalDate endDate) {
//        // Iterate through the list of DateRanges for the user
//        if (data.getDateRanges() != null) {
//            for (DateRange dateRange : data.getDateRanges()) {
//                // Check if the DateRange with the same start and end dates exists
////                if (dateRange.getStartDate().equals(startDate) && dateRange.getEndDate().equals(endDate)) {
//                    return dateRange;
////                }
//            }
//        }
//        return null;
//    }
//    private DateRange findExistingDateRange(LegacyData data, LocalDate startDate, LocalDate endDate) {
//        if (data.getDateRanges() == null) {
//            return null;
//        }
//        return data.getDateRanges().stream()
//                .filter(dateRange ->
//                        startDate.equals(dateRange.getStartDate()) &&
//                                endDate.equals(dateRange.getEndDate())
//                )
//                .findFirst()
//                .orElse(null);
//    }




    @CrossOrigin
    public List<LegacyData> searchByFirstName(String firstName) {
        // This method is used to search the database by first name (case insensitive) and return a list of users that match the search criteria
        // (e.g. if the user searches for "John", the method will return all users with the first name "John" or "john" or "JOHN")
        return legacyDataRepository.findByFirstNameContainingIgnoreCase(firstName);
    }

    @CrossOrigin
    public LegacyData findByFirstName(String firstName) {
        // This is just a simple example. You might have more complex logic,
        // especially handling the case where multiple users might have the same first name
        List<LegacyData> users = legacyDataRepository.findByFirstNameContainingIgnoreCase(firstName);
        if (! users.isEmpty()) {
            // Just returning the first match for simplicity
            return users.get(0);
        }
        return null; // No match found for the given first name
    }


    /*
    * The getBillingHistory method is used to retrieve the billing history.
    * The method first retrieves all the LegacyData entities from the database.
    * The method then converts each LegacyData entity to a BillingHistoryDTO object.
    * The method returns the list of BillingHistoryDTO objects.
    * The @CrossOrigin annotation is used to allow the frontend to access the backend.
    * The @Transactional annotation is used to ensure that the data is retrieved from the database in a consistent state.
     */
    public List<BillingHistoryDTO> getBillingHistory() {
        // Retrieve all LegacyData entities from the database using the repository
        List<LegacyData> allLegacyData = (List<LegacyData>) legacyDataRepository.findAll();
        // Convert each LegacyData entity to a BillingHistoryDTO object and return the list of BillingHistoryDTO objects
        return allLegacyData.stream().map(this::convertToBillingHistoryDTO).collect(Collectors.toList());
    }

    /*
    * The convertToBillingHistoryDTO method is used to convert the LegacyData entity to BillingHistoryDTO.
    * It takes a LegacyData entity as a parameter.
    * The method first creates a new BillingHistoryDTO object.
    * The method then sets the first name, last name, and total claim charge fields of the BillingHistoryDTO object.
    * The method then converts the date ranges and skipped dates from the LegacyData entity to the BillingHistoryDTO object.
    * The method returns the BillingHistoryDTO object.
     */
    private BillingHistoryDTO convertToBillingHistoryDTO(LegacyData legacyData) {
        BillingHistoryDTO dto = new BillingHistoryDTO(); // Create a new BillingHistoryDTO object
        dto.setFirstName(legacyData.getFirstName()); // Set the first name
        dto.setLastName(legacyData.getLastName()); // Set the last name

        // Calculate total claim charge for the user (sum of all total claim charges for the user since multiple files can be generated from each request in the batch)
        double totalClaimCharge = legacyData.getTotalClaimCharges().stream() // Get the list of total claim charges for the user from the LegacyData entity and convert it to a stream
                .mapToDouble(TotalClaimChargePerUser::getTotalClaimCharge) // Convert the stream to a DoubleStream and get the total claim charge for each TotalClaimChargePerUser entity in the list
                .sum(); // Sum the total claim charges for each TotalClaimChargePerUser entity in the list to get the total claim charge for the user
        dto.setTotalClaimCharge(totalClaimCharge); // Set the total claim charge for the user in the BillingHistoryDTO object

        // Existing logic to set date ranges and skipped dates
        List<DateRangeDTO> dateRangeDTOs = legacyData.getDateRanges().stream()// Get the list of date ranges for the user from the LegacyData entity and convert it to a stream
                .map(dr -> new DateRangeDTO(dr.getStartDate(), dr.getEndDate())) // Convert the stream to a stream of DateRangeDTO objects (convert each DateRange entity to a DateRangeDTO object)
                .collect(Collectors.toList()); // Convert the stream to a list of DateRangeDTO objects
        dto.setDateRanges(dateRangeDTOs); // Set the date ranges in the BillingHistoryDTO object
        dto.setSkippedDates(legacyData.getSkippedDates()); // Set the skipped dates in the BillingHistoryDTO object

        return dto; // Return the BillingHistoryDTO object with the data
    }


    // ... other service methods as needed
}