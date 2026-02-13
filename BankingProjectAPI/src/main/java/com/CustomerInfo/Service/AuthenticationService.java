package com.CustomerInfo.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.CustomerInfo.DTO.TpinRequest;
import com.CustomerInfo.DTO.TpinResponse;
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


@Service
public class AuthenticationService {

	
	private static final Logger logger =
	        LoggerFactory.getLogger(AuthenticationService.class);

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
	
	
	
	 // In-memory store for OTPs: key = otpReference, value = OTP details
    private Map<String, OTPEntry> otpStore = new ConcurrentHashMap<>();

    private final int OTP_EXPIRY_MINUTES = 5; // OTP valid for 5 minutes

    //  Generate OTP
    public GenerateOTPResponse generateOTP(GenerateOTPRequest request)  {
        GenerateOTPResponse response = new GenerateOTPResponse();
        
       
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
        
        logger.info("OTP generated successfully, otpReference={}", otpReference);

        
        
        System.out.println("Generated OTP for " + request.getMobileNumber() + " : " + otpValue);
        } catch (Exception e) {
        	logger.error("Error generating OTP for mobileNumber= {} ", e);
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
        	 logger.warn("Invalid OTP reference={}" ,
                     request.getOtpReference());
            response.setResponseCode("1001");
            response.setMessage("Invalid OTP reference.");
            return response;
        }

        //  Check mobile number
        if (!otpEntry.getMobileNumber().equals(request.getMobileNumber())) {
        	logger.warn("Mobile number mismatch for otpReference={}",request.getOtpReference());
            response.setResponseCode("1002");
            response.setMessage("Mobile number mismatch.");
            return response;
        }

        // Check expiry
        if (Instant.now().isAfter(otpEntry.getExpiryTime())) {
        	logger.info("OTP expired for otpReference={}" ,
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
            logger.debug("OTP decrypted successfully for otpReference ={}" , request.getOtpReference());
        } else {
            otpToValidate = request.getOtp();
        }
        } catch (Exception e) {
            response.setResponseCode("1005");
            logger.error("Failed to decrypt OTP for otpReference={}", request.getOtpReference(), e);
            response.setMessage("Invalid encrypted OTP format.");
            return response;
        }

        // Validate OTP
        if (otpToValidate.equals(otpEntry.getOtpValue())) {
        	logger.info("OTP validated successfully for otpReference ={} " , request.getOtpReference() 
                   );
            response.setResponseCode("0000");
            response.setMessage("OTP validated successfully.");

        } else {
            response.setResponseCode("1004");
            logger.warn("Invalid OTP entered for otpReference ={}",request.getOtpReference()  
                   );
            response.setMessage("Invalid OTP.");
        }

        return response;
    }
    
    public TpinResponse validateTpin(TpinRequest request)  {
    	logger.info("validateTpin called for relationshipID={} ",request.getRelationshipID());
        TpinResponse response = new TpinResponse();
 
        Optional<TpinDetails> tpinData = tpinRepository.findByRelationshipIdNative(request.getRelationshipID());
        if (!tpinData.isPresent()) {
            response.setResponseCode("1002");
            logger.info("Relationship ID not found={}",request.getRelationshipID());
            response.setMessage("Relationship ID not found");
            return response;
        }
 
        TpinDetails tpin = tpinData.get();
 
  
        String tpinToValidate;
 
        try {
            if ("Y".equalsIgnoreCase(request.getIsEncrypted())) {
                
                tpinToValidate = EncryptDecrypt.decrypt(request.getTpin());
                logger.info("TPIN decrypted successfully for relationshipID ={}" ,request.getRelationshipID());
                System.out.println("decrypted :"+tpinToValidate);
            } else {
                
                tpinToValidate = request.getTpin();
            }
        } catch (Exception e) {
            response.setResponseCode("1005");
            logger.error("Failed to decrypt incoming TPIN for relationshipID={}" ,request.getRelationshipID(), e);
            response.setMessage("Invalid encrypted TPIN format");
            return response;
        }
        
        String TPIN_DB;
		try {
			TPIN_DB = EncryptDecrypt.decrypt(tpin.getTpinHash());
	        
			logger.info("Stored TPIN hash decrypted successfully for relationshipID={}" , request.getRelationshipID());
 

	
	        if (tpinToValidate.equalsIgnoreCase(TPIN_DB)) {
	        	logger.info("TPIN validated successfully for relationshipID={} " , request.getRelationshipID());
	            response.setResponseCode("0000");
	            response.setMessage("TPIN verified successfully");
	        } else {
	        	
	            response.setResponseCode("1001");
	            logger.info("Invalid TPIN entered for relationshipID={}", request.getRelationshipID());
	            response.setMessage("Invalid TPIN");
	        }
		} catch (Exception e) {
			logger.error("Failed to decrypt stored TPIN hash for relationshipID={}", request.getRelationshipID(), e);
			e.printStackTrace();
		}
 
        return response;
    } 
 
}
