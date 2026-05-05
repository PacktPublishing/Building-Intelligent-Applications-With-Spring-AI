plugins {
  java
  id("org.springframework.boot") version "3.5.0"
  id("io.spring.dependency-management") version "1.1.7"
}

group = 'com.packt.spring.ai'
version = '0.3.0-SNAPSHOT'

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

repositories {
  mavenCentral()
}

extra["springAiVersion"] = "1.0.0"
}

dependencies {
  implementation("org.springframework.ai:spring-ai-starter-model-open-ai")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.ai:spring-ai-bom:${springAiVersion}")
  }
}

tasks.named('test') {
  useJUnitPlatform()
}
