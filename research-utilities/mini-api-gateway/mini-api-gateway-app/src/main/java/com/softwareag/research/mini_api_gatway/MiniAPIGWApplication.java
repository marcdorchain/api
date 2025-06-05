/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway;

import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import io.r2dbc.spi.ConnectionFactory;

@SpringBootApplication
public class MiniAPIGWApplication {

	public static void main(String[] args) {
		// Netty requires accessLogEnabled System property to be set before Spring
		// launch
		String enableAccessLog = System.getenv("ENABLE_ACCESS_LOG");
		if (enableAccessLog != null) {
			System.setProperty("reactor.netty.http.server.accessLogEnabled", enableAccessLog);
		}
		// Generate a random identifier for this instance, if none is set
		if (System.getenv("INSTANCE_ID") == null) {
			System.setProperty("INSTANCE_ID", UUID.randomUUID().toString());
		}

		// Launch Spring
		SpringApplication.run(MiniAPIGWApplication.class, args);
	}

	@Profile("h2")
	@Bean
	ConnectionFactoryInitializer h2Initializer(ConnectionFactory connectionFactory) {
		var initializer = new ConnectionFactoryInitializer();
		initializer.setConnectionFactory(connectionFactory);
		initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema-h2.sql")));

		return initializer;
	}

//	@Profile("postgres")
//	@Bean
//	ConnectionFactoryInitializer postgresInitializer(ConnectionFactory connectionFactory) {
//		var initializer = new ConnectionFactoryInitializer();
//		initializer.setConnectionFactory(connectionFactory);
//		initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema-postgres.sql")));
//
//		return initializer;
//	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return new RouteService();
	}

}
