-- SPDX-FileCopyrightText: Copyright (c) 2025 Software GmbH, Darmstadt, Germany and/or its subsidiaries and/or its affiliates
-- SPDX-FileContributor: Jonas Schmitt
--
-- SPDX-License-Identifier: Apache-2.0

ALTER TABLE PUBLIC.OPEN_ID_CONNECT_ISSUER
ADD COLUMN CLIENT_ID TEXT,
ADD COLUMN CLIENT_SECRET TEXT;