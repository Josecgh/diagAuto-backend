package com.auto.vehicles;

import com.auto.contracts.IVehicleService;
import com.auto.mappers.VehicleMapper;
import com.auto.user.UserEntity;
import com.auto.user.UserRepository;
import com.auto.vehicles.dto.VehicleDTORequest;
import com.auto.vehicles.dto.VehicleDTOResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VehicleService implements IVehicleService {

  private final VehicleRepository vehicleRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public VehicleDTOResponse create(VehicleDTORequest request, String ownerEmail) {
    UserEntity owner = findUser(ownerEmail);
    VehicleEntity vehicle = VehicleMapper.toEntity(request, owner);
    return VehicleMapper.toDTO(vehicleRepository.save(vehicle));
  }

  @Override
  @Transactional(readOnly = true)
  public VehicleDTOResponse findById(Long id, String ownerEmail) {
    return VehicleMapper.toDTO(findOwnedVehicle(id, ownerEmail));
  }

  @Override
  @Transactional(readOnly = true)
  public List<VehicleDTOResponse> findAll(String ownerEmail) {
    return vehicleRepository.findAllByUser_Email(ownerEmail).stream()
        .map(VehicleMapper::toDTO)
        .toList();
  }

  @Override
  @Transactional
  public VehicleDTOResponse update(Long id, VehicleDTORequest request, String ownerEmail) {
    VehicleEntity vehicle = findOwnedVehicle(id, ownerEmail);
    VehicleMapper.updateEntity(vehicle, request);
    return VehicleMapper.toDTO(vehicleRepository.save(vehicle));
  }

  @Override
  @Transactional
  public void delete(Long id, String ownerEmail) {
    vehicleRepository.delete(findOwnedVehicle(id, ownerEmail));
  }

  private VehicleEntity findOwnedVehicle(Long id, String ownerEmail) {
    return vehicleRepository.findByIdAndUser_Email(id, ownerEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
  }

  private UserEntity findUser(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }
}
