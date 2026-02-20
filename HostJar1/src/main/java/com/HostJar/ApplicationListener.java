package com.HostJar;


import java.io.File;
import java.util.Date;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
 

 
@Component

public class ApplicationListener {
 
    private static final Logger logger = LogManager.getLogger(ApplicationListener.class);
 
    @Autowired

    private static Environment env;
 
    @EventListener(ApplicationReadyEvent.class)

    public static void initializeLogging() {

        try {

            String configPath = env.getProperty("LOG4J_CONFIG_PATH");
 
            if (configPath == null || configPath.isEmpty()) {

                logger.warn("LOG4J_CONFIG_PATH not set. Using default log4j2.xml from classpath.");

                return;

            }
 
            File file = new File(configPath);

            if (!file.exists()) {

                logger.error("Log4j2 config file not found at: {}", configPath);

                return;

            }
 
            LoggerContext context = (LoggerContext) LogManager.getContext(false);

            context.setConfigLocation(file.toURI());
 
            logger.info("Log4j2 configuration loaded from {} at {}", configPath, new Date());
 
        } catch (Exception ex) {

            logger.error("Error initializing logging", ex);

        }

    }
    



 
}