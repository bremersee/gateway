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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

/**
 * The application tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"in-memory"})
@TestInstance(Lifecycle.PER_CLASS)
class ApplicationTests {

  /**
   * The local server port.
   */
  @LocalServerPort
  int port;

  /**
   * Base url of the local server.
   *
   * @return the base url of the local server
   */
  String baseUrl() {
    return "http://localhost:" + port;
  }

  /**
   * Creates a new web client, that uses the real security configuration.
   *
   * @return the web client
   */
  WebClient newWebClient() {
    return WebClient.builder()
        .baseUrl(baseUrl())
        .build();
  }

  /**
   * Fetch health.
   */
  @Test
  void fetchHealth() {
    StepVerifier.create(newWebClient()
        .get()
        .uri("/actuator/health")
        .accept(MediaType.ALL)
        .exchange())
        .assertNext(clientResponse -> assertEquals(HttpStatus.OK, clientResponse.statusCode()))
        .verifyComplete();
  }

  /**
   * Fetch metrics.
   */
  @Test
  void fetchMetrics() {
    StepVerifier.create(newWebClient()
        .get()
        .uri("/actuator/metrics")
        .headers(httpHeaders -> httpHeaders
            .setBasicAuth("actuator", "actuator", StandardCharsets.UTF_8))
        .accept(MediaType.ALL)
        .exchange())
        .assertNext(clientResponse -> assertEquals(HttpStatus.OK, clientResponse.statusCode()))
        .verifyComplete();
  }

  /**
   * Fetch metrics and expect unauthorized.
   */
  @Test
  void fetchMetricsAndExpectUnauthorized() {
    StepVerifier.create(newWebClient()
        .get()
        .uri("/actuator/metrics")
        .accept(MediaType.ALL)
        .exchange())
        .assertNext(clientResponse -> assertEquals(
            HttpStatus.UNAUTHORIZED,
            clientResponse.statusCode()))
        .verifyComplete();
  }

}
