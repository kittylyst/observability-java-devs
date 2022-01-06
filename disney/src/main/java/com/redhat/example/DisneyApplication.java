package com.redhat.example;

import com.redhat.utils.OpenTelemetryConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DisneyApplication {

  public static void main(String[] args) {
    // Configure OpenTelemetry as early as possible
    OpenTelemetryConfig.configureGlobal("marvel-app");
    SpringApplication.run(DisneyApplication.class, args);
  }
}
