/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.api.response;

import com.softwareag.research.mini_api_gatway.model.RouteEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RouteResponse extends RouteEntity {

	private String gatewayEndpoint;

	public RouteResponse(RouteEntity rE, String gatewayEndpoint) {
		super();
		setActive(rE.isActive());
		setEndpoint(rE.getEndpoint());
		setFilters(rE.getFilters());
		setGlobalFilters(rE.getGlobalFilters());
		setId(rE.getId());
		setName(rE.getName());
		setSpecification(rE.getSpecification());
		setVersion(rE.getVersion());

		setGatewayEndpoint(gatewayEndpoint);
	}

}
