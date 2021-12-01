/**
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.communicationhub;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log4j2
public class ComhubRestProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComhubRestProxyApplication.class, args);
        log.info("Started ComhubRestProxyApplication.");
    }
}
