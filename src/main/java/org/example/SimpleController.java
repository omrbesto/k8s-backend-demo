package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot on Kubernetes!";
    }

    @GetMapping("/ping")
    public String ping() {
        return "ping";
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }
}