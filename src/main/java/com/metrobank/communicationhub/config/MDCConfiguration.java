/** Copyright 2021 Metro Bank. All rights reserved. */
package com.metrobank.communicationhub.config;

import com.metrobank.commons.utils.UniqueIdGenerator;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class MDCConfiguration extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String uniqueID = Long.toString(UniqueIdGenerator.generateId());
        request.setAttribute("generatedId", uniqueID);
        MDC.put("uuid", uniqueID);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("uuid");
        }
    }
}
