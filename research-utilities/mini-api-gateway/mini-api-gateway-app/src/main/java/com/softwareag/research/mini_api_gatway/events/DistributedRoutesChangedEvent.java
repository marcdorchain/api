/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.softwareag.research.mini_api_gatway.events;

import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

public class DistributedRoutesChangedEvent extends RemoteApplicationEvent {

	private static final long serialVersionUID = -4713670930853221789L;

	public DistributedRoutesChangedEvent() {
		super();
	}

	public DistributedRoutesChangedEvent(Object source, String originService, Destination destinationService) {
		super(source, originService, destinationService);
	}

}
