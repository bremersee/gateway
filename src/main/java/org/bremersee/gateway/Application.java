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

import org.bremersee.context.MessageSourceAutoConfiguration;
import org.bremersee.converter.ModelMapperAutoConfiguration;
import org.bremersee.web.reactive.BaseCommonConvertersAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The application.
 *
 * @author Christian Bremer
 */
@SpringBootApplication(exclude = {
    BaseCommonConvertersAutoConfiguration.class,
    ModelMapperAutoConfiguration.class,
    MessageSourceAutoConfiguration.class
})
public class Application {

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
