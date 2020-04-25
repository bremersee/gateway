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

package org.bremersee.gateway.config;

import org.bremersee.security.authentication.AuthenticationProperties;
import org.bremersee.security.authentication.PasswordFlowReactiveAuthenticationManager;
import org.bremersee.security.authentication.RoleBasedAuthorizationManager;
import org.bremersee.security.authentication.RoleOrIpBasedAuthorizationManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.util.Assert;

/**
 * The security configuration.
 *
 * @author Christian Bremer
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

  /**
   * The in-memory security configuration.
   */
  @ConditionalOnProperty(
      prefix = "bremersee.security.authentication",
      name = "enable-jwt-support",
      havingValue = "false", matchIfMissing = true)
  @Configuration
  @EnableConfigurationProperties(AuthenticationProperties.class)
  static class InMemory {

    private AuthenticationProperties properties;

    /**
     * Instantiates a new in-memory security configuration.
     *
     * @param properties the properties
     */
    public InMemory(AuthenticationProperties properties) {
      this.properties = properties;
    }

    /**
     * User details service.
     *
     * @return the user details service
     */
    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
      return new MapReactiveUserDetailsService(properties.buildBasicAuthUserDetails());
    }

    @Bean
    @Order(51)
    public SecurityWebFilterChain gatewayFilterChain(ServerHttpSecurity http) {
      return http
          .securityMatcher(new NegatedServerWebExchangeMatcher(EndpointRequest.toAnyEndpoint()))
          .authorizeExchange().anyExchange().permitAll()
          .and()
          .httpBasic().disable()
          .csrf().disable()
          .build();
    }

    /**
     * The security filter chain.
     *
     * @param http the http
     * @return the security web filter chain
     */
    @Bean
    @Order(52)
    public SecurityWebFilterChain actuatorFilterChain(ServerHttpSecurity http) {
      //noinspection DuplicatedCode
      return http
          .securityMatcher(EndpointRequest.toAnyEndpoint())
          .authorizeExchange()
          .pathMatchers(HttpMethod.OPTIONS).permitAll()
          .matchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
          .matchers(EndpointRequest.to(InfoEndpoint.class)).permitAll()
          .matchers(new AndServerWebExchangeMatcher(
              EndpointRequest.toAnyEndpoint(),
              ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/**")))
          .access(new RoleOrIpBasedAuthorizationManager(
              properties.getActuator().getRoles(),
              properties.getRolePrefix(),
              properties.getActuator().getIpAddresses()))
          .matchers(EndpointRequest.toAnyEndpoint())
          .access(new RoleBasedAuthorizationManager(
              properties.getActuator().getAdminRoles(),
              properties.getRolePrefix()))
          .anyExchange().denyAll()
          .and()
          .httpBasic()
          .and()
          .formLogin().disable()
          .csrf().disable()
          .build();
    }
  }

  /**
   * The security configuration with password flow and OpenID provider.
   */
  @ConditionalOnProperty(
      prefix = "bremersee.security.authentication",
      name = "enable-jwt-support",
      havingValue = "true")
  @Configuration
  @EnableConfigurationProperties(AuthenticationProperties.class)
  static class PasswordFlow {

    private AuthenticationProperties properties;

    private PasswordFlowReactiveAuthenticationManager passwordFlowAuthenticationManager;

    /**
     * Instantiates a new security configuration with password flow.
     *
     * @param properties the properties
     * @param authenticationManagerProvider the authentication manager provider
     */
    public PasswordFlow(AuthenticationProperties properties,
        ObjectProvider<PasswordFlowReactiveAuthenticationManager> authenticationManagerProvider) {
      this.properties = properties;
      this.passwordFlowAuthenticationManager = authenticationManagerProvider.getIfAvailable();
      Assert.notNull(passwordFlowAuthenticationManager,
          "Password flow authentication manager must be present.");
    }

    @Bean
    @Order(51)
    public SecurityWebFilterChain gatewayFilterChain(ServerHttpSecurity http) {
      return http
          .securityMatcher(new NegatedServerWebExchangeMatcher(EndpointRequest.toAnyEndpoint()))
          .authorizeExchange()
          .pathMatchers(HttpMethod.OPTIONS).permitAll()
          .anyExchange().permitAll()
          .and()
          .httpBasic().disable()
          .csrf().disable()
          .build();
    }

    /**
     * The security filter chain.
     *
     * @param http the http
     * @return the security web filter chain
     */
    @Bean
    @Order(52)
    public SecurityWebFilterChain actuatorFilterChain(ServerHttpSecurity http) {
      //noinspection DuplicatedCode
      return http
          .securityMatcher(EndpointRequest.toAnyEndpoint())
          .authorizeExchange()
          .pathMatchers(HttpMethod.OPTIONS).permitAll()
          .matchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
          .matchers(EndpointRequest.to(InfoEndpoint.class)).permitAll()
          .matchers(new AndServerWebExchangeMatcher(
              EndpointRequest.toAnyEndpoint(),
              ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/**")))
          .access(new RoleOrIpBasedAuthorizationManager(
              properties.getActuator().getRoles(),
              properties.getRolePrefix(),
              properties.getActuator().getIpAddresses()))
          .matchers(EndpointRequest.toAnyEndpoint())
          .access(new RoleBasedAuthorizationManager(
              properties.getActuator().getAdminRoles(),
              properties.getRolePrefix()))
          .anyExchange().denyAll()
          .and()
          .httpBasic()
          .authenticationManager(passwordFlowAuthenticationManager)
          .and()
          .formLogin().disable()
          .csrf().disable()
          .build();
    }
  }

}
