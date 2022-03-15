package com.jbsapp.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @PostMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/anonymous")
    public String anonymous() {
        return "anonymous";
    }
}
