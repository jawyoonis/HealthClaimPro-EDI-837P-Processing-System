package com.billing.webapp.runner;

import com.billing.webapp.service.LegacyDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestRunner implements CommandLineRunner {

    @Autowired
    private LegacyDataService legacyDataService;
    @Override
    public void run(String... args) throws Exception {

    }


}
