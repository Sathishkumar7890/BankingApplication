package com.OAuth.service;

import org.springframework.stereotype.Service;

@Service
public class ClientDetailsService {

    // Hardcoded client credentials for demo
    private static final String CLIENT_ID = "ivr_client";
    private static final String CLIENT_SECRET = "ivr_client_secret";

    public boolean validateClient(String clientId, String clientSecret, String grantType) {
        // Only supporting client_credentials grant type
        return CLIENT_ID.equals(clientId) &&
               CLIENT_SECRET.equals(clientSecret) &&
               "client_credentials".equals(grantType);
    }
}
