/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.security;

import org.springframework.context.ApplicationEvent;

public class RefreshIssuersEvent extends ApplicationEvent {

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param source the object on which the event initially occurred (never
	 *               {@code null})
	 */
	public RefreshIssuersEvent(Object source) {
		super(source);
	}

}