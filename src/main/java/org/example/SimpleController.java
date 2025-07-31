package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleController {
    private static final Logger logger = LoggerFactory.getLogger(SimpleController.class);

    @GetMapping("/hello")
    public String hello() {
        logger.info("Hello endpoint called");
        return "Hello from Spring Boot on Kubernetes!";
    }

    @GetMapping("/ping")
    public String ping() {
        logger.debug("Ping endpoint called");
        return "ping";
    }

    @GetMapping("/test")
    public String test(){
        logger.info("Test endpoint called");
        return "test";
    }

    @GetMapping("/simulate-error") // เปลี่ยนชื่อ Path
    public String simulateError() { // เปลี่ยนชื่อ method ให้ตรงกับ Path
        logger.error("Simulating error for testing");
        throw new RuntimeException("Simulated error for testing");
    }

    @GetMapping("/logs")
    public String generateLogs() {
        logger.trace("TRACE level log");
        logger.debug("DEBUG level log");
        logger.info("INFO level log");
        logger.warn("WARN level log");
        logger.error("ERROR level log");
        return "Logs generated at all levels";
    }
}