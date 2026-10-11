package com.auto.vehicles;

import com.auto.contracts.IVehicleService;
import com.auto.vehicles.dto.VehicleDTORequest;
import com.auto.vehicles.dto.VehicleDTOResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

  private final IVehicleService vehicleService;

  @PostMapping({"", "/"})
  @ResponseStatus(HttpStatus.CREATED)
  public VehicleDTOResponse create(
      @Valid @RequestBody VehicleDTORequest request,
      Authentication authentication) {
    return vehicleService.create(request, authentication.getName());
  }

  @GetMapping({"", "/"})
  public List<VehicleDTOResponse> findAll(Authentication authentication) {
    return vehicleService.findAll(authentication.getName());
  }

  @GetMapping("{id}")
  public VehicleDTOResponse findById(
      @PathVariable Long id,
      Authentication authentication) {
    return vehicleService.findById(id, authentication.getName());
  }

  @PutMapping("{id}")
  public VehicleDTOResponse update(
      @PathVariable Long id,
      @Valid @RequestBody VehicleDTORequest request,
      Authentication authentication) {
    return vehicleService.update(id, request, authentication.getName());
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, Authentication authentication) {
    vehicleService.delete(id, authentication.getName());
  }
}
