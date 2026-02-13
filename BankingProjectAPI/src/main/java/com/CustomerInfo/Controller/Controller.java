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
import com.CustomerInfo.DTO.IdentifyCustomerRequest;
import com.CustomerInfo.DTO.IdentifyCustomerResponse;
import com.CustomerInfo.DTO.NrmnValidationRequest;
import com.CustomerInfo.DTO.NrmnValidationResponse;
import com.CustomerInfo.DTO.SetPreferredLanguageRequest;
import com.CustomerInfo.DTO.SetPreferredLanguageResponse;
import com.CustomerInfo.DTO.TpinRequest;
import com.CustomerInfo.DTO.TpinResponse;
import com.CustomerInfo.OTP.GenerateOTPRequest;
import com.CustomerInfo.OTP.GenerateOTPResponse;
import com.CustomerInfo.OTP.ValidateOTPRequest;
import com.CustomerInfo.OTP.ValidateOTPResponse;
import com.CustomerInfo.Service.CustomerService;

@RestController
@RequestMapping("/Customer")
public class Controller {
	 
//	private static final Logger logger = LogManager.getLogger(Controller.class);
//	
//	
//	@Autowired
//    private CustomerService CustomerService;
//	
//	 @PostMapping("/identify")
//	    public IdentifyCustomerResponse identifyCustomer(@RequestBody IdentifyCustomerRequest request) {
//		    logger.info("PREVIOUS CONTROLLAR");
//	        logger.info("dentify called with request:", request);
//	        IdentifyCustomerResponse response = CustomerService.identifyCustomer(request);
//	        logger.info("identify response:", response);
//	        return response;
//	    }
//	    @PostMapping("/preferredLanguage")
//	    public SetPreferredLanguageResponse updatePreferredLanguage(@RequestBody SetPreferredLanguageRequest request) {
//	        logger.info("preferredLanguage called with request:", request);
//	        SetPreferredLanguageResponse response = CustomerService.updatePreferredLanguage(request);
//	        logger.info("preferredLanguage response: ", response);
//	        return response;
//	    }
//	    
//	    @PostMapping("/generateOtp")
//	    public GenerateOTPResponse generateOtp(@RequestBody GenerateOTPRequest request) {
//	        logger.info("generateOtp called with request: {}", request);
//	        GenerateOTPResponse response = CustomerService.generateOTP(request);
//	        logger.info("generateOtp response: {}", response);
//	        return response;
//	    }
//	    
//	    @PostMapping("/accounts")
//	    public AccountListResponse getAccountList(@RequestBody AccountListReqest request) {
//	        logger.info("accountsList called with request:", request);
//	        AccountListResponse response = CustomerService.getAccountListByRelationshipID(request);
//	        logger.info("accountsList response:", response);
//	        return response;
//	    }
//
//	    @PostMapping("/validateOtp")
//	    public ValidateOTPResponse validateOtp(@RequestBody ValidateOTPRequest request) {
//	        logger.info("validateOtp called with request:", request);
//	        ValidateOTPResponse response = CustomerService.validateOTP(request);
//	        logger.info("validateOtp response:", response);
//	        return response;
//	    }
//
//	    @PostMapping("/validate")
//	    public NrmnValidationResponse validate(@RequestBody NrmnValidationRequest request) {
//	        logger.info("validate called with request:", request);
//	        NrmnValidationResponse response = CustomerService.validateNrmnCustomer(request);
//	        logger.info("validate response: ", response);
//	        return response;
//	    }
//
//	    @PostMapping("/validateTpin")
//	    public ResponseEntity<TpinResponse> validateTpin(@RequestBody TpinRequest request) {
//	        logger.info("validateTpin called with request: ", request);
//	        TpinResponse response = CustomerService.validateTpin(request);
//	        logger.info("validateTpin response: ", response);
//	        return ResponseEntity.ok(response);
//	    }
//
//	    @PostMapping("/generate")
//	    public ResponseEntity<ResponseEntity<TpinResponse>> generateTpin(@RequestBody TpinRequest request) {
//	        logger.info("generate tpin called with request: {}", request);
//	        ResponseEntity<TpinResponse> response = CustomerService.generateOrChangeTpin(request);
//	        logger.info("generate tpin response: {}", response);
//	        return ResponseEntity.ok(response);
//	    }
//
//	    @PostMapping("/fetch")
//	    public ResponseEntity<CreditCardResponse> fetchCreditCards(@RequestBody CreditCardRequest request) {
//	        logger.info("credicardlist called with request: {}", request);
//	        CreditCardResponse response = CustomerService.fetchCards(request);
//	        logger.info("credicardlist response: {}", response);
//	        return ResponseEntity.ok(response);
//	    }
	
}