package com.example.demo.controller;

import com.example.demo.service.PubSubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PubSubController {

    @Autowired
    private PubSubService pubSubService;

    @RequestMapping("/sendMsg")
    @ResponseBody
    public String sendMsg(@RequestParam("msg") String msg) {
        try {
            pubSubService.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "msg has sended";
    }

    @RequestMapping("/getMsg")
    @ResponseBody
    public String getMsg() {
        return pubSubService.receiveMessage();
    }
}
