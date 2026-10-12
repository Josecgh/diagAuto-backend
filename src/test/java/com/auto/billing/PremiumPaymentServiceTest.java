package com.auto.billing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.auto.user.UserRepository;
import com.stripe.model.checkout.Session;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class PremiumPaymentServiceTest {
  private Session payment() {
    Session session = new Session();
    session.setPaymentStatus("paid"); session.setMode("payment"); session.setLivemode(false);
    session.setAmountTotal(9999L); session.setCurrency("eur");
    session.setMetadata(Map.of("purpose", "diagauto_premium")); session.setClientReferenceId("1");
    return session;
  }

  @Test
  void wrongAmountCurrencyPurposeModeOrPaymentStatusCannotUpgrade() {
    UserRepository users = mock(UserRepository.class);
    PremiumPaymentService service = new PremiumPaymentService(users);
    Session wrongAmount = payment(); wrongAmount.setAmountTotal(1L);
    Session wrongCurrency = payment(); wrongCurrency.setCurrency("usd");
    Session live = payment(); live.setLivemode(true);
    Session unpaid = payment(); unpaid.setPaymentStatus("unpaid");
    Session wrongPurpose = payment(); wrongPurpose.setMetadata(Map.of());
    Session subscription = payment(); subscription.setMode("subscription");
    for (Session session : new Session[] { wrongAmount, wrongCurrency, live, unpaid, wrongPurpose, subscription }) {
      assertFalse(service.isPaidPremium(session));
      assertThrows(ResponseStatusException.class, () -> service.fulfill(session));
    }
    verifyNoInteractions(users);
  }
}
