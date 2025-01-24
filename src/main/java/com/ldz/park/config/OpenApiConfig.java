package com.ldz.park.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI parkOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("停车场管理系统 API")
            .description("停车场管理系统接口文档")
            .version("1.0")
            .contact(new Contact()
                .name("开发团队")
                .email("support@example.com")));
  }
}