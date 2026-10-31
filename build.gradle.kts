import com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep
import org.gradle.process.ExecOperations
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import javax.inject.Inject

plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.20.0"
    id("com.diffplug.spotless") version "8.9.0"
    id("com.google.cloud.tools.jib") version "3.5.4"
}

group = "cm.jemil"
version = providers.gradleProperty("appVersion").getOrElse("0.0.0-SNAPSHOT")

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
    implementation("org.hibernate.orm:hibernate-spatial")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    implementation("org.springframework.boot:spring-boot-starter-validation")
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
// Spotless formatting (license headers + import order; no Java formatter due to Java 25 incompatibility)
// ---------------------------------------------------------------------------

spotless {
    java {
        // googleJavaFormat() / palantirJavaFormat() not compatible with Java 25 yet
        licenseHeader(
            """
            /*
             * Copyright (c) 2026 ProxiGaz
             * All rights reserved.
             */
            """.trimIndent(),
        )
        // kotlinGradle { ktlint() } // disabled due to Kotlin version incompatibility

        targetExclude("build/**")
        toggleOffOn()
        googleJavaFormat("1.33.0")
            .reflowLongStrings()
            .formatJavadoc(true)
            .reorderImports(true)
            .groupArtifact("com.google.googlejavaformat:google-java-format")
    }
    kotlin {
        targetExclude("build/**")
        target("**/*.kts")
        ktlint("1.5.0")
    }
    format("xml", {
        targetExclude("build/**")
        target("src/**/*.xml")
        eclipseWtp(EclipseWtpFormatterStep.XML)
    })

    yaml {
        targetExclude("build/**")
        target("src/*/resources/**/*.yaml", "src/*/resources/**/*.yml", "openapi/**/*.yaml")
        targetExclude("src/test/resources/docker-compose.yml")
        jackson()
            .feature("ORDER_MAP_ENTRIES_BY_KEYS", true)
    }
    gherkin {
        targetExclude("build/**")
        target("src/test/resources/**/*.feature")
        gherkinUtils()
            .version("9.0.0")
    }
}

tasks.named("spotlessJavaApply") {
    group = "formatting"
    description = "Applies Spotless formatting to Java sources"
}

tasks.named("spotlessJavaCheck") {
    group = "verification"
    description = "Checks Java sources for Spotless formatting violations"
}

tasks.named("check") {
    dependsOn("spotlessJavaCheck")
}

// ---------------------------------------------------------------------------
// OpenAPI generation (Demo contract -> API interface + DTOs)
// ---------------------------------------------------------------------------
tasks.register<GenerateTask>("mainOpenApiGenerate") {
    generatorName = "spring"
    inputSpec = "$rootDir/specs/openapi/main.yaml"
    outputDir =
        layout.buildDirectory
            .dir("generated/sources/openapi/main")
            .get()
            .asFile.path
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
    testClassesDirs =
        sourceSets.test
            .get()
            .output.classesDirs
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

// ---------------------------------------------------------------------------
// Jib configuration for container builds (Docker Hub)
// Note: Health check is defined in docker-compose files, not in the image
// To push to GHCR, run: ./gradlew jib -Djib.to.image=ghcr.io/jemil-dilan/proxi.gaz
// ---------------------------------------------------------------------------
jib {
    from {
        image = "eclipse-temurin:25-jre"
    }
    to {
        image = "docker.io/mohamednfonka/proxi.gaz"
        tags = setOf(project.version.toString(), "latest")
    }
    container {
        jvmFlags = listOf("-XX:MaxRAMPercentage=75.0")
        user = "10001:10001"
        creationTime = "USE_CURRENT_TIMESTAMP"
        ports = listOf("8080")
    }
}
