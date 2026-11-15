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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

/**
 * The application tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
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
            .retrieve().toEntity(String.class))
        .assertNext(response -> assertEquals(HttpStatus.OK, response.getStatusCode()))
        .verifyComplete();
  }

}
