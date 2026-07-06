package com.winx.fleet.api.ui;

import com.winx.fleet.domain.model.Vehicle;
import com.winx.fleet.domain.model.VehicleStatus;
import com.winx.fleet.domain.model.VehicleType;
import com.winx.fleet.domain.repository.VehicleRepository;
import com.winx.fleet.domain.service.VehicleAvailabilityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * UI-only view across the ENTIRE fleet (every provider), used to exercise
 * the relaxed GET /api/vehicles (no providerId) path interactively. This
 * reads straight from the repository/domain services in-process - it never
 * calls the REST layer of this same service over HTTP.
 */
@Controller
public class BrowseUiController {

    private final VehicleRepository vehicleRepository;
    private final VehicleAvailabilityService availabilityService;

    public BrowseUiController(VehicleRepository vehicleRepository, VehicleAvailabilityService availabilityService) {
        this.vehicleRepository = vehicleRepository;
        this.availabilityService = availabilityService;
    }

    @GetMapping("/ui/browse")
    public String browse(@RequestParam(required = false) VehicleType type,
                          @RequestParam(required = false) VehicleStatus status,
                          Model model) {
        List<Vehicle> all = vehicleRepository.findAll();

        List<Vehicle> filtered = all.stream()
                .filter(v -> type == null || v.getType() == type)
                .filter(v -> status == null || v.getStatus() == status)
                .toList();

        model.addAttribute("vehicles", filtered);
        model.addAttribute("stats", FleetStats.of(all));
        model.addAttribute("types", VehicleType.values());
        model.addAttribute("statuses", VehicleStatus.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);
        return "browse";
    }

    @PostMapping("/ui/vehicles/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                                @RequestParam(required = false) VehicleType type,
                                @RequestParam(required = false) VehicleStatus status) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow();
        VehicleStatus next = vehicle.getStatus() == VehicleStatus.AVAILABLE
                ? VehicleStatus.BOOKED
                : VehicleStatus.AVAILABLE;
        // Same domain service call the PATCH /api/vehicles/{id}/status endpoint uses.
        availabilityService.updateStatus(id, next);

        UriComponentsBuilder redirect = UriComponentsBuilder.fromPath("/ui/browse");
        if (type != null) {
            redirect.queryParam("type", type);
        }
        if (status != null) {
            redirect.queryParam("status", status);
        }
        return "redirect:" + redirect.build().toUriString();
    }
}
