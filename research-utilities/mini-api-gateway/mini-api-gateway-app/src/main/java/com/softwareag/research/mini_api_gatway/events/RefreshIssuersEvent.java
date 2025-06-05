/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.events;

import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

public class RefreshIssuersEvent extends RemoteApplicationEvent {

	private static final long serialVersionUID = 862987115824114603L;

	public RefreshIssuersEvent() {
		super();
	}

	public RefreshIssuersEvent(Object source, String originService, Destination destinationService) {
		super(source, originService, destinationService);
	}
}