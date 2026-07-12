# AGENTS.md

- Spring Boot 3.2.3 single-module Maven app (`com.ldz.park.ParkApplication`) on Java 21; use the Maven wrapper, but in this checkout `mvnw` may not be executable, so `bash ./mvnw ...` is safer than `./mvnw ...`.
- Useful commands: `bash ./mvnw test`, focused test `bash ./mvnw -Dtest=ParkApplicationTests test`, package `bash ./mvnw clean package`; Docker build also packages with `-DskipTests`.
- Tests compile with `pom.xml` release 21; if Maven fails with “不支持发行版本 21”, the local JDK is too old, not necessarily the code.
- Default active profile is `prod` in `src/main/resources/application.properties`; override when running locally, e.g. `bash ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` or pass `--spring.profiles.active=dev`.
- Database config is custom `mysql.*` properties wired by `config/MySQLConfiguration.java`, not standard `spring.datasource.*`; it appends `allowMultiQueries=true` when creating the Hikari datasource.
- MyBatis mapper interfaces live in `src/main/java/com/ldz/park/dao` and XML SQL lives in matching `src/main/resources/com/ldz/park/dao/*.xml`; keep interface method names and XML statement ids in sync.
- Controller/service pattern is `web/*Controller` -> `service/*Service` -> `dao/*Mapper`; API responses generally use `model/meta/ApiResponse` and pagination uses PageHelper `PageInfo` plus `ApiResponse.successWithPage`.
- JWT auth is enforced by `security/UserSecurityInterceptor` for `/**`; only `/api/login`, `/api/register`, `/api/sso/**`, Swagger/OpenAPI assets, `/lib/**`, and `/favicon.ico` are excluded. Use `Authorization: Bearer <token>`.
- Swagger UI is configured at `/swagger-ui.html` but disabled in `application-prod.properties`; enable/use a non-prod profile for API docs.
- Do not run MyBatis generator casually: `src/main/resources/generatorConfig.xml` targets real/external DB settings and writes generated Java into `src/main/java`.
- Secrets/default credentials are currently present in properties/config classes; do not copy them into logs, tests, docs, or new code. Prefer existing env overrides such as `MYSQL_USER`, `MYSQL_PASSWORD`, `JWT_SECRET`, `COS_*`, `WECHAT_MINIAPP_APP_ID`, and `WECHAT_MINIAPP_SECRET`.
- WeChat mini-program silent login (`POST /api/oauth/mini/login` and legacy `POST /api/wx-login`) reads `WECHAT_MINIAPP_APP_ID` / `WECHAT_MINIAPP_SECRET` from env; empty values cause `WECHAT_CONFIG_ERROR`. See `docs/mini-login.md`.
- CI (`.github/workflows/deploy.yml`) runs only on `main` pushes/manual dispatch and builds/pushes Docker image `park-java`; it does not run Maven tests separately.
- Dockerfile uses Eclipse Temurin 21, runs `./mvnw clean package -DskipTests`, exposes 8080, but app config defaults `server.port` to `${port:3429}` unless overridden.
