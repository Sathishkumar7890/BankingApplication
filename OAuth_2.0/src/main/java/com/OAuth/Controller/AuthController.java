package com.OAuth.Controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.OAuth.DTO.TokenResponse;
import com.OAuth.Security.JwtTokenProvider;

@RestController
@RequestMapping("/oauth")
public class AuthController {

	private static final Logger logger = LogManager.getLogger(AuthController.class);
    private final JwtTokenProvider tokenProvider;

    public AuthController(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/token")
    public TokenResponse getToken(
            @RequestParam String client_id,
            @RequestParam String client_secret,
            @RequestParam String grant_type) {

        // ✅ Dummy check (you can accept anything for testing)
        if (!"client_credentials".equals(grant_type)) {
        	 logger.info("Invalid client_credentials ");
            throw new IllegalArgumentException("Only client_credentials supported");
        }

        // Generate JWT token for this client_id
        logger.info("Token generated successfully ");
        String token = tokenProvider.generateToken(client_id);

        return new TokenResponse(token);
    }
}
