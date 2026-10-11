package com.auto.contracts;

import com.auto.vehicles.dto.VehicleDTORequest;
import com.auto.vehicles.dto.VehicleDTOResponse;
import java.util.List;

public interface IVehicleService {

  VehicleDTOResponse create(VehicleDTORequest request, String ownerEmail);

  VehicleDTOResponse findById(Long id, String ownerEmail);

  List<VehicleDTOResponse> findAll(String ownerEmail);

  VehicleDTOResponse update(Long id, VehicleDTORequest request, String ownerEmail);

  void delete(Long id, String ownerEmail);
}
