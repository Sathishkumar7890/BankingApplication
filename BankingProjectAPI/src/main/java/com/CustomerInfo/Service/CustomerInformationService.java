package com.CustomerInfo.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.CustomerInfo.DTO.AccountListReqest;
import com.CustomerInfo.DTO.AccountListResponse;
import com.CustomerInfo.DTO.CreditCardRequest;
import com.CustomerInfo.DTO.CreditCardResponse;
import com.CustomerInfo.DTO.SetPreferredLanguageRequest;
import com.CustomerInfo.DTO.SetPreferredLanguageResponse;
import com.CustomerInfo.DTO.TpinRequest;
import com.CustomerInfo.DTO.TpinResponse;
import com.CustomerInfo.Model.CustomerAccountDetails;
import com.CustomerInfo.Model.CustomerCardDetails;
import com.CustomerInfo.Model.CustomerDetails;
import com.CustomerInfo.Model.TpinDetails;
import com.CustomerInfo.Repository.AccountDetailsRepo;
import com.CustomerInfo.Repository.CreditCardDetailsRepo;
import com.CustomerInfo.Repository.CustomerDetailsRepo;
import com.CustomerInfo.Repository.TpinDetailsRepo;
import com.CustomerInfo.Security.EncryptDecrypt;

import jakarta.transaction.Transactional;

@Service
public class CustomerInformationService {

	
	
	private static final Logger logger =
	        LoggerFactory.getLogger(CustomerInformationService.class);

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
	
	
	
	
	 public AccountListResponse getAccountListByRelationshipID(AccountListReqest Request) {

	        AccountListResponse response = new AccountListResponse();

	        
	        CustomerDetails customer =
	                customerRepo.findByRelationshipID(Request.getRelationshipID()).orElse(null);

	        if (customer == null) {
	        	logger.warn("No customer found with relationshipID ={}" , Request.getRelationshipID());
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
	        	logger.warn("No Accounts Found={}");
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

	            logger.info(" Customer AccountNumber={}", acc.getAccountNumber());
	            logger.info(" AccountType ={} " , acc.getAccountType());
	            logger.info(" Currency ={}" , acc.getCurrency());
	            logger.info(" ProductCode ={}" , acc.getProductCode());
	            logger.info(" Customer is Active ={}" , acc.getIsActive());
	            accounts.add(account);
	        }

	        
	        AccountListResponse.Data data = new AccountListResponse.Data();
	        data.setMobileNumber(customer.getMobileNumber());
	        data.setAccounts(accounts);

	        // Success response
	       
	        response.setStatus("Success");
	        logger.info("API Hit Successfully");
	        response.setResponseCode("0000");
	        response.setData(data);

	        return response;
	    }
	 
	 
	 @Transactional
		public ResponseEntity<TpinResponse> generateOrChangeTpin(TpinRequest request) {

		    TpinResponse response = new TpinResponse();

		    try {

		        /* ------------------ 1. READ INPUT ------------------ */

		        String action = request.getAction();
		        String relationshipId = request.getRelationshipID();
		        String isEncrypted = request.getIsEncrypted();
		        String inputTpin = request.getTpin();

		        /* ------------------ 2. BASIC VALIDATION ------------------ */

		        if (!"GENERATE".equalsIgnoreCase(action) &&
		            !"CHANGE".equalsIgnoreCase(action)) {

		            response.setResponseCode("1002");
		            logger.info(" Currency ={}");
		            response.setMessage("Invalid action");
		            logger.info(" Invalid action ={}" ,request.getAction() );
		            logger.info(" Response Code ={}" ,response.getResponseCode() );
		            return ResponseEntity.badRequest().body(response);
		            
		        }

		        if (relationshipId == null || relationshipId.trim().isEmpty()) {
		            response.setResponseCode("1005");
		            response.setMessage("Invalid Relationship ID");
		            logger.info("Invalid Relationship ID={}",request.getRelationshipID());
		            return ResponseEntity.badRequest().body(response);
		        }

		        /* ------------------ 3. CUSTOMER VALIDATION ------------------ */

		        Optional<CustomerDetails> customerOpt =
		                customerRepo.findByRelationshipID(relationshipId);

		        if (customerOpt.isEmpty()) {
		            response.setResponseCode("1005");
		       
		            response.setMessage("Invalid Relationship ID");
		            
		            logger.info("Invalid Relationship ID={}",request.getRelationshipID());
		            return ResponseEntity.badRequest().body(response);
		        }

		        CustomerDetails customer = customerOpt.get();

		        /* ------------------ 4. TPIN FORMAT FLAG ------------------ */

		        if (!"Y".equalsIgnoreCase(isEncrypted) &&
		            !"N".equalsIgnoreCase(isEncrypted)) {

		            response.setResponseCode("1006");
		            response.setMessage("Invalid TPIN format flag");
		           logger.info("Invalid TPIN format flag={}" ,request.getIsEncrypted());
		            return ResponseEntity.badRequest().body(response);
		        }

		        if (inputTpin == null || inputTpin.trim().isEmpty()) {
		            response.setResponseCode("1001");
		            response.setMessage("TPIN cannot be empty");
		            logger.info("TPIN cannot be empty={}" ,request.getTpin());
		            return ResponseEntity.badRequest().body(response);
		        }

		        

		        String plainTpin;

		        if ("Y".equalsIgnoreCase(isEncrypted)) {

		            
		            if (inputTpin.matches("\\d{4}")) {
		                response.setResponseCode("1007");
		                response.setMessage("Encrypted TPIN expected but plain TPIN received");
		                logger.info("IEncrypted TPIN expected but plain TPIN received={}" ,inputTpin);
		                return ResponseEntity.badRequest().body(response);
		            }

		            plainTpin = EncryptDecrypt.decrypt(inputTpin);

		            if (plainTpin == null || !plainTpin.matches("\\d{4}")) {
		                response.setResponseCode("1001");
		                response.setMessage("Decrypted TPIN must be exactly 4 digits");
		                logger.info("Decrypted TPIN must be exactly 4 digits={}" ,inputTpin);
		                logger.info("ResponseCode={}" ,response.getResponseCode());
		                return ResponseEntity.badRequest().body(response);
		            }

		        } else {

		            
		            if (!inputTpin.matches("\\d{4}")) {
		                response.setResponseCode("1008");
		                response.setMessage("Plain TPIN expected but encrypted TPIN received");
		                return ResponseEntity.badRequest().body(response);
		            }

		            plainTpin = inputTpin;
		        }

		        String encryptedTpin = EncryptDecrypt.encrypt(plainTpin);

		        Optional<TpinDetails> tpinOpt =
		                tpinRepository.findByRelationshipIdNative(relationshipId);

		        /* ------------------ 6. GENERATE TPIN ------------------ */

		        if ("GENERATE".equalsIgnoreCase(action)) {

		            if (customer.getHasTpin() == true) {
		                response.setResponseCode("1003");
		                response.setMessage("Failed! TPIN already exists.");
		                logger.info("TPIN Already Exit");
		                return ResponseEntity.badRequest().body(response);
		            }

		            TpinDetails tpin = new TpinDetails();
		            tpin.setRelationshipId(relationshipId);
		            tpin.setTpinHash(encryptedTpin);
		            tpinRepository.save(tpin);

		            
		            customer.setHasTpin(true);
		            customerRepo.save(customer);

		            response.setResponseCode("0000");
		            logger.info("Your TPIN has been successfully generated.");
		            response.setMessage("Your TPIN has been successfully generated.");
		            logger.info("Tpin Generated successfully={}",response.getResponseCode());
		            return ResponseEntity.ok(response);
		        }

		        if (customer.getHasTpin() == false) {
		            response.setResponseCode("1001");
		            response.setMessage("You do not have a TPIN to change.Please generate TPIN first.");
		            logger.info("Customer does not have Tpin.");
		            return ResponseEntity.badRequest().body(response);
		        }

		        if (tpinOpt.isEmpty()) {
		            response.setResponseCode("1004");
		            response.setMessage("TPIN details not found.");
		            logger.info("Tpin Not found");
		            return ResponseEntity.badRequest().body(response);
		        }

		        TpinDetails existingTpin = tpinOpt.get();
		        existingTpin.setTpinHash(encryptedTpin);
		        tpinRepository.save(existingTpin);

		        response.setResponseCode("0000");
		        response.setMessage("Your TPIN has been successfully changed.");
		        logger.info("Tpin Chnaged successfully ");
		        return ResponseEntity.ok(response);

		    } catch (Exception e) {
		        e.printStackTrace();
		        response.setResponseCode("9999");
		        response.setMessage("TPIN operation failed");
		        logger.info("Tpin Changed successfully ");
		        return ResponseEntity.internalServerError().body(response);
		    }
		}




	     
	    public CreditCardResponse fetchCards(CreditCardRequest request) { 
	    	
	                
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
		            	logger.warn("No credit cards found for relationshipID={}" ,  request.getRelationshipId());
		                response.setResponseCode("1001");
		                response.setMessage("CardDetails NotFound");
		                response.setCreditCards(null);
		                return response;
		            }

		            response.setResponseCode("0000");
		            response.setCreditCards(cards);
		            return response;

		        } catch (Exception e) {
		        	logger.error("Error fetching credit cards for relationshipID={}", 
		                     request != null ? request.getRelationshipId() : "null", e);
		            response.setResponseCode("1111");
		            response.setCreditCards(null);
		            return response;
		        }
		 }
	    
	    @Transactional
	    public SetPreferredLanguageResponse updatePreferredLanguage(SetPreferredLanguageRequest request) {

	        SetPreferredLanguageResponse response = new SetPreferredLanguageResponse();
	        
	        logger.info("preferredLanguage={}" , request.getPreferredLanguage());

	        String lang = request.getPreferredLanguage();

	        // Validate preferred language
	        if (!"HI".equalsIgnoreCase(lang) && !"EN".equalsIgnoreCase(lang)) {
	        	 logger.warn("Invalid preferred language={} " , lang);
	            response.setResponseCode("1002");
	            response.setMessage("Invalid preferred language");
	            return response;
	        }

	        // Update DB
	        int updatedRows = customerRepo.updatePreferredLanguageByRelationshipID(
	                request.getRelationshipID(), lang.toUpperCase());

	        if (updatedRows == 0) {
	            response.setResponseCode("1001"); // Customer not found
	            logger.warn("No customer found for relationshipID, Preferred language update failed={}", request.getRelationshipID());
	            response.setMessage("Customer not found");
	            return response;
	        }

	        // Success response
	        response.setResponseCode("0000");
	        logger.info("Preferred language updated successfully for relationshipID={}" ,request.getRelationshipID() );
	        response.setMessage("Preferred language updated successfully");
	        return response;
	    }

}
