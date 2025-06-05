-- SPDX-FileCopyrightText: Copyright (c) 2025 Software GmbH, Darmstadt, Germany and/or its subsidiaries and/or its affiliates
-- SPDX-FileContributor: Jonas Schmitt
--
-- SPDX-License-Identifier: Apache-2.0

CREATE TABLE IF NOT EXISTS PUBLIC.GLOBAL_FILTER_ENTITY (
	NAME TEXT UNIQUE NOT NULL,
	FILTER JSON NOT NULL,
	_VERSION INT,
	CONSTRAINT CONSTRAINT_8 PRIMARY KEY (NAME)
);

ALTER TABLE PUBLIC.ROUTE_ENTITY
ADD COLUMN GLOBAL_FILTERS JSON;