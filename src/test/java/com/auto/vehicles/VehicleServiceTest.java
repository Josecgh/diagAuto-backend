package com.auto.vehicles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.auto.enums.AccountStatus;
import com.auto.enums.VehicleType;
import com.auto.user.UserEntity;
import com.auto.user.UserRepository;
import com.auto.vehicles.dto.VehicleDTORequest;
import com.auto.vehicles.dto.VehicleDTOResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

  @Mock private VehicleRepository vehicleRepository;
  @Mock private UserRepository userRepository;
  @InjectMocks private VehicleService vehicleService;

  @Test
  void createAssociatesVehicleWithItsOwner() {
    UserEntity owner = owner("ana@example.com");
    VehicleDTORequest request = request("1234ABC");
    when(userRepository.findByEmailForUpdate(owner.getEmail())).thenReturn(Optional.of(owner));
    when(vehicleRepository.save(any(VehicleEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    VehicleDTOResponse response = vehicleService.create(request, owner.getEmail());

    ArgumentCaptor<VehicleEntity> captor = ArgumentCaptor.forClass(VehicleEntity.class);
    verify(vehicleRepository).save(captor.capture());
    assertEquals(owner, captor.getValue().getUser());
    assertEquals("1234ABC", response.plate());
    assertEquals(owner.getId(), response.userId());
  }

  @Test
  void findAllOnlyReturnsVehiclesForTheRequestedOwner() {
    when(vehicleRepository.findAllByUser_Email("ana@example.com"))
        .thenReturn(List.of(vehicle("1234ABC", owner("ana@example.com"))));

    List<VehicleDTOResponse> response = vehicleService.findAll("ana@example.com");

    assertEquals(1, response.size());
    assertEquals("1234ABC", response.get(0).plate());
    verify(vehicleRepository).findAllByUser_Email("ana@example.com");
  }

  @Test
  void findByIdReturnsNotFoundWhenVehicleDoesNotBelongToOwner() {
    when(vehicleRepository.findByIdAndUser_Email(3L, "ana@example.com"))
        .thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> vehicleService.findById(3L, "ana@example.com"));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void updateChangesVehicleFields() {
    VehicleEntity entity = vehicle("1234ABC", owner("ana@example.com"));
    VehicleDTORequest request = request("5678DEF");
    when(vehicleRepository.findByIdAndUser_Email(3L, "ana@example.com"))
        .thenReturn(Optional.of(entity));
    when(vehicleRepository.save(entity)).thenReturn(entity);

    VehicleDTOResponse response = vehicleService.update(3L, request, "ana@example.com");

    assertEquals("5678DEF", response.plate());
    assertEquals(42000L, response.kilometres());
  }

  @Test
  void deleteOnlyDeletesVehicleFoundForOwner() {
    VehicleEntity entity = vehicle("1234ABC", owner("ana@example.com"));
    when(vehicleRepository.findByIdAndUser_Email(3L, "ana@example.com"))
        .thenReturn(Optional.of(entity));

    vehicleService.delete(3L, "ana@example.com");

    verify(vehicleRepository).delete(entity);
  }

  private UserEntity owner(String email) {
    return UserEntity.builder()
        .email(email)
        .password("encoded")
        .name("Ana")
        .lastName("López")
        .accountStatus(AccountStatus.FREE)
        .build();
  }

  @Test
  void freePlanCanCreateFourthVehicle() {
    UserEntity owner = owner("ana@example.com");
    when(userRepository.findByEmailForUpdate(owner.getEmail())).thenReturn(Optional.of(owner));
    when(vehicleRepository.countByUser_Email(owner.getEmail())).thenReturn(3L);
    when(vehicleRepository.save(any(VehicleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    vehicleService.create(request("1234ABC"), owner.getEmail());
    verify(vehicleRepository).save(any(VehicleEntity.class));
  }

  @Test
  void freePlanCannotCreateFifthVehicle() {
    UserEntity owner = owner("ana@example.com");
    when(userRepository.findByEmailForUpdate(owner.getEmail())).thenReturn(Optional.of(owner));
    when(vehicleRepository.countByUser_Email(owner.getEmail())).thenReturn(4L);
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> vehicleService.create(request("1234ABC"), owner.getEmail()));
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    verify(vehicleRepository, never()).save(any(VehicleEntity.class));
  }

  @Test
  void premiumCanCreateFifthVehicle() {
    UserEntity owner = owner("ana@example.com");
    owner.setAccountStatus(AccountStatus.PREMIUM);
    when(userRepository.findByEmailForUpdate(owner.getEmail())).thenReturn(Optional.of(owner));
    when(vehicleRepository.save(any(VehicleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    vehicleService.create(request("1234ABC"), owner.getEmail());
    verify(vehicleRepository).save(any(VehicleEntity.class));
  }

  private VehicleEntity vehicle(String plate, UserEntity owner) {
    return VehicleEntity.builder()
        .vehicleType(VehicleType.CAR)
        .brand("Seat")
        .model("Ibiza")
        .plate(plate)
        .kilometres(10000L)
        .hasFault(false)
        .technicalInspectionDate(LocalDate.of(2026, 5, 1))
        .user(owner)
        .build();
  }

  private VehicleDTORequest request(String plate) {
    return new VehicleDTORequest(VehicleType.CAR, "Seat", "Ibiza", plate, 42000L,
        true, LocalDate.of(2026, 5, 1), "Engine noise");
  }
}
