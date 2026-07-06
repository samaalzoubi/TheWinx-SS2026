package com.winx.fleet.api.ui;

import com.winx.fleet.domain.repository.VehicleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeUiController {

    private final VehicleRepository vehicleRepository;

    public HomeUiController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("/ui")
    public String home(Model model) {
        model.addAttribute("stats", FleetStats.of(vehicleRepository.findAll()));
        return "home";
    }
}
