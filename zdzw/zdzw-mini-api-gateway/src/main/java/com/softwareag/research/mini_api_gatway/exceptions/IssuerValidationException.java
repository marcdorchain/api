/*
 * SPDX-FileCopyrightText: Copyright 2025 Software GmbH
 * SPDX-License-Identifier: Apache-2.0
 */
package com.softwareag.research.mini_api_gatway.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception that results in HTTP Status 403 when thrown because the token used
 * for authentication is invalid. Reasons could be that the issuer is not
 * trusted or the token is expired or revoked.
 *
 * @author jonsch
 *
 */
public class IssuerValidationException extends ResponseStatusException {

	private static final long serialVersionUID = 4963149167382756699L;

	public IssuerValidationException(String message) {
		super(HttpStatus.FORBIDDEN, message);
	}

}
