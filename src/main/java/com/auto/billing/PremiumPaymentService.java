package com.auto.billing;

import com.auto.enums.AccountStatus;
import com.auto.mappers.UserMapper;
import com.auto.user.UserRepository;
import com.auto.user.dto.UserDTOResponse;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PremiumPaymentService {
  private final UserRepository users;

  public boolean isPaidPremium(Session session) {
    return "paid".equals(session.getPaymentStatus()) && "payment".equals(session.getMode())
        && Boolean.FALSE.equals(session.getLivemode()) && Long.valueOf(9999).equals(session.getAmountTotal())
        && "eur".equals(session.getCurrency()) && session.getMetadata() != null
        && "diagauto_premium".equals(session.getMetadata().get("purpose"));
  }

  @Transactional
  public UserDTOResponse fulfill(Session session) {
    if (!isPaidPremium(session)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment not confirmed");
    Long userId;
    try { userId = Long.valueOf(session.getClientReferenceId()); }
    catch (RuntimeException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment account"); }
    var user = users.findByIdForUpdate(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    // Repeated webhook deliveries are safe: upgrading is idempotent.
    if (user.getAccountStatus() != AccountStatus.PREMIUM) {
      user.setAccountStatus(AccountStatus.PREMIUM);
      users.save(user);
    }
    return UserMapper.toDTO(user);
  }
}
