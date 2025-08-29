package org.example.pharmaproject.admin.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPageController {

    @GetMapping("/admin/main")
    public String mainPage() {
        return "main";
    }
}
