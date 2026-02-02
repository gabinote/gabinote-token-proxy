import org.hidetake.gradle.swagger.generator.GenerateSwaggerUI

plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.spring") version "2.2.10"
    kotlin("kapt") version "2.2.10"
    id("org.jetbrains.kotlin.plugin.noarg") version "2.2.10"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.epages.restdocs-api-spec") version "0.17.1"
    id("org.hidetake.swagger.generator") version "2.18.2"
    id("jacoco")
}
group = "com.gabinote"
version = "0.0.1-SNAPSHOT"
description = "token-proxy"
val springCloudVersion = "2025.0.0"
noArg {
    annotation("com.fasterxml.jackson.annotation.JsonCreator")
    annotation("com.fasterxml.jackson.annotation.JsonIgnoreProperties")
    annotation("com.gabinote.coffeenote.common.util.json.annotation.JsonNoArg")
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:2.0.2")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    //core
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test") {

//        exclude(group = "com.vaadin.external.google", module = "android-json")

    }
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    // junit
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    // mockk
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    // https://mvnrepository.com/artifact/io.mockk/mockk
    testImplementation("io.mockk:mockk:1.14.2")
    // https://mvnrepository.com/artifact/com.ninja-squad/springmockk
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    // testcontainers
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:kafka")
    testImplementation("com.github.dasniko:testcontainers-keycloak:3.7.0")
    // https://mvnrepository.com/artifact/io.debezium/debezium-testing-testcontainers
    testImplementation("io.debezium:debezium-testing-testcontainers:3.4.0.Final")
    // rest assured
    testImplementation("io.rest-assured:rest-assured:5.5.5")
    testImplementation("io.rest-assured:kotlin-extensions:5.5.5")
    // kotest
    val kotestVersion = "5.9.1"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation("org.springframework:spring-jdbc")
    //test utils
    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // database
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-redis

    // caching
    implementation("org.springframework.boot:spring-boot-starter-cache")
    // https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")

    // aop
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework:spring-aspects")

    // docs
    implementation("org.webjars:swagger-ui:4.11.1")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.17.1")

    // logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.5")

    // dto
    // mapstruct
    // https://mvnrepository.com/artifact/org.mapstruct/mapstruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    kapt("org.mapstruct:mapstruct-processor:1.5.5.Final")
//    compileOnly("org.mapstruct:mapstruct-processor:1.5.5.Final")
    // jackson
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.15.2"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    //kafka
    implementation("org.springframework.kafka:spring-kafka")
// https://mvnrepository.com/artifact/io.github.resilience4j/resilience4j-circuitbreaker
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:2.3.0")

    //json
    // https://mvnrepository.com/artifact/com.lectra/koson
    implementation("com.lectra:koson:1.2.9")

    //keycloak
    // https://mvnrepository.com/artifact/org.keycloak/keycloak-admin-client
    implementation("org.keycloak:keycloak-admin-client:26.0.8")
    implementation("org.keycloak:keycloak-authz-client:26.0.8")

    // circuit breaker
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.test {
    useJUnitPlatform()
    systemProperty("user.timezone", "Asia/Seoul")
    jvmArgs("-Duser.timezone=Asia/Seoul")
    systemProperty("mockk.stacktraces.on", "true")
    systemProperty("mockk.stacktraces.alignment", "left")
    jvmArgs(
        "--add-opens", "java.base/java.time=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
    )
    systemProperty("spring.profiles.active", "test")
    testLogging {
        events("passed", "skipped", "failed", "standard_out", "standard_error")
        showStandardStreams = true
    }
    finalizedBy("openapi3")
    finalizedBy("jacocoTestReport")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // 테스트 후에 JaCoCo 리포트 생성

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)

    violationRules {
        rule {
            limit {
                minimum = "0.8".toBigDecimal()
            }
            excludes = listOf(
                "com.common.config.*",
                "com.common.dto.*",
                "com.field.dto.*",
            )
        }
    }
}


openapi3 {
    title = "API 문서"
    description = "RestDocsWithSwagger Docs"
    version = "0.0.1"
    format = "yaml"
}


tasks.withType<GenerateSwaggerUI> {
    dependsOn("openapi3")
}

swaggerSources {
    create("api").apply {
        setInputFile(File("${project.buildDir}/api-spec/openapi3.yaml"))
    }
}