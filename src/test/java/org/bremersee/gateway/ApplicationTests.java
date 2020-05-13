/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.gateway;

import static org.bremersee.security.core.AuthorityConstants.ACTUATOR_ROLE_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.StringUtils;

/**
 * The application tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "bremersee.security.authentication.enable-jwt-support=false"
})
@ActiveProfiles({"in-memory"})
@TestInstance(Lifecycle.PER_CLASS)
class ApplicationTests {

	/**
	 * The application context.
	 */
	@Autowired
  ApplicationContext context;

	/**
	 * The web test client.
	 */
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  WebTestClient webTestClient;

	/**
	 * Sets up.
	 */
	@BeforeAll
  void setUp() {
    // https://docs.spring.io/spring-security/site/docs/current/reference/html/test-webflux.html
    WebTestClient
        .bindToApplicationContext(this.context)
        .configureClient()
        .build();
  }

	/**
	 * Fetch health.
	 */
	@Test
  void fetchHealth() {
    webTestClient
        .get()
        .uri("/actuator/health")
        .accept(MediaType.ALL)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value((Consumer<String>) content -> assertTrue(StringUtils.hasText(content)));
  }

	/**
	 * Fetch metrics.
	 */
	@WithMockUser(
      username = "actuator",
      password = "actuator",
      authorities = {ACTUATOR_ROLE_NAME})
  @Test
  void fetchMetrics() {
    webTestClient
        .get()
        .uri("/actuator/metrics")
        .accept(MediaType.ALL)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .value((Consumer<String>) content -> assertTrue(StringUtils.hasText(content)));
  }

	/**
	 * Fetch metrics and expect unauthorized.
	 */
	@Test
  void fetchMetricsAndExpectUnauthorized() {
    webTestClient
        .get()
        .uri("/actuator/metrics")
        .accept(MediaType.ALL)
        .exchange()
        .expectStatus().isUnauthorized();
  }

}
