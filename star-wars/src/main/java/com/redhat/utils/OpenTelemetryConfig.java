package com.redhat.utils;

import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
//import io.opentelemetry.exporter.logging.LoggingMetricExporter;
//import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.internal.RetryPolicy;
import io.opentelemetry.exporter.otlp.internal.grpc.DefaultGrpcExporterBuilder;
//import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporter;
//import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;
//import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
//import io.opentelemetry.sdk.logs.export.BatchLogProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.SERVICE_INSTANCE_ID;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.SERVICE_NAME;

public class OpenTelemetryConfig {

  // "OTLP_HOST"
  private static final Supplier<String> OTLP_HOST_SUPPLIER = () -> "http://localhost:4317";

  public static void configureGlobal(String defaultServiceName) {
    var resource = configureResource(defaultServiceName);

    // Configure traces
    var spanExporterBuilder =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint(OTLP_HOST_SUPPLIER.get());
//            .addHeader("api-key", "");

    // Enable retry policy via unstable API
    DefaultGrpcExporterBuilder.getDelegateBuilder(
            OtlpGrpcSpanExporterBuilder.class, spanExporterBuilder)
        .addRetryPolicy(RetryPolicy.getDefault());

    var sdkTracerProviderBuilder =
        SdkTracerProvider.builder()
            .setResource(resource)
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporterBuilder.build()).build());
    OpenTelemetrySdk.builder()
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .setTracerProvider(sdkTracerProviderBuilder.build())
        .buildAndRegisterGlobal();

    // Configure metrics
    var meterProviderBuilder = SdkMeterProvider.builder().setResource(resource);

    var metricExporterBuilder =
        OtlpGrpcMetricExporter.builder()
            .setPreferredTemporality(AggregationTemporality.DELTA)
            .setEndpoint(OTLP_HOST_SUPPLIER.get());
//            .addHeader("api-key", );

    // Enable retry policy via unstable API
    DefaultGrpcExporterBuilder.getDelegateBuilder(
            OtlpGrpcMetricExporterBuilder.class, metricExporterBuilder)
        .addRetryPolicy(RetryPolicy.getDefault());

    meterProviderBuilder.registerMetricReader(
        PeriodicMetricReader.builder(metricExporterBuilder.build())
            .setInterval(Duration.ofSeconds(5))
            .newMetricReaderFactory());

    GlobalMeterProvider.set(meterProviderBuilder.build());
  }

  private static Resource configureResource(String serviceName) {
    return Resource.getDefault()
        .merge(
            Resource.builder()
                .put(
                    SERVICE_NAME,
                    serviceName)
                .put(SERVICE_INSTANCE_ID, UUID.randomUUID().toString())
                .build());
  }

  private OpenTelemetryConfig() {}
}
