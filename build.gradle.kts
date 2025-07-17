plugins {
	java
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.redisson"
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
	// spring
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-logging")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// cache
	implementation("org.redisson:redisson:3.40.0")
	implementation("com.github.ben-manes.caffeine:caffeine")

	implementation("org.testcontainers:testcontainers-bom:1.21.3")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:testcontainers")

	// lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testCompileOnly("org.projectlombok:lombok")
	testAnnotationProcessor("org.projectlombok:lombok")

	// test
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// 현재 OS와 아키텍처를 확인하여 macOS용 Netty DNS 의존성을 동적으로 추가
	val osName = System.getProperty("os.name")
	if (osName.startsWith("Mac OS X")) {
		val osArch = System.getProperty("os.arch")
		val classifier = when (osArch) {
			"aarch64" -> "osx-aarch_64" // Apple Silicon (M1/M2/M3)
			"x86_64" -> "osx-x86_64"   // Intel
			else -> null
		}

		if (classifier != null) {
			testImplementation("io.netty:netty-resolver-dns-native-macos:4.1.111.Final:$classifier")
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
