package com.auto.secure;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;

class CsrfCookieFilterTest {

  @Test
  void readsCsrfTokenWhenPresentAndContinuesFilterChain() throws ServletException, IOException {
    CsrfCookieFilter filter = new CsrfCookieFilter();
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();
    CsrfToken csrfToken = org.mockito.Mockito.mock(CsrfToken.class);
    when(csrfToken.getToken()).thenReturn("csrf-value");
    request.setAttribute(CsrfToken.class.getName(), csrfToken);

    filter.doFilter(request, response, chain);

    verify(csrfToken).getToken();
    assertSame(request, chain.getRequest());
  }

  @Test
  void continuesWhenCsrfTokenIsNotPresent() throws ServletException, IOException {
    CsrfCookieFilter filter = new CsrfCookieFilter();
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain chain = new MockFilterChain();

    filter.doFilter(request, response, chain);

    assertSame(request, chain.getRequest());
  }
}
