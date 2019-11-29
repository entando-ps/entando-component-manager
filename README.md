# Entando Component Manager
This service serves as an abstraction layer for interaction with Digital-Exchanges servers.
Bundles served by Digital-exchanges can be installed in an EntandoApp using this service. This service relies on the Entando K8S service for interaction with the K8S cluster

## Install

You'll need a Kubernetes cluster running, configure the environments described down below and execute the project.

## Environment Variables
>- `KEYCLOAK_AUTH_URL`: The keycloak authentication URL. Default: `http://localhost:8081/auth`
>- `KEYCLOAK_REALM`: The keycloak realm. Default: `entando`
>- `KEYCLOAK_CLIENT_ID`: The keycloak resource/clientId. Default: `entando-config`
>- `KEYCLOAK_CLIENT_SECRET`: The keycloak client secret.
>- `DB_VENDOR`: Which database will be used. Default `postgres`
>- `DB_HOST`: Database host. Default `localhost`
>- `DB_PORT`: Database port. Default `5432`
>- `DB_NAME`: Database name. Default `digital_exchange`
>- `DB_OPTIONS`: Database options. Default `useSSL=false`
>- `DB_USER`: Database user. Default `admin`
>- `DB_PASS`: Database password. Default `admin`

>- `ENTANDO_URL`: The URL to access the Entando App instance.
>- `ENTANDO_APP_NAMESPACE`: The kubernetes namespace where the entando app is running. Default to `test-namespace`;
>- `ENTANDO_APP_NAME`: The entando app name that this service is in. Defaults to `test-entando`.

## Testing
Before running the tests, first run `docker-compose up` or make sure you have a PostgreSQL available and edit the environment variables to run on the DB. Then just run the test command line from an IDE of your choice or by command line.

```mvn test```

### Optional Environment Variables:
>- `LOG_LEVEL`: Log level. Default: `INFO`

## Dependencies
This project uses Entando `web-commons` which uses Entando `keycloak-connector`.

In order to make it work on dev environment, you have to clone and install the dependencies or just add to your IntelliJ workspace.

* Web Commons: https://github.com/entando/web-commons
* Keycloak Connector: https://github.com/entando/keycloak-commons

### CLI:
```
$ git clone git@github.com:entando/web-commons.git
$ git clone git@github.com:entando/keycloak-commons.git

$ cd web-commons && mvn install -Dmaven.test.skip=true && cd ..
$ cd keycloak-commons && mvn install -Dmaven.test.skip=true && cd ..
```
