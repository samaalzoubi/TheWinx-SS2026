package com.winx.fleet.api.ui;

import com.winx.fleet.domain.model.VehicleType;
import com.winx.fleet.domain.service.VehicleAvailabilityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
public class SearchUiController {

    private final VehicleAvailabilityService availabilityService;

    public SearchUiController(VehicleAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/ui/search")
    public String search(@RequestParam(required = false) Double lat,
                          @RequestParam(required = false) Double lon,
                          @RequestParam(required = false) Double radiusKm,
                          @RequestParam(required = false) VehicleType type,
                          @RequestParam(required = false) BigDecimal maxPrice,
                          Model model) {

        model.addAttribute("types", VehicleType.values());
        model.addAttribute("lat", lat);
        model.addAttribute("lon", lon);
        model.addAttribute("radiusKm", radiusKm);
        model.addAttribute("type", type);
        model.addAttribute("maxPrice", maxPrice);

        if (lat != null && lon != null && radiusKm != null) {
            model.addAttribute("results", availabilityService.findAvailableNear(
                    lat, lon, radiusKm, Optional.ofNullable(type), Optional.ofNullable(maxPrice),
                    Optional.empty(), Optional.empty()));
        }

        return "search";
    }
}
