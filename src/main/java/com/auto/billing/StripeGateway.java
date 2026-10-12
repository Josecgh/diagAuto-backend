package com.auto.billing;

import com.auto.user.UserEntity;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class StripeGateway {
  private final String secretKey;
  private final String frontendUrl;
  public StripeGateway(@Value("${stripe.secret-key:}") String secretKey,
      @Value("${stripe.frontend-url:http://localhost:4200}") String frontendUrl) {
    this.secretKey = secretKey;
    this.frontendUrl = frontendUrl.replaceAll("/+$", "");
  }
  private StripeClient client() {
    if (!secretKey.startsWith("sk_test_")) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Stripe test key is not configured");
    }
    return new StripeClient(secretKey);
  }
  public Session createCheckout(UserEntity owner) throws StripeException {
    var price = SessionCreateParams.LineItem.PriceData.builder()
        .setCurrency("eur").setUnitAmount(9999L)
        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
            .setName("DiagAuto Premium — pago único").build()).build();
    var params = SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.PAYMENT)
        .addAllowedPaymentMethodType(SessionCreateParams.AllowedPaymentMethodType.CARD)
        .setCustomerEmail(owner.getEmail()).setClientReferenceId(owner.getId().toString())
        .putMetadata("purpose", "diagauto_premium")
        .addLineItem(SessionCreateParams.LineItem.builder().setPriceData(price).setQuantity(1L).build())
        .setSuccessUrl(frontendUrl + "/premium?checkout=success&session_id={CHECKOUT_SESSION_ID}")
        .setCancelUrl(frontendUrl + "/premium?checkout=cancelled").build();
    return client().v1().checkout().sessions().create(params);
  }
  public Session retrieve(String id) throws StripeException {
    return client().v1().checkout().sessions().retrieve(id);
  }
}
