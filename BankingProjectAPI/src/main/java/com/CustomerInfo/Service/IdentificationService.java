package com.CustomerInfo.Service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CustomerInfo.DTO.IdentifyCustomerRequest;
import com.CustomerInfo.DTO.IdentifyCustomerResponse;
import com.CustomerInfo.DTO.NrmnValidationRequest;
import com.CustomerInfo.DTO.NrmnValidationResponse;
import com.CustomerInfo.Model.CustomerAccountDetails;
import com.CustomerInfo.Model.CustomerCardDetails;
import com.CustomerInfo.Model.CustomerDetails;
import com.CustomerInfo.Repository.AccountDetailsRepo;
import com.CustomerInfo.Repository.CreditCardDetailsRepo;
import com.CustomerInfo.Repository.CustomerDetailsRepo;
import com.CustomerInfo.Repository.TpinDetailsRepo;


@Service
public class IdentificationService {

	
	
	private static final Logger logger =
	        LoggerFactory.getLogger(IdentificationService.class);

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
	
	
	
	
	
	
	public NrmnValidationResponse validateNrmnCustomer(NrmnValidationRequest request) {
	     
   	 logger.info("validateNrmnCustomer accountNumber= {}", request.getAccNum());

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
               logger.info("Creditcard found with relationshipID={}" ,relationshipId);
               if (card != null) {
                   relationshipId = card.getRelationshipId();
               }
    
           } else {
               response.setResponseCode("1001");
               logger.warn("Invalid account or card number length={}" , input.length());
               response.setMessage("Invalid account or card number");
               return response;
           }
    
          
           
    
         
           CustomerDetails customer =
                   customerRepo.findByRelationshipID(relationshipId)
                               .orElse(null);
    
           if (customer == null) {
               response.setResponseCode("1003");
               logger.warn("Customer not found with accountNumber={}" , input);
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
           logger.info("Customer Verified Successfully");
           logger.info("validateNrmnCustomer completed successfully for relationshipID:{} ", input);

           response.setData(data);
    
           return response;
       }
	
	
	 public IdentifyCustomerResponse identifyCustomer(IdentifyCustomerRequest request) {

	    	
	        IdentifyCustomerResponse response = new IdentifyCustomerResponse();
	        IdentifyCustomerResponse.Data data = new IdentifyCustomerResponse.Data();

	        
	        CustomerDetails customer = customerRepo
	                .findByMobileNumber(request.getMobileNumber())
	                .orElse(null);

	        if (customer == null) {
	        	logger.warn("Customer not found ");
	            response.setResponseCode("1001"); 
	            response.setMessage("Customer not found");
	            response.setData(null);
	            return response;
	        }

	        logger.info("Identify Customer found with relationshipID={}", customer.getRelationshipID());
	        List<CustomerAccountDetails> accounts =
	                accountRepo.findByRelationshipID(customer.getRelationshipID());
	        logger.info("Found customeraccounts={} ", 
	                accounts.size());

	        // Fetch Credit Cards using Relationship ID
	        List<CustomerCardDetails> cards =
	                creditCardRepo.findByRelationshipId(customer.getRelationshipID());

	        logger.info("Found  credit card details ={}", 
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

	        logger.info("identifyCustomer completed successfully = {}", customer.getRelationshipID());
	        return response;
	    }
  
}
