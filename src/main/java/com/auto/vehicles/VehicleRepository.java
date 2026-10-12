package com.auto.vehicles;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {

  List<VehicleEntity> findAllByUser_Email(String email);
  long countByUser_Email(String email);

  Optional<VehicleEntity> findByIdAndUser_Email(Long id, String email);
}
