package com.deliverysystem.controller.api.v1;

import com.deliverysystem.domain.Vehicle;
import com.deliverysystem.dto.ApiResponse;
import com.deliverysystem.dto.VehicleDto;
import com.deliverysystem.repository.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {
    
    private final VehicleRepository vehicleRepository;
    
    public VehicleController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleDto>>> getVehicles(
            @RequestParam(required = false) String depotId) {
        List<Vehicle> vehicles;
        if (depotId != null) {
            vehicles = vehicleRepository.findByDepotId(depotId);
        } else {
            vehicles = vehicleRepository.findAll();
        }
        
        List<VehicleDto> dtos = vehicles.stream().map(vehicle -> {
            VehicleDto dto = new VehicleDto();
            dto.setId(vehicle.getId());
            dto.setDepotId(vehicle.getDepot().getId());
            dto.setRegistration(vehicle.getRegistration());
            dto.setMakeModel(vehicle.getMake() + " " + vehicle.getModel());
            dto.setCapacity(vehicle.getCapacity());
            if (vehicle.getMotDate() != null) {
                dto.setMotDue(vehicle.getMotDate().format(DATE_FORMATTER));
            }
            if (vehicle.getNextServiceDue() != null) {
                dto.setNextService(vehicle.getNextServiceDue().format(DATE_FORMATTER));
            }
            dto.setStatus(vehicle.getStatus());
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}
