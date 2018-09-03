package com.ingenico.api;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@EnableScheduling
@ComponentScan("com.ingenico.api")
@SpringBootApplication
public class IngenicoApplication {

	public static void main(String[] args) {
		SpringApplication.run(IngenicoApplication.class, args);
	}

	@Bean
	public Docket newsApi() {
		return new Docket(DocumentationType.SWAGGER_2)//
				.apiInfo(apiInfo())//
				.select() //
				.paths(PathSelectors.regex("/api/.*")) //
				.build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()//
				.title("Ingenico API")//
				.contact(new Contact("Taha Berkay Duman", "https://www.linkedin.com/in/tahaberkayduman", "tahaberkayduman.tbd@gmail.com"))//
				.description("This Swagger enabled RESTful API provides money transfer service for monetary accounts")//
				.version("0.0.1")//
				.build();
	}
	
	  @Bean	  
	  @Primary	  
	  @ConfigurationProperties(prefix = "spring.datasource") public DataSource
	  dataSource() { return DataSourceBuilder.create().build(); }
	  
	  @Bean public JdbcTemplate jdbcTemplate() { return new
	  JdbcTemplate(dataSource()); }

}
