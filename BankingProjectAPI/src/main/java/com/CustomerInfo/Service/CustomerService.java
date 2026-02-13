package com.CustomerInfo.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.CustomerInfo.Controller.Controller;
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
import com.CustomerInfo.Model.CustomerAccountDetails;
import com.CustomerInfo.Model.CustomerCardDetails;
import com.CustomerInfo.Model.CustomerDetails;
import com.CustomerInfo.Model.TpinDetails;
import com.CustomerInfo.OTP.GenerateOTPRequest;
import com.CustomerInfo.OTP.GenerateOTPResponse;
import com.CustomerInfo.OTP.OTPEntry;
import com.CustomerInfo.OTP.ValidateOTPRequest;
import com.CustomerInfo.OTP.ValidateOTPResponse;
import com.CustomerInfo.Repository.AccountDetailsRepo;
import com.CustomerInfo.Repository.CreditCardDetailsRepo;
import com.CustomerInfo.Repository.CustomerDetailsRepo;
import com.CustomerInfo.Repository.TpinDetailsRepo;
import com.CustomerInfo.Security.EncryptDecrypt;


import jakarta.transaction.Transactional; 


@Service
public class CustomerService {
	
	private static final Logger logger =
	        LoggerFactory.getLogger(CustomerService.class);


    @Autowired
    private CustomerDetailsRepo customerRepo;

    @Autowired
    private AccountDetailsRepo accountRepo;

    @Autowired
    private CreditCardDetailsRepo creditCardRepo;
    
    @Autowired
    private TpinDetailsRepo tpinRepository;

    public List<CustomerDetails> getAll() {
        return customerRepo.findAll();
    }
    
    IdentifyCustomerResponse response = new IdentifyCustomerResponse();
    
    
    public IdentifyCustomerResponse identifyCustomer(IdentifyCustomerRequest request) {

    	logger.info("Customer called with mobileNumber:", request.getMobileNumber());
        IdentifyCustomerResponse response = new IdentifyCustomerResponse();
        IdentifyCustomerResponse.Data data = new IdentifyCustomerResponse.Data();

        
        CustomerDetails customer = customerRepo
                .findByMobileNumber(request.getMobileNumber())
                .orElse(null);

        if (customer == null) {
        	logger.warn("Customer not found for mobileNumber:", request.getMobileNumber());
            response.setResponseCode("1001"); 
            response.setMessage("Customer not found");
            response.setData(null);
            return response;
        }

        logger.info("Identify Customer found with relationshipID:", customer.getRelationshipID());
        List<CustomerAccountDetails> accounts =
                accountRepo.findByRelationshipID(customer.getRelationshipID());
        logger.info("Found customer accounts ", 
                accounts.size());

        // Fetch Credit Cards using Relationship ID
        List<CustomerCardDetails> cards =
                creditCardRepo.findByRelationshipId(customer.getRelationshipID());

        logger.info("Found  credit card details", 
                cards.size());
        
        
        data.setMobileNumber(customer.getMobileNumber());
        data.setRelationshipID(customer.getRelationshipID());
        data.setRMN(customer.getIsRMN());
        data.setPreferredLanguage(customer.getPreferredLanguage());
        data.setHasTPIN(customer.getHasTpin());

        // Prepare Products List
        List<IdentifyCustomerResponse.Product> products = new ArrayList<>();

        // Map Accounts → Products (AC)
        if (accounts != null && !accounts.isEmpty()) {
            for (CustomerAccountDetails acc : accounts) {

                IdentifyCustomerResponse.Product product = new IdentifyCustomerResponse.Product();

                product.setNumber(acc.getAccountNumber());
                product.setType("AC");
                product.setIsActive(acc.getIsActive());
                product.setAccountType(acc.getAccountType()); // ✅ ONLY for AC
                product.setProductCode(acc.getProductCode());

                // Safety: AC should not have CardType
                product.setCardType(null);

                products.add(product);
            }
        }

        // Map Credit Cards → Products (CC)
        if (cards != null && !cards.isEmpty()) {
            for (CustomerCardDetails card : cards) {

                IdentifyCustomerResponse.Product product = new IdentifyCustomerResponse.Product();

                product.setNumber(card.getCreditCardNumber());
                product.setType("CC");
                product.setIsActive(card.getStatus());

                // ❌ Card should NOT have accountType
                product.setAccountType(null);

                product.setCardType(card.getCreditCardType());
                product.setProductCode("CC" + card.getCcId());
                products.add(product);
            }
        }
        
        data.setProducts(products);

        
        response.setResponseCode("0000");
        response.setData(data);

        logger.info("identifyCustomer completed successfully", customer.getRelationshipID());
        return response;
    }
    
    @Transactional
    public SetPreferredLanguageResponse updatePreferredLanguage(SetPreferredLanguageRequest request) {

        SetPreferredLanguageResponse response = new SetPreferredLanguageResponse();
        
        logger.info("preferredLanguage: " + request.getPreferredLanguage());

        String lang = request.getPreferredLanguage();

        // Validate preferred language
        if (!"HI".equalsIgnoreCase(lang) && !"EN".equalsIgnoreCase(lang)) {
        	 logger.warn("Invalid preferred language " + lang);
            response.setResponseCode("1002");
            response.setMessage("Invalid preferred language");
            return response;
        }

        // Update DB
        int updatedRows = customerRepo.updatePreferredLanguageByRelationshipID(
                request.getRelationshipID(), lang.toUpperCase());

        if (updatedRows == 0) {
            response.setResponseCode("1001"); // Customer not found
            logger.warn("No customer found for relationshipID, Preferred language update failed.", request.getRelationshipID());
            response.setMessage("Customer not found");
            return response;
        }

        // Success response
        response.setResponseCode("0000");
        logger.info("Preferred language updated successfully for relationshipID: " +request.getRelationshipID(),"and update lang" + lang.toUpperCase() );
        response.setMessage("Preferred language updated successfully");
        return response;
    }
    
    // In-memory store for OTPs: key = otpReference, value = OTP details
    private Map<String, OTPEntry> otpStore = new ConcurrentHashMap<>();

    private final int OTP_EXPIRY_MINUTES = 5; // OTP valid for 5 minutes

    //  Generate OTP
    public GenerateOTPResponse generateOTP(GenerateOTPRequest request)  {
        GenerateOTPResponse response = new GenerateOTPResponse();
        
        logger.info("generateOTP called for mobileNumber: " + request.getMobileNumber());
        try {

        String otpReference = UUID.randomUUID().toString().substring(0, 9);
        String otpValue = String.valueOf((int)(Math.random() * 900000 + 100000)); // 6-digit OTP
        Instant expiryTime = Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES);

        // Store OTP in memory
        otpStore.put(otpReference, new OTPEntry(request.getMobileNumber(), otpValue, expiryTime));


        response.setResponseCode("0000");
        response.setOtpReference(otpReference);
        response.setMessage("OTP has been sent to your mobile number.");
        response.setExpiry(expiryTime.toString());
        
        logger.info("OTP generated successfully for mobileNumber: "+request.getMobileNumber() + "otpReference: "+otpReference);
        
        
        System.out.println("Generated OTP for " + request.getMobileNumber() + " : " + otpValue);
        } catch (Exception e) {
        	logger.error("Error generating OTP for mobileNumber: "+ request.getMobileNumber(), e);
            response.setResponseCode("9999");
            response.setMessage("Internal error while generating OTP.");
        }

        return response;
    }
    
    public ValidateOTPResponse validateOTP(ValidateOTPRequest request) {

        ValidateOTPResponse response = new ValidateOTPResponse();

        //  Check OTP reference
        OTPEntry otpEntry = otpStore.get(request.getOtpReference());
        if (otpEntry == null) {
        	 logger.warn("Invalid OTP reference:" +
                     request.getOtpReference());
            response.setResponseCode("1001");
            response.setMessage("Invalid OTP reference.");
            return response;
        }

        //  Check mobile number
        if (!otpEntry.getMobileNumber().equals(request.getMobileNumber())) {
        	logger.warn("Mobile number mismatch for otpReference:",request.getOtpReference());
            response.setResponseCode("1002");
            response.setMessage("Mobile number mismatch.");
            return response;
        }

        // Check expiry
        if (Instant.now().isAfter(otpEntry.getExpiryTime())) {
        	logger.info("OTP expired for otpReference:" +
                    request.getOtpReference());
            otpStore.remove(request.getOtpReference());
            response.setResponseCode("1003");
            response.setMessage("OTP has expired.");
            return response;	
        }

        // Get OTP value based on encryption flag
        String otpToValidate;
        
        try {
        if ("Y".equalsIgnoreCase(request.getIsEncrypted())) {
            otpToValidate = EncryptDecrypt.decrypt(request.getOtp());
            logger.debug("OTP decrypted successfully for otpReference: " + request.getOtpReference());
        } else {
            otpToValidate = request.getOtp();
        }
        } catch (Exception e) {
            response.setResponseCode("1005");
            logger.error("Failed to decrypt OTP for otpReference:", request.getOtpReference(), e);
            response.setMessage("Invalid encrypted OTP format.");
            return response;
        }

        // Validate OTP
        if (otpToValidate.equals(otpEntry.getOtpValue())) {
        	logger.info("OTP validated successfully for otpReference " +  request.getOtpReference() + "and mobileNumber" + 
                    request.getMobileNumber());
            response.setResponseCode("0000");
            response.setMessage("OTP validated successfully.");

        } else {
            response.setResponseCode("1004");
            logger.warn("Invalid OTP entered for otpReference :" +request.getOtpReference()  +"and mobileNumber: "  
                    + request.getMobileNumber());
            response.setMessage("Invalid OTP.");
        }

        return response;
    }

    
    public AccountListResponse getAccountListByRelationshipID(AccountListReqest Request) {

        AccountListResponse response = new AccountListResponse();

        
        CustomerDetails customer =
                customerRepo.findByRelationshipID(Request.getRelationshipID()).orElse(null);

        if (customer == null) {
        	logger.warn("No customer found " + "relationshipID" + Request.getRelationshipID());
            response.setMessage("No Accounts Found");
            response.setResponseCode("1001");
            response.setData(null);
            return response;
        }

        //  Fetch accounts
        List<CustomerAccountDetails> accountEntities =
                accountRepo.findByRelationshipID(Request.getRelationshipID());
        
        if (accountEntities == null) {
        	response.setMessage("No Accounts Found");
            response.setResponseCode("1001");
            response.setData(null);
            return response;
        }

        //  Map accounts
        List<AccountListResponse.Account> accounts = new ArrayList<>();

        for (CustomerAccountDetails acc : accountEntities) {

            AccountListResponse.Account account =
                    new AccountListResponse.Account();

            account.setAccountNumber(acc.getAccountNumber());
            account.setAccountType(acc.getAccountType());
            account.setCurrency(acc.getCurrency());
            account.setBalance(acc.getBalance());
            account.setProductCode(acc.getProductCode());
            account.setIsActive(acc.getIsActive());

            logger.info(" Customer AccountNumber " + acc.getAccountNumber());
            logger.info(" AccountType  " + acc.getAccountType());
            logger.info(" Currency  " + acc.getCurrency());
            logger.info(" ProductCode " + acc.getProductCode());
            logger.info(" Customer is Active " + acc.getIsActive());
            accounts.add(account);
        }

        
        AccountListResponse.Data data = new AccountListResponse.Data();
        data.setMobileNumber(customer.getMobileNumber());
        data.setAccounts(accounts);

        // Success response
        response.setStatus("Success");
        response.setResponseCode("0000");
        response.setData(data);

        return response;
    }

    public NrmnValidationResponse validateNrmnCustomer(NrmnValidationRequest request) {
     
    	 logger.info("validateNrmnCustomer accountNumber:", request.getAccNum());

            NrmnValidationResponse response = new NrmnValidationResponse();
     
            String input = request.getAccNum();
            String relationshipId = null;
            String accType = request.getAccType();
     
           
            if (input == null || input.trim().isEmpty()) {
                response.setResponseCode("1001");
                logger.info("Invalid account or card number provided");
                response.setMessage("Invalid account or card number");
                return response;
            }
     
            
            if ("ACCNO".equalsIgnoreCase(accType) && input.length() == 11) {

                CustomerAccountDetails account =
                        accountRepo.findByAccountNumber(input);

                if (account != null) {
                    relationshipId = account.getRelationshipID();
                }

                logger.info("Account lookup completed, relationshipId={}", relationshipId);
            }else if ("CC".equalsIgnoreCase(accType) && input.length() == 16) {
     
                CustomerCardDetails card =
                        creditCardRepo.findByCreditCardNumber(input);
                logger.info("Creditcard found with relationshipID:" + relationshipId);
                if (card != null) {
                    relationshipId = card.getRelationshipId();
                }
     
            } else {
                response.setResponseCode("1001");
                logger.warn("Invalid account or card number length:" + input.length());
                response.setMessage("Invalid account or card number");
                return response;
            }
     
           
            if (relationshipId == null) {
            	logger.warn("Relationship ID not found for input");
                response.setResponseCode("1002");
                response.setMessage("Relationship ID not found");
                return response;
            }
     
          
            CustomerDetails customer =
                    customerRepo.findByRelationshipID(relationshipId)
                                .orElse(null);
     
            if (customer == null) {
                response.setResponseCode("1003");
                logger.warn("Customer not found for relationshipID " + relationshipId);
                response.setMessage("Customer not found");
                return response;
            }
     
            
            List<CustomerAccountDetails> accounts =
                    accountRepo.findByRelationshipID(relationshipId);
     
            List<CustomerCardDetails> cards =
                    creditCardRepo.findByRelationshipId(relationshipId);
     
            
            NrmnValidationResponse.Data data = new NrmnValidationResponse.Data();
            data.setRelationshipID(relationshipId);
            data.setMobileNumber(customer.getMobileNumber());
     
            List<NrmnValidationResponse.Product> products = new ArrayList<>();
     
            
            if (accounts != null) {
                for (CustomerAccountDetails acc : accounts) {
                    NrmnValidationResponse.Product product =
                            new NrmnValidationResponse.Product();
     
                    product.setNumber(acc.getAccountNumber());
                    product.setType("AC");
                    product.setIsActive(acc.getIsActive());
                    product.setAccountType(acc.getAccountType());
                    product.setProductCode(acc.getProductCode());
                    product.setCardType(null);
     
                    logger.info(" Customer AccountNumber " + acc.getAccountNumber());
                    logger.info(" AccountType  " + acc.getAccountType());
                    logger.info(" Customer is Active " + acc.getIsActive());
                    
                    products.add(product);
                }
            }
     
            
            if (cards != null) {
                for (CustomerCardDetails card : cards) {
                    NrmnValidationResponse.Product product =
                            new NrmnValidationResponse.Product();
     
                    product.setNumber(card.getCreditCardNumber());
                    product.setType("CC");
                    product.setIsActive(card.getStatus());
                    product.setCardType(card.getCreditCardType());
                    product.setProductCode(card.getProductCode());
                    products.add(product);
                }
            }
     
            data.setProducts(products);
     
            response.setResponseCode("0000");
            response.setMessage("Customer validated successfully");
            logger.info("validateNrmnCustomer completed successfully for relationshipID: "+ input);

            response.setData(data);
     
            return response;
        }
   
   	
    public TpinResponse validateTpin(TpinRequest request)  {
    	logger.info("validateTpin called for relationshipID: " + request.getRelationshipID());
        TpinResponse response = new TpinResponse();
 
        Optional<TpinDetails> tpinData = tpinRepository.findByRelationshipIdNative(request.getRelationshipID());
        if (!tpinData.isPresent()) {
            response.setResponseCode("1002");
            logger.info("Relationship ID not found: "+ request.getRelationshipID());
            response.setMessage("Relationship ID not found");
            return response;
        }
 
        TpinDetails tpin = tpinData.get();
 
  
        String tpinToValidate;
 
        try {
            if ("Y".equalsIgnoreCase(request.getIsEncrypted())) {
                
                tpinToValidate = EncryptDecrypt.decrypt(request.getTpin());
                logger.info("TPIN decrypted successfully for relationshipID: " + request.getRelationshipID());
                System.out.println("decrypted :"+tpinToValidate);
            } else {
                
                tpinToValidate = request.getTpin();
            }
        } catch (Exception e) {
            response.setResponseCode("1005");
            logger.error("Failed to decrypt incoming TPIN for relationshipID: " + request.getRelationshipID(), e);
            response.setMessage("Invalid encrypted TPIN format");
            return response;
        }
        
        String TPIN_DB;
		try {
			TPIN_DB = EncryptDecrypt.decrypt(tpin.getTpinHash());
	        
			logger.info("Stored TPIN hash decrypted successfully for relationshipID: " + request.getRelationshipID());
 
	        System.out.println("decrypted :"+tpinToValidate +" Hash TPIN :"+tpin.getTpinHash()+"  "+TPIN_DB);
	
	        if (tpinToValidate.equalsIgnoreCase(TPIN_DB)) {
	        	logger.info("TPIN validated successfully for relationshipID: " + request.getRelationshipID());
	            response.setResponseCode("0000");
	            response.setMessage("TPIN verified successfully");
	        } else {
	        	
	            response.setResponseCode("1001");
	            logger.info("Invalid TPIN entered for relationshipID: "+ request.getRelationshipID());
	            response.setMessage("Invalid TPIN");
	        }
		} catch (Exception e) {
			logger.error("Failed to decrypt stored TPIN hash for relationshipID: "+ request.getRelationshipID(), e);
			e.printStackTrace();
		}
 
        return response;
    } 
    private TpinResponse build(String code, String msg) {
        TpinResponse response = new TpinResponse();
        response.setResponseCode(code);
        response.setMessage(msg);
        return response;
    }

 
    @Transactional
    public TpinResponse generateOrChangeTpin(TpinRequest request) {

        try {

            String relationshipId = request.getRelationshipID();

            if (relationshipId == null || relationshipId.trim().isEmpty()) {
                return build("1005", "Invalid Relationship ID");
            }

            relationshipId = relationshipId.trim();

            Optional<CustomerDetails> customerOpt =
                    customerRepo.findByRelationshipId(relationshipId);

            if (customerOpt.isEmpty()) {
                return build("1005", "Invalid Relationship ID");
            }

            CustomerDetails customer = customerOpt.get();

            String action = request.getAction();
            String inputTpin = request.getTpin();

            if (!"GENERATE".equalsIgnoreCase(action) &&
                !"CHANGE".equalsIgnoreCase(action)) {

                return build("1002", "Invalid action");
            }

            if (inputTpin == null || !inputTpin.matches("\\d{4}")) {
                return build("1001", "TPIN must be exactly 4 digits");
            }

            String encryptedTpin = EncryptDecrypt.encrypt(inputTpin);

            Optional<TpinDetails> tpinOpt =
                    tpinRepository.findByRelationshipIdNative(relationshipId);

            /* -------- GENERATE -------- */

            if ("GENERATE".equalsIgnoreCase(action)) {

                if (customer.getHasTpin()) {
                    return build("1003", "Failed! TPIN already exists.");
                }

                TpinDetails tpin = new TpinDetails();
                tpin.setRelationshipId(relationshipId);
                tpin.setTpinHash(encryptedTpin);

                tpinRepository.save(tpin);

                customer.setHasTpin(true);
                customerRepo.save(customer);

                return build("0000",
                        "Your TPIN has been successfully generated.");
            }

            /* -------- CHANGE -------- */

            if (!customer.getHasTpin()) {
                return build("1010", "Generate TPIN first.");
            }

            if (tpinOpt.isEmpty()) {
                return build("1004", "TPIN details not found.");
            }

            TpinDetails existingTpin = tpinOpt.get();
            existingTpin.setTpinHash(encryptedTpin);
            tpinRepository.save(existingTpin);

            return build("0000",
                    "Your TPIN has been successfully changed.");

        } catch (Exception e) {

            // ✅ Replace printStackTrace with logger if available
            e.printStackTrace();

            return build("9999", "TPIN operation failed");
        }
    }

	
    public CreditCardResponse fetchCards(CreditCardRequest request) {
    	 
    	logger.info("fetchCards called for relationshipID: ", 
                request != null ? request.getRelationshipId() : "null");
	        CreditCardResponse response = new CreditCardResponse();

	        try {
	            
	            if (request == null || request.getRelationshipId() == null
	                    || request.getRelationshipId().isEmpty()) {
	            	logger.info("Invalid request or missing relationshipID");
	                response.setResponseCode("1002");
	                response.setMessage("RelationshipId NotFound");
	                response.setCreditCards(null);
	                return response;
	            }

	            List<CustomerCardDetails> cards =
	            		creditCardRepo.findByRelationshipId(request.getRelationshipId());

	            
	            if (cards == null || cards.isEmpty()) {
	            	logger.warn("No credit cards found for relationshipID: {}" +  request.getRelationshipId());
	                response.setResponseCode("1001");
	                response.setMessage("CardDetails NotFound");
	                response.setCreditCards(null);
	                return response;
	            }

	            response.setResponseCode("0000");
	            response.setCreditCards(cards);
	            return response;

	        } catch (Exception e) {
	        	logger.error("Error fetching credit cards for relationshipID: {}", 
	                     request != null ? request.getRelationshipId() : "null", e);
	            response.setResponseCode("1111");
	            response.setCreditCards(null);
	            return response;
	        }
	 }

   	 
   	 
}
