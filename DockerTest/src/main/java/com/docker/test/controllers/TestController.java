package com.docker.test.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {
    @GetMapping("/")
    public Map<String, Object> getValues() {
        Map<String, Object> map = new HashMap<>();
        map.put("message", "Java API is working fine!");
        map.put("languages", Arrays.asList("Java", "Python", "JavaScript"));
        map.put("code", 2345);
        return map;
    }
}
