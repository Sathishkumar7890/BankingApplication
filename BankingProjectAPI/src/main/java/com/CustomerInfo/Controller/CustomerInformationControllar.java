package com.CustomerInfo.Controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CustomerInfo.DTO.AccountListReqest;
import com.CustomerInfo.DTO.AccountListResponse;
import com.CustomerInfo.DTO.CreditCardRequest;
import com.CustomerInfo.DTO.CreditCardResponse;
import com.CustomerInfo.DTO.SetPreferredLanguageRequest;
import com.CustomerInfo.DTO.SetPreferredLanguageResponse;
import com.CustomerInfo.DTO.TpinRequest;
import com.CustomerInfo.DTO.TpinResponse;
import com.CustomerInfo.Service.AuthenticationService;
import com.CustomerInfo.Service.CustomerInformationService;
import com.CustomerInfo.Service.CustomerService;

@RestController
@RequestMapping("/CustomerInformation")
public class CustomerInformationControllar {

	private static final Logger logger = LogManager.getLogger(CustomerInformationControllar.class);
	
	
	static {
	    logger.info("Customer Information Controller Loaded");
	}
	
	@Autowired
    private CustomerService CustomerService;
	
	
	
	
	
	
	
	   @PostMapping("/preferredLanguage")
	    public SetPreferredLanguageResponse updatePreferredLanguage(@RequestBody SetPreferredLanguageRequest request) {
	        logger.info("preferredLanguage called with request:", request);
	        SetPreferredLanguageResponse response = CustomerService.updatePreferredLanguage(request);
	        logger.info("preferredLanguage response: ", response);
	        return response;
	    }
	   
	    @PostMapping("/accounts")
	    public AccountListResponse getAccountList(@RequestBody AccountListReqest request) {
	        logger.info("accountsList called with request:", request);
	        AccountListResponse response = CustomerService.getAccountListByRelationshipID(request);
	        logger.info("accountsList response:", response);
	        return response;
	    }
	    
	    @PostMapping("/generate")
	    public TpinResponse generateTpin(@RequestBody TpinRequest request) {
	        logger.info("generate tpin called with request: {}", request);

	        TpinResponse response = CustomerService.generateOrChangeTpin(request);

	        logger.info("generate tpin response: {}", response);

	        return response; // Spring will convert it to JSON automatically
	    }

	    @PostMapping("/fetch")
	    public ResponseEntity<CreditCardResponse> fetchCreditCards(@RequestBody CreditCardRequest request) {
	        logger.info("credicardlist called with request: {}", request);
	        CreditCardResponse response = CustomerService.fetchCards(request);
	        logger.info("credicardlist response: {}", response);
	        return ResponseEntity.ok(response);
	    }
	
}
