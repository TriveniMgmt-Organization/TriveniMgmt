plugins {
	java
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.trivenimgmt.store"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.security:spring-security-crypto")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.github.f4b6a3:uuid-creator:6.1.0")
	implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	// implementation("org.flywaydb:flyway-core:11.10.2")
//	implementation("org.liquibase:liquibase-core")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
	// MapStruct Core Dependency
	implementation("org.mapstruct:mapstruct:1.5.5.Final") // Use the latest stable version
// Rate Limiting with Bucket4j
//	implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:8.15.0")
	// MapStruct Processor (for annotation processing)
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final") // Must match core version

	// If you are using Lombok, add lombok-mapstruct-binding
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
	compileOnly("org.projectlombok:lombok")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("com.h2database:h2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
//	implementation("io.jsonwebtoken:jjwt-api:0.12.5")
//	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
//	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
	}

tasks.withType<Test> {
	useJUnitPlatform()
}
