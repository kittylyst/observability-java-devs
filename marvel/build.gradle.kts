plugins {
    id("java-library") apply true
}

springBoot {
    mainClass.set("com.redhat.example.MarvelApplication")
}

tasks.named<JavaExec>("bootRun") {
    // Run the app on port 8080
    jvmArgs = listOf("-Dserver.port=8081")
}

configurations.all {
    exclude(module = "spring-boot-starter-logging")
}
// Force upgrade log4j2 to avoid vulnerability: https://spring.io/blog/2021/12/10/log4j2-vulnerability-and-spring-boot
ext["log4j2.version"] = "2.17.1"

val junitVersion: String by project
val otelVersion: String by project
val otelAlphaVersion: String by project
val otelInstrumentationVersion: String by project
val grpcVersion: String by project
val armeriaBomVersion: String by project
val otelProtoVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    implementation(platform("io.opentelemetry:opentelemetry-bom:${otelVersion}"))
    implementation(platform("io.opentelemetry:opentelemetry-bom-alpha:${otelAlphaVersion}"))
    implementation(platform("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom-alpha:${otelInstrumentationVersion}"))
    implementation(platform("io.grpc:grpc-bom:${grpcVersion}"))
    implementation("com.linecorp.armeria:armeria-bom:${armeriaBomVersion}")

    api("io.opentelemetry:opentelemetry-api")
    api("io.opentelemetry:opentelemetry-api-metrics")
    api("io.opentelemetry:opentelemetry-sdk")
    api("io.opentelemetry:opentelemetry-sdk-metrics")
    api("io.opentelemetry:opentelemetry-exporter-otlp")
    api("io.opentelemetry:opentelemetry-exporter-otlp-metrics")
    api("io.opentelemetry:opentelemetry-semconv")
    api("io.opentelemetry.proto:opentelemetry-proto:${otelProtoVersion}")
    implementation("io.grpc:grpc-netty-shaded")
    api("io.grpc:grpc-stub")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testImplementation("org.mockito:mockito-core:4.1.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}