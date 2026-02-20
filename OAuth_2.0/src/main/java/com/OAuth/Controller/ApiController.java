package com.OAuth.Controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.OAuth.Config.SpringConfig;

@RestController
public class ApiController {
	private static final Logger logger = LogManager.getLogger(ApiController.class);
	
    @GetMapping("/api/test")
    public String testApi() {
        return "Success! You accessed a secured API ✅";
    }
}
