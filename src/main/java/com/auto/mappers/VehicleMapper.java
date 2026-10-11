package com.auto.mappers;

import com.auto.user.UserEntity;
import com.auto.vehicles.VehicleEntity;
import com.auto.vehicles.dto.VehicleDTORequest;
import com.auto.vehicles.dto.VehicleDTOResponse;

public class VehicleMapper {

  private VehicleMapper() {}

  public static VehicleEntity toEntity(VehicleDTORequest request, UserEntity owner) {
    return VehicleEntity.builder()
        .vehicleType(request.vehicleType())
        .brand(request.brand())
        .model(request.model())
        .plate(request.plate())
        .kilometres(request.kilometres())
        .hasFault(request.hasFault())
        .technicalInspectionDate(request.technicalInspectionDate())
        .faultDescription(request.faultDescription())
        .user(owner)
        .build();
  }

  public static VehicleEntity updateEntity(VehicleEntity entity, VehicleDTORequest request) {
    entity.setVehicleType(request.vehicleType());
    entity.setBrand(request.brand());
    entity.setModel(request.model());
    entity.setPlate(request.plate());
    entity.setKilometres(request.kilometres());
    entity.setHasFault(request.hasFault());
    entity.setTechnicalInspectionDate(request.technicalInspectionDate());
    entity.setFaultDescription(request.faultDescription());
    return entity;
  }

  public static VehicleDTOResponse toDTO(VehicleEntity entity) {
    if (entity == null) return null;

    return VehicleDTOResponse.builder()
        .id(entity.getId())
        .vehicleType(entity.getVehicleType())
        .brand(entity.getBrand())
        .model(entity.getModel())
        .plate(entity.getPlate())
        .kilometres(entity.getKilometres())
        .hasFault(entity.getHasFault())
        .technicalInspectionDate(entity.getTechnicalInspectionDate())
        .faultDescription(entity.getFaultDescription())
        .userId(entity.getUser().getId())
        .build();
  }
}
