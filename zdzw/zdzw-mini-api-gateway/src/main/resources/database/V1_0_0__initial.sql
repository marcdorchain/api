-- SPDX-FileCopyrightText: Copyright (c) 2025 Software GmbH, Darmstadt, Germany and/or its subsidiaries and/or its affiliates
-- SPDX-FileContributor: Jonas Schmitt
--
-- SPDX-License-Identifier: Apache-2.0

CREATE TABLE IF NOT EXISTS PUBLIC.ROUTE_ENTITY (
	ID BIGSERIAL NOT NULL,
	ENDPOINT TEXT NOT NULL,
	NAME TEXT NOT NULL,
	VERSION TEXT NOT NULL,
	SPECIFICATION JSON,
	ACTIVE BOOLEAN NOT NULL,
	FILTERS JSON,
	CONSTRAINT CONSTRAINT_4 PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS PUBLIC.OPEN_ID_CONNECT_ISSUER (
	ID BIGSERIAL NOT NULL,
	ISSUER TEXT UNIQUE NOT NULL,
	TOKEN_INTROSPECTION_ENDPOINT TEXT,
	MATCH_MODE TEXT NOT NULL,
	CONSTRAINT CONSTRAINT_5 PRIMARY KEY (ID)
);