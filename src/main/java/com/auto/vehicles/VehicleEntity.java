package com.auto.vehicles;

import com.auto.user.UserEntity;
import com.auto.enums.VehicleType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor (access = AccessLevel.PROTECTED)
public class VehicleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  @Setter(AccessLevel.NONE)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "vehicle_type", nullable = false)
  private VehicleType vehicleType;

  @Column(nullable = false)
  private String brand;

  @Column(nullable = false)
  private String model;

  @Column(nullable = false)
  private String plate;

  @Column(name = "kilometers", nullable = false)
  private Long kilometres;

  @Column(name = "has_fault", nullable = false)
  private Boolean hasFault;

  @Column(name = "technical_inspection_date")
  private LocalDate technicalInspectionDate;

  @Column(name = "fault_description")
  private String faultDescription;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Builder
  public VehicleEntity(VehicleType vehicleType, String brand, String model, String plate,
      Long kilometres, Boolean hasFault, LocalDate technicalInspectionDate,
      String faultDescription, UserEntity user) {
    this.vehicleType = vehicleType;
    this.brand = brand;
    this.model = model;
    this.plate = plate;
    this.kilometres = kilometres;
    this.hasFault = hasFault;
    this.technicalInspectionDate = technicalInspectionDate;
    this.faultDescription = faultDescription;
    this.user = user;
  }
}
