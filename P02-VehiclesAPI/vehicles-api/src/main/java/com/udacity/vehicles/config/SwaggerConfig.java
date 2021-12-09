package com.udacity.vehicles.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;

@Configuration
@Controller
@ApiIgnore
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.udacity.vehicles.api"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Verhicles REST API",
                "This API returns vehicles.",
                "1.0",
                "",
                new Contact("Sebastian Koch", "www.se2c.de", "kochsebastian@gmail.com"),
                "License of API", "http://www.se2c.de/license", Collections.emptyList());
    }

    @RequestMapping("/swagger-ui.html")
    public String doc() {
        return "redirect:/swagger-ui/index.html";
    }

}
