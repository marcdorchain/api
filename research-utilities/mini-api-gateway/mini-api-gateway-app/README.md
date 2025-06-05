<!--
SPDX-FileCopyrightText: Copyright (c) 2025 Software GmbH, Darmstadt, Germany and/or its subsidiaries and/or its affiliates
SPDX-FileContributor: Jonas Schmitt

SPDX-License-Identifier: Apache-2.0
-->

# Mini API Gateway

## Installation

Clone repository, put UI module in `ui_module` folder, set configuration via environment variables and then run in IDE or use `mvn package` and then run .jar file.


## Configuration

Currently, all configuration is done via environment variables. Some are required to use Mini API Gateway.

| Variable                         | Description                                                                                                                                                                                | Default               | Required |
|----------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------|----------|
| ADMIN_USERNAME                   | Username for the Admin User                                   | admin                  | No      |
| ADMIN_PASSWORD                   | Password for the Admin User                                    | admin                  | No      |
| TOKEN_VALIDATION_URL             | Default OpenID Connect issuer whose tokens will be granted access to the gateway. **Even if token access is not used, any dummy URL must be supplied.**                                    |                       | **Yes**  |
| TOKEN_VALIDATION_MODE            | How supplied tokens should be validated. "SIGNATURE" mode only verifies the token signature and expiry timestamp while "INTROSPECTION" contacts the issuer to confirm the tokens validity. | SIGNATURE             | No       |
| TOKEN_INTROSPECTION_CLIENTID     | Client ID that should be used to authenticate for Token Introspection at default issuer. Only required if TOKEN_VALIDATION_MODE is INTROSPECTION.                                          | null                  | No*      |
| TOKEN_INTROSPECTION_CLIENTSECRET | Client Secret that should be used to authenticate for Token Introspection at default issuer. Only required if TOKEN_VALIDATION_MODE is INTROSPECTION.                                      | null                  | No*      |
| TOKEN_VALIDATION_ROLESPATH       | Token attribute that contains the user role(s) provided as JSON Path. E.g. "realm-access.my-gateway-app.roles". If the Token attribute value is a string instead of a list, it will be split by whitespace into a list.                                     | scope                  | No      |
| TOKEN_VALIDATION_USERROLE        | Token Role that should be mapped to the Gateway User Role.                                      | ROLE_USER                  | No      |
| TOKEN_VALIDATION_ADMINROLE       | Token Role that should be mapped to the Gateway Admin Role.                                      | ROLE_ADMIN                  | No      |
| PROFILE                          | Comma-separated list of profiles to activate. Profiles are explained below.                                                                                                                | h2                    | No       |
| PUBLIC_URL                       | The public URL where the gateway can be reached. Will be used in responses to clients to construct gateway URLs for routes.                                                                | http://localhost:8080 | No       |
| SERVER_PORT                      | Port to bind the web server to. Should not be changed if using docker image.                                                                                                               | 8080                  | No       |
| DB_URL                           | Postgres URL (e.g. mypostgres.net:5432/database) to connect to. Required if using profile postgres.                                                                                        |                       | No*      |
| DB_USER                          | User for Postgres database. Required if using profile postgres.                                                                                                                            |                       | No*      |
| DB_PASSWORD                      | Password for Postgres database. Required if using profile postgres.                                                                                                                        |                       | No*      |

## Profiles

The PROFILE variable controls the behaviour and availability of different functionalities of the application.

| Profile       | Description                                                                                                                              |
|---------------|------------------------------------------------------------------------------------------------------------------------------------------|
| h2            | Uses embedded H2 database to store data. Should not be used with the docker image, as data will be lost when the container is destroyed. |
| postgres      | Uses a configured PostgreSQL database to store application data.                                                                         |
