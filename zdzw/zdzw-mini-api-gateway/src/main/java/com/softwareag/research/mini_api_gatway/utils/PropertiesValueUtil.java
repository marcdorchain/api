/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class PropertiesValueUtil implements ApplicationContextAware {

	private static ApplicationContext appContext;

	public static String getPropertyValue(String propertyPath) {
		return appContext.getEnvironment().getProperty(propertyPath);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		appContext = applicationContext;
	}

}
