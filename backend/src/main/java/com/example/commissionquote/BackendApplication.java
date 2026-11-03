package com.example.commissionquote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Starts the Commission Quote app: our quote API, the mock vendor API and (in the Docker image) the React UI.
 * {@link ConfigurationPropertiesScan} picks up the typed configuration classes (VendorProperties,
 * MockVendorProperties).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
