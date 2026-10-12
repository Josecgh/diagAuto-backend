package com.auto.billing;

import com.auto.user.UserRepository;
import com.auto.user.dto.UserDTOResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/billing")
public class BillingController {
  private final StripeGateway stripe;
  private final PremiumPaymentService payments;
  private final UserRepository users;
  private final ObjectMapper mapper;
  private final String webhookSecret;
  public BillingController(StripeGateway stripe, PremiumPaymentService payments, UserRepository users,
      ObjectMapper mapper, @Value("${stripe.webhook-secret:}") String webhookSecret) {
    this.stripe = stripe; this.payments = payments; this.users = users;
    this.mapper = mapper; this.webhookSecret = webhookSecret;
  }
  public record CheckoutResponse(String url) {}
  public record Confirmation(boolean paid, UserDTOResponse user) {}

  @PostMapping("/checkout")
  public CheckoutResponse checkout(Authentication authentication) throws StripeException {
    var owner = users.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (owner.getAccountStatus() == com.auto.enums.AccountStatus.PREMIUM)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Already Premium");
    return new CheckoutResponse(stripe.createCheckout(owner).getUrl());
  }

  @PostMapping("/checkout/{id}/confirm")
  public Confirmation confirm(@PathVariable String id, Authentication authentication) throws StripeException {
    var owner = users.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    var session = stripe.retrieve(id);
    if (!owner.getId().toString().equals(session.getClientReferenceId()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    if (!payments.isPaidPremium(session)) return new Confirmation(false, null);
    return new Confirmation(true, payments.fulfill(session));
  }

  @PostMapping("/webhook")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signature) throws StripeException {
    if (webhookSecret.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
    String type;
    String sessionId;
    try {
      type = Webhook.constructEvent(payload, signature, webhookSecret).getType();
      if (!"checkout.session.completed".equals(type) && !"checkout.session.async_payment_succeeded".equals(type)) return;
      sessionId = mapper.readTree(payload).path("data").path("object").path("id").asText();
      if (!sessionId.startsWith("cs_test_")) throw new IllegalArgumentException();
    } catch (SignatureVerificationException | java.io.IOException | IllegalArgumentException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe webhook");
    }
    var session = stripe.retrieve(sessionId);
    if (payments.isPaidPremium(session)) payments.fulfill(session);
  }
}
