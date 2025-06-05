/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import lombok.extern.log4j.Log4j2;

@RemoteApplicationEventScan(basePackages = { "com.softwareag.research.mini_api_gatway.events" })
@Configuration
@Log4j2
public class EventService {

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Value("${spring.cloud.bus.id}")
	private String cloudInstanceId;

	private static final Destination clusterDestination = () -> "mini-api-gateway:**";

	@EventListener
	public void handleRouteChangedEvent(DistributedRoutesChangedEvent event) {
		if (!event.getOriginService().equals(cloudInstanceId)) {
			log.info("Received RoutesChanged Event from other instance");
			applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
		}
	}

	public void publishRouteRefresh() {
		applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
		applicationEventPublisher
				.publishEvent(new DistributedRoutesChangedEvent(this, cloudInstanceId, clusterDestination));
	}

	public void publishIssuerRefresh() {
		applicationEventPublisher.publishEvent(new RefreshIssuersEvent(this, cloudInstanceId, clusterDestination));
	}

}
