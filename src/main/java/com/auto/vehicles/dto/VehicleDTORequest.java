package com.auto.vehicles.dto;

import com.auto.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public record VehicleDTORequest(
    @NotNull VehicleType vehicleType,
    @NotBlank String brand,
    @NotBlank String model,
    @NotBlank String plate,
    @NotNull @PositiveOrZero Long kilometres,
    @NotNull Boolean hasFault,
    LocalDate technicalInspectionDate,
    String faultDescription
) {}
