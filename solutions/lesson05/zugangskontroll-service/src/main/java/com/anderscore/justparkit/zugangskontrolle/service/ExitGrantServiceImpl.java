package com.anderscore.justparkit.zugangskontrolle.service;

import com.anderscore.justparkit.zugangskontrolle.client.PaymentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExitGrantServiceImpl implements ExitGrantService {

    private static final Logger logger = LoggerFactory.getLogger(ExitGrantServiceImpl.class);

    @Autowired
    private CircuitBreakerFactory circuitBreakerFactory;
    @Autowired
    private PaymentClient paymentClient;

    @Override
    public boolean isExitGranted(Long ticketId) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("exitGrantCircuitBreaker");
        return circuitBreaker.run(() -> paymentClient.isPaid(ticketId), throwable -> fallback(throwable, ticketId));
    }

    private boolean fallback(Throwable t, Long ticketId){
        logger.error("Technical error while checking payment. Granting exit for ticket {}.", ticketId, t);
        return true;
    }
}