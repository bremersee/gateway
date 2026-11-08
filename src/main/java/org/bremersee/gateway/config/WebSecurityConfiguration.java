/*
 * Copyright 2026 the original author or authors.
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

package org.bremersee.gateway.config;

import static org.springframework.util.ObjectUtils.isEmpty;

import org.bremersee.spring.boot.autoconfigure.security.authentication.AuthenticationProperties;
import org.bremersee.spring.security.ldaptive.authentication.ReactiveLdaptiveAuthenticationManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.security.autoconfigure.actuate.web.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.FormLoginSpec;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

/**
 * The web security configuration.
 *
 * @author Christian Bremer
 */
@EnableWebFluxSecurity
@EnableConfigurationProperties({
    AuthenticationProperties.class
})
@Configuration
class WebSecurityConfiguration {

  private final AuthenticationProperties authenticationProperties;

  private final CorsConfigurationSource corsConfigurationSource;

  private final ReactiveLdaptiveAuthenticationManager ldaptiveAuthenticationManager;

  private final ReactiveUserDetailsService userDetailsService;

  /**
   * Instantiates a new web security configuration.
   *
   * @param authenticationProperties the authentication properties
   * @param corsConfigurationSource the cors configuration source
   */
  WebSecurityConfiguration(
      AuthenticationProperties authenticationProperties,
      CorsConfigurationSource corsConfigurationSource,
      ObjectProvider<ReactiveLdaptiveAuthenticationManager> ldaptiveAuthenticationManager,
      ObjectProvider<ReactiveUserDetailsService> userDetailsService) {

    this.authenticationProperties = authenticationProperties;
    this.corsConfigurationSource = corsConfigurationSource;
    this.ldaptiveAuthenticationManager = ldaptiveAuthenticationManager.getIfAvailable();
    this.userDetailsService = userDetailsService.getIfAvailable();
  }

  /**
   * Creates a security web filter chain.
   *
   * @param http the http
   * @return the security filter chain
   */
  @Bean
  SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
    String[] actuatorReadRoles = authenticationProperties.getActuator().getReadRoles()
        .toArray(new String[0]);
    String[] actuatorWriteRoles = authenticationProperties.getActuator().getWriteRoles()
        .toArray(new String[0]);
    http
        .authorizeExchange(authorize -> authorize
            .pathMatchers(HttpMethod.OPTIONS, "/**")
            .permitAll()

            .matchers(
                EndpointRequest.to(InfoEndpoint.class),
                EndpointRequest.to(HealthEndpoint.class))
            .permitAll()

            .matchers(new AndServerWebExchangeMatcher(
                EndpointRequest.toAnyEndpoint(),
                new PathPatternParserServerWebExchangeMatcher("/**", HttpMethod.GET)))
            .hasAnyAuthority(actuatorReadRoles)

            .matchers(EndpointRequest.toAnyEndpoint())
            .hasAnyAuthority(actuatorWriteRoles)

            /*
            .pathMatchers("/v3/**").permitAll()
            .pathMatchers("/webjars/**").permitAll()
            .pathMatchers("/swagger-ui/**").permitAll()
            .pathMatchers("/swagger-ui.html").permitAll()
            */

            .anyExchange()
            .permitAll()
        )

        .csrf(CsrfSpec::disable)

        .cors(customizer -> customizer
            .configurationSource(corsConfigurationSource))

        .headers(headerSpec -> headerSpec
            .frameOptions(fos -> fos.mode(Mode.SAMEORIGIN)))

        .formLogin(FormLoginSpec::disable);

    if (!isEmpty(ldaptiveAuthenticationManager)) {
      http.httpBasic(customizer -> customizer
          .authenticationManager(ldaptiveAuthenticationManager));
    } else if (!isEmpty(userDetailsService)) {
      var authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(
          userDetailsService);
      http.httpBasic(customizer -> customizer
          .authenticationManager(authenticationManager));
    }

    return http.build();
  }

}
