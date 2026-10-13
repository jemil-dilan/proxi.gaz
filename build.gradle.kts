import javax.inject.Inject
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.gradle.process.ExecOperations

plugins {
	java
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.20.0"
}

group = "cm.jemil"
version = "0.0.0-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// --- main ---
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-liquibase")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")
	implementation("org.mapstruct:mapstruct:1.6.3")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
	implementation("jakarta.validation:jakarta.validation-api")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")

	// --- test ---
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-liquibase-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("io.rest-assured:rest-assured:6.0.1")
	testImplementation("org.awaitility:awaitility")
	testImplementation("com.fasterxml.jackson.core:jackson-databind")
	testImplementation("io.cucumber:cucumber-java8:8.0.4")
	testImplementation("io.cucumber:cucumber-spring:8.0.4")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
	testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
}

// ---------------------------------------------------------------------------
// OpenAPI generation (Demo contract -> API interface + DTOs)
// ---------------------------------------------------------------------------
tasks.register<GenerateTask>("mainOpenApiGenerate") {
	generatorName = "spring"
	inputSpec = "$rootDir/specs/openapi/main.yaml"
	outputDir = layout.buildDirectory.dir("generated/sources/openapi/main").get().asFile.path
	apiPackage = "cm.jemil.proxi.gaz.generated.adapter.rest.inbound.api"
	modelPackage = "cm.jemil.proxi.gaz.generated.adapter.rest.inbound.dto"
	configOptions =
		mutableMapOf(
			"dateLibrary" to "java8-localdatetime",
			"library" to "spring-boot",
			"interfaceOnly" to "true",
			"useTags" to "true",
			"useJakartaEe" to "true",
			"useJackson3" to "true",
			"useSpringBoot4" to "true",
			"openApiNullable" to "false",
			"skipDefaultInterface" to "true",
		)
	inputs.file(inputSpec)
	outputs.dir(layout.buildDirectory.dir("generated/sources/openapi/main"))
	doFirst {
		delete(layout.buildDirectory.dir("generated/sources/openapi/main"))
	}
}

tasks.named("compileJava") {
	dependsOn("mainOpenApiGenerate")
}

sourceSets.main {
	java.srcDir(layout.buildDirectory.dir("generated/sources/openapi/main/src/main/java"))
}

// ---------------------------------------------------------------------------
// Test tasks: unit (test) / data (dataTest) / e2e (e2eTest + Cucumber CLI)
// ---------------------------------------------------------------------------
tasks.test {
	useJUnitPlatform {
		excludeTags("e2e", "data")
	}
}

tasks.register<Test>("dataTest") {
	group = "verification"
	description = "Runs Testcontainers/JPA data tests"
	testClassesDirs = sourceSets.test.get().output.classesDirs
	classpath = sourceSets.test.get().runtimeClasspath
	dependsOn("testClasses")
	useJUnitPlatform {
		includeTags("data")
	}
}

interface InjectedExecOps {
	@get:Inject
	val execOps: ExecOperations
}

tasks.register("e2eTest") {
	group = "verification"
	description = "Runs Cucumber E2E tests"
	dependsOn("testClasses")

	val injected = project.objects.newInstance<InjectedExecOps>()
	val testRuntimeClasspath = sourceSets.test.get().runtimeClasspath

	doLast {
		injected.execOps.javaexec {
			classpath = testRuntimeClasspath
			mainClass = "io.cucumber.core.cli.Main"
			args =
				listOf(
					"--plugin",
					"html:build/cucumber-reports/html/index.html",
					"--plugin",
					"json:build/cucumber-reports/json/cucumber.json",
					"--tags",
					"not @Disabled",
					"--glue",
					"cm.jemil.proxi.gaz.e2e.steps",
					"src/test/resources/e2e/features",
				)
		}
	}
}
