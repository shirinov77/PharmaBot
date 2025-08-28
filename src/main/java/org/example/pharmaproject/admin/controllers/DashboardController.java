package org.example.pharmaproject.admin.controllers;

import org.example.pharmaproject.services.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "Boshqaruv paneli");
        model.addAttribute("totalUsers", dashboardService.getTotalUsers());
        model.addAttribute("totalOrders", dashboardService.getTotalOrders());
        model.addAttribute("totalProducts", dashboardService.getTotalProducts());
        model.addAttribute("totalRevenue", dashboardService.getTotalRevenue());
        return "dashboard";
    }
}
