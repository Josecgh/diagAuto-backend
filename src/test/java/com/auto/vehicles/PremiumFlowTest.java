package com.auto.vehicles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.auto.billing.StripeGateway;
import com.auto.user.UserRepository;
import com.stripe.model.checkout.Session;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:premiumflow;DB_CLOSE_DELAY=-1", "stripe.webhook-secret=whsec_test"})
@AutoConfigureMockMvc
class PremiumFlowTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository users;
  @MockBean private StripeGateway stripe;

  private MockHttpSession login(String email) throws Exception {
    mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(
        "{\"name\":\"Ana\",\"lastName\":\"López\",\"email\":\"" + email + "\",\"password\":\"password123\"}"))
        .andExpect(status().isCreated());
    var result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(
        "{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
        .andExpect(status().isOk()).andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }
  private Session payment(String email, String status) {
    Session payment = new Session();
    payment.setId("cs_test_paid"); payment.setUrl("https://checkout.stripe.com/c/pay/cs_test_paid");
    payment.setClientReferenceId(users.findByEmail(email).orElseThrow().getId().toString());
    payment.setPaymentStatus(status); payment.setMode("payment"); payment.setLivemode(false);
    payment.setAmountTotal(9999L); payment.setCurrency("eur");
    payment.setMetadata(Map.of("purpose", "diagauto_premium")); return payment;
  }
  private String signature(String payload) throws Exception {
    long timestamp = System.currentTimeMillis() / 1000;
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec("whsec_test".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    return "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8)));
  }
  private static final String EVENT = """
      {"id":"evt_test","object":"event","type":"checkout.session.completed","data":{"object":{"id":"cs_test_paid","object":"checkout.session"}}}
      """;

  @Test
  void fifthVehicleRequiresConfirmedPaymentAndWebhookIsIdempotent() throws Exception {
    String email = "stripeflow@example.com";
    var session = login(email);
    String vehicle = """
        {"vehicleType":"CAR","brand":"Toyota","model":"Corolla","plate":"1234ABC","kilometres":0,"hasFault":false}
        """;
    for (int i = 0; i < 4; i++)
      mockMvc.perform(post("/api/vehicles").session(session).contentType(MediaType.APPLICATION_JSON).content(vehicle)).andExpect(status().isCreated());
    mockMvc.perform(post("/api/vehicles").session(session).contentType(MediaType.APPLICATION_JSON).content(vehicle)).andExpect(status().isConflict());
    when(stripe.createCheckout(any())).thenReturn(payment(email, "unpaid"));
    mockMvc.perform(post("/api/billing/checkout").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.url").value("https://checkout.stripe.com/c/pay/cs_test_paid"));
    mockMvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.accountStatus").value("FREE"));
    when(stripe.retrieve("cs_test_paid")).thenReturn(payment(email, "paid"));
    for (int i = 0; i < 2; i++)
      mockMvc.perform(post("/api/billing/webhook").header("Stripe-Signature", signature(EVENT)).contentType(MediaType.APPLICATION_JSON).content(EVENT)).andExpect(status().isNoContent());
    mockMvc.perform(post("/api/vehicles").session(session).contentType(MediaType.APPLICATION_JSON).content(vehicle)).andExpect(status().isCreated());
    mockMvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.accountStatus").value("PREMIUM"));
    mockMvc.perform(get("/api/vehicles").session(session)).andExpect(jsonPath("$.length()").value(5));
  }
  @Test
  void unpaidSessionDoesNotUpgradeAndPaidConfirmationDoes() throws Exception {
    var session = login("confirm@example.com");
    when(stripe.retrieve("cs_test_paid")).thenReturn(payment("confirm@example.com", "unpaid"));
    mockMvc.perform(post("/api/billing/checkout/cs_test_paid/confirm").session(session)).andExpect(jsonPath("$.paid").value(false));
    when(stripe.retrieve("cs_test_paid")).thenReturn(payment("confirm@example.com", "paid"));
    mockMvc.perform(post("/api/billing/checkout/cs_test_paid/confirm").session(session)).andExpect(jsonPath("$.user.accountStatus").value("PREMIUM"));
  }
  @Test
  void anotherAccountsCheckoutCannotBeConfirmed() throws Exception {
    login("owner@example.com");
    var session = login("other@example.com");
    when(stripe.retrieve("cs_test_paid")).thenReturn(payment("owner@example.com", "paid"));
    mockMvc.perform(post("/api/billing/checkout/cs_test_paid/confirm").session(session)).andExpect(status().isForbidden());
    mockMvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.accountStatus").value("FREE"));
  }
  @Test
  void invalidWebhookSignatureIsRejected() throws Exception {
    mockMvc.perform(post("/api/billing/webhook").header("Stripe-Signature", "t=1,v1=invalid").contentType(MediaType.APPLICATION_JSON).content(EVENT)).andExpect(status().isBadRequest());
  }
  @Test
  void anonymousCheckoutIsForbiddenAndDemoEndpointIsRemoved() throws Exception {
    mockMvc.perform(post("/api/billing/checkout")).andExpect(status().isForbidden());
    var session = login("removed@example.com");
    mockMvc.perform(post("/api/users/me/premium-demo").session(session)).andExpect(status().isNotFound());
  }
}
