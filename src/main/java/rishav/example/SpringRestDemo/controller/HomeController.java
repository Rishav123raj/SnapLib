package rishav.example.SpringRestDemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String demo() {
        return "Rishav Raj Bhagat";
    }
}
