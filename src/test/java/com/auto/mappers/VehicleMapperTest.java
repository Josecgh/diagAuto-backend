package com.auto.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.auto.enums.AccountStatus;
import com.auto.enums.VehicleType;
import com.auto.user.UserEntity;
import com.auto.vehicles.VehicleEntity;
import com.auto.vehicles.dto.VehicleDTORequest;
import com.auto.vehicles.dto.VehicleDTOResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class VehicleMapperTest {

  @Test
  void toEntityMapsRequestFieldsAndOwner() {
    UserEntity owner = owner();
    VehicleDTORequest request = request("1234ABC", 42000L, true, "Ruidos en el motor");

    VehicleEntity entity = VehicleMapper.toEntity(request, owner);

    assertEquals(VehicleType.CAR, entity.getVehicleType());
    assertEquals("Seat", entity.getBrand());
    assertEquals("Ibiza", entity.getModel());
    assertEquals("1234ABC", entity.getPlate());
    assertEquals(42000L, entity.getKilometres());
    assertEquals(true, entity.getHasFault());
    assertEquals(LocalDate.of(2026, 5, 1), entity.getTechnicalInspectionDate());
    assertEquals("Ruidos en el motor", entity.getFaultDescription());
    assertSame(owner, entity.getUser());
  }

  @Test
  void updateEntityReplacesRequestFieldsAndKeepsOwner() {
    UserEntity owner = owner();
    VehicleEntity entity = VehicleMapper.toEntity(
        request("1234ABC", 10000L, false, null), owner);
    VehicleDTORequest update = request("5678DEF", 42000L, true, "Ruidos en el motor");

    VehicleEntity updated = VehicleMapper.updateEntity(entity, update);

    assertSame(entity, updated);
    assertEquals(VehicleType.CAR, updated.getVehicleType());
    assertEquals("Seat", updated.getBrand());
    assertEquals("Ibiza", updated.getModel());
    assertEquals("5678DEF", updated.getPlate());
    assertEquals(42000L, updated.getKilometres());
    assertEquals(true, updated.getHasFault());
    assertEquals(LocalDate.of(2026, 5, 1), updated.getTechnicalInspectionDate());
    assertEquals("Ruidos en el motor", updated.getFaultDescription());
    assertSame(owner, updated.getUser());
  }

  @Test
  void toDTOMapsEntityFieldsAndOwnerId() {
    UserEntity owner = owner();
    VehicleEntity entity = VehicleMapper.toEntity(
        request("1234ABC", 42000L, true, "Ruidos en el motor"), owner);

    VehicleDTOResponse response = VehicleMapper.toDTO(entity);

    assertNull(response.id());
    assertEquals(VehicleType.CAR, response.vehicleType());
    assertEquals("Seat", response.brand());
    assertEquals("Ibiza", response.model());
    assertEquals("1234ABC", response.plate());
    assertEquals(42000L, response.kilometres());
    assertEquals(true, response.hasFault());
    assertEquals(LocalDate.of(2026, 5, 1), response.technicalInspectionDate());
    assertEquals("Ruidos en el motor", response.faultDescription());
    assertEquals(42L, response.userId());
  }

  @Test
  void toDTOReturnsNullWhenEntityIsNull() {
    assertNull(VehicleMapper.toDTO(null));
  }

  private UserEntity owner() {
    UserEntity owner = UserEntity.builder()
        .email("ana@example.com")
        .password("hashed-password")
        .name("Ana")
        .lastName("López")
        .accountStatus(AccountStatus.FREE)
        .build();
    ReflectionTestUtils.setField(owner, "id", 42L);
    return owner;
  }

  private VehicleDTORequest request(String plate, Long kilometres, Boolean hasFault,
      String faultDescription) {
    return new VehicleDTORequest(VehicleType.CAR, "Seat", "Ibiza", plate, kilometres,
        hasFault, LocalDate.of(2026, 5, 1), faultDescription);
  }
}
