/**
 * Copyright 2021 Metro Bank. All rights reserved.
 */
package com.metrobank.communicationhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket registrationAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(PathSelectors.any())
                .apis(RequestHandlerSelectors.basePackage("com.metrobank.communicationhub.controller"))
                .build()
                .apiInfo(metaData())
                .useDefaultResponseMessages(false);
    }

    private ApiInfo metaData() {
        return new ApiInfo(
                "",
                "Communication Hub REST Proxy",
                "1.0",
                "Terms of service",
                new Contact("Communication Hub Support", "https://www.metrobankonline.co.uk/", ""),
                "All rights reserved",
                "https://www.metrobankonline.co.uk/",
                Collections.emptyList());
    }
}
