//buildscript {
    repositories {
        mavenCentral()
    }
//}

plugins {
    java
    id("com.diffplug.spotless") version "5.11.1" apply false
    id("org.springframework.boot") version "2.5.8" apply false
    id("io.spring.dependency-management") version "1.0.10.RELEASE" apply false
    // id("de.undercouch.download") version "4.1.1" apply false
}

allprojects {
    group = "com.redhat"
    version = "1.0.0"

    tasks.withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

}

subprojects {
  repositories {
    mavenCentral()
  }

    apply {
        plugin("com.diffplug.spotless")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
//    plugin("de.undercouch.download")
    }

}



tasks.named<Test>("test") {
  useJUnitPlatform()
}