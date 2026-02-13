package com.CustomerInfo.Controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CustomerInfo.DTO.TpinRequest;
import com.CustomerInfo.DTO.TpinResponse;
import com.CustomerInfo.OTP.GenerateOTPRequest;
import com.CustomerInfo.OTP.GenerateOTPResponse;
import com.CustomerInfo.OTP.ValidateOTPRequest;
import com.CustomerInfo.OTP.ValidateOTPResponse;
import com.CustomerInfo.Service.AuthenticationService;
import com.CustomerInfo.Service.CustomerService;


@RestController
@RequestMapping("/Authentication")
public class AuthenticationControllar {

	private static final Logger logger =
	        LogManager.getLogger(AuthenticationControllar.class);

	
	static {
	    logger.info("AuthenticationControllar Controller Loaded");
	}
	
	@Autowired
    private AuthenticationService CustomerService;
	
	
	
	 @PostMapping("/generateOtp")
	    public GenerateOTPResponse generateOtp(@RequestBody GenerateOTPRequest request) {
	        logger.info("generateOtp called with request: {}", request);
	        GenerateOTPResponse response = CustomerService.generateOTP(request);
	        logger.info("generateOtp response: {}", response);
	        return response;
	    }
	 
	  @PostMapping("/validateOtp")
	    public ValidateOTPResponse validateOtp(@RequestBody ValidateOTPRequest request) {
	        logger.info("validateOtp called with request:", request);
	        ValidateOTPResponse response = CustomerService.validateOTP(request);
	        logger.info("validateOtp response:", response);
	        return response;
	    }
	  
	   @PostMapping("/validateTpin")
	    public ResponseEntity<TpinResponse> validateTpin(@RequestBody TpinRequest request) {
	        logger.info("validateTpin called with request: ", request);
	        TpinResponse response = CustomerService.validateTpin(request);
	        logger.info("validateTpin response: ", response);
	        return ResponseEntity.ok(response);
	    }
	    
}



