package com.auto.vehicles.dto;

import java.time.LocalDate;

import com.auto.enums.VehicleType;
import lombok.Builder;

@Builder
public record VehicleDTOResponse(
  Long id,
  VehicleType vehicleType,
  String brand,
  String model,
  String plate,
  Long kilometres,
  Boolean hasFault,
  LocalDate technicalInspectionDate,
  String faultDescription,
  Long userId
) {

}
