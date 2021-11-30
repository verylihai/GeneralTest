package com.example.demo.controller;

import com.example.demo.utils.TimeHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HealthCheck {

    @RequestMapping("/ping")
    @ResponseBody
    public String ping() {
        System.out.println(TimeHelper.getCurrentTime() + "receive message...");
        return "pong";
    }
}
