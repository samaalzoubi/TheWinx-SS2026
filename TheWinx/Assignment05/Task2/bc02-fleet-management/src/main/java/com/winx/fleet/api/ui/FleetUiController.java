package com.winx.fleet.api.ui;

import com.winx.fleet.domain.model.BillingModel;
import com.winx.fleet.domain.model.PricingPolicy;
import com.winx.fleet.domain.model.UsageRestrictions;
import com.winx.fleet.domain.model.Vehicle;
import com.winx.fleet.domain.model.VehicleLocation;
import com.winx.fleet.domain.model.VehicleType;
import com.winx.fleet.domain.repository.VehicleRepository;
import com.winx.fleet.domain.service.FleetStatusService;
import com.winx.fleet.domain.service.VehicleRegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class FleetUiController {

    private final VehicleRegistrationService registrationService;
    private final FleetStatusService fleetStatusService;
    private final VehicleRepository vehicleRepository;

    public FleetUiController(VehicleRegistrationService registrationService,
                              FleetStatusService fleetStatusService,
                              VehicleRepository vehicleRepository) {
        this.registrationService = registrationService;
        this.fleetStatusService = fleetStatusService;
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("/ui/provider/{providerId}/fleet")
    public String fleet(@PathVariable Long providerId, Model model) {
        model.addAttribute("providerId", providerId);
        model.addAttribute("vehicles", fleetStatusService.getFleetOverview(providerId));
        model.addAttribute("types", VehicleType.values());
        model.addAttribute("billingModels", BillingModel.values());
        return "fleet";
    }

    @PostMapping("/ui/provider/{providerId}/fleet")
    public String createVehicle(@PathVariable Long providerId,
                                 @RequestParam VehicleType type,
                                 @RequestParam(required = false) String description,
                                 @RequestParam BigDecimal pricePerUnit,
                                 @RequestParam BillingModel billingModel,
                                 @RequestParam(required = false) Integer maxDurationMinutes,
                                 @RequestParam(required = false) Integer maxKilometers,
                                 @RequestParam(required = false) Integer minAge,
                                 @RequestParam(required = false) Integer maxPersons,
                                 @RequestParam Double latitude,
                                 @RequestParam Double longitude) {

        registrationService.createVehicle(providerId, type, description,
                new PricingPolicy(pricePerUnit, billingModel),
                new UsageRestrictions(maxDurationMinutes, maxKilometers, minAge, maxPersons),
                new VehicleLocation(latitude, longitude));

        return "redirect:/ui/provider/" + providerId + "/fleet";
    }

    @PostMapping("/ui/vehicles/{id}/delete")
    public String deleteVehicle(@PathVariable Long id, @RequestParam Long providerId) {
        registrationService.deleteVehicle(id);
        return "redirect:/ui/provider/" + providerId + "/fleet";
    }

    @GetMapping("/ui/vehicles/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow();
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("billingModels", BillingModel.values());
        return "edit";
    }

    @PostMapping("/ui/vehicles/{id}/edit")
    public String editSubmit(@PathVariable Long id,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) BigDecimal pricePerUnit,
                              @RequestParam(required = false) BillingModel billingModel,
                              @RequestParam(required = false) Integer maxDurationMinutes,
                              @RequestParam(required = false) Integer maxKilometers,
                              @RequestParam(required = false) Integer minAge,
                              @RequestParam(required = false) Integer maxPersons) {

        PricingPolicy pricingPolicy = pricePerUnit != null || billingModel != null
                ? new PricingPolicy(pricePerUnit, billingModel) : null;
        UsageRestrictions restrictions = new UsageRestrictions(maxDurationMinutes, maxKilometers, minAge, maxPersons);

        Vehicle updated = registrationService.updateVehicle(id, description, pricingPolicy, restrictions);
        return "redirect:/ui/provider/" + updated.getProviderId() + "/fleet";
    }
}
