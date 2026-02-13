package com.CustomerInfo.Controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CustomerInfo.DTO.IdentifyCustomerRequest;
import com.CustomerInfo.DTO.IdentifyCustomerResponse;
import com.CustomerInfo.DTO.NrmnValidationRequest;
import com.CustomerInfo.DTO.NrmnValidationResponse;
import com.CustomerInfo.Service.CustomerService;
import com.CustomerInfo.Service.IdentificationService;

@RestController
@RequestMapping("/Identification")
public class IdentificationControllar {

	
	private static final Logger logger =
	        LoggerFactory.getLogger(IdentificationControllar.class);

	
	
	
	@Autowired
    private IdentificationService CustomerService;
	
	
	 @PostMapping("/validate")
	 
	    public NrmnValidationResponse validate(@RequestBody NrmnValidationRequest request) {
		 logger.info("NRMN API HIT REQUEST = {}", request.toString());
		
	        NrmnValidationResponse response = CustomerService.validateNrmnCustomer(request);
	        logger.info("NRMN API CALLED");
	        return response;
	    }
	 
	 @PostMapping("/identify")
	    public IdentifyCustomerResponse identifyCustomer(@RequestBody IdentifyCustomerRequest request) {
	        logger.info("dentify called with request:", request);
	        IdentifyCustomerResponse response = CustomerService.identifyCustomer(request);
	        logger.info("identify response:", response);
	        return response;
	    }
	 
	
}
