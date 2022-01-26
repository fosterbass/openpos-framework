## Coding Standards & Best Practices for OpenPOS

### Naming Conventions
- Interfaces start with I.   ie.  IBusinessService
- Database Model classes are suffixed with Model.java.  ie. TransModel.java
- Database interaction goes into Repository classes.  ie. TransRepository.java

### General
- Use Lombok where it makes sense to
  - @Slf4j for logging
  - @Data for POJOs
  - @Builder for creating objects

### Modules & Services
- Module = Microservice
- Modules are domain specific projects that expose standalone APIs and interact with standalone databases.   Database tables are prefixed with a namespace to identify tables that are related.
- A module has an -api project that exposes an api of both model classes and service interfaces.   The implementation project has internal classes and endpoint implementations.
  - ie. commerce-payment-api
  - ie. commerce-payment
- Different implementations should have their own projects.
  - ie. commerce-payment-adyen
- Packaging
  - org.jumpmind.pos.domain - XxxxModule.java classes go here
  - org.jumpmind.pos.domain.model - Database models go here.  Repositories go here.
  - org.jumpmind.pos.domain.service - Service interfaces go here.  Endpoints go here
  - org.jumpmind.pos.domain.service.model - Request and Response objects go here along with payloads
- All service calls should take a Request object if they have arguments they need to accept and return a Response object should they have return values.
  - HttpMethod.PUT should be used for all services that accept Request objects
  - HttpMethod.GET should be use for all service that return a Response object, but do not accept a Request object
- Exception Handling
  - Throw exception or swallow and return graceful error
  - Unknown exceptions should always bubble up
  - Known failure conditions should be handled by Response objects
- Always prefer @Builders over constructors

### States
- States should have unit tests. Ie.  AddCustomerStateTest

### UX - Angular


