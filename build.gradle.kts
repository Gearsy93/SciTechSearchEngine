plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.gearsy"
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
	maven { url = uri("https://repo.spring.io/milestone") }
}

extra["springAiVersion"] = "1.0.0-M5"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-neo4j")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.ai:spring-ai-neo4j-store-spring-boot-starter")
	implementation("org.springframework.ai:spring-ai-transformers-spring-boot-starter")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("com.microsoft.onnxruntime:onnxruntime_gpu:1.20.0")
	implementation("org.apache.commons:commons-math3:3.6.1")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.register<JavaExec>("runFillNeo4j") {
	group = "application"
	mainClass.set("com.gearsy.thesaurusdatacollector.ThesaurusDataCollectorApplicationKt")
	classpath = sourceSets["main"].runtimeClasspath

	// Читаем свойство 'filename' из Gradle, если оно задано, иначе можно задать значение по умолчанию.
	val fileName: String = if (project.hasProperty("filename")) {
		project.property("filename").toString()
	} else {
		"defaultFileName" // можно задать значение по умолчанию или вывести ошибку
	}
	args = listOf("-fillNeo4j", fileName)
}

tasks.register<JavaExec>("runClearNeo4j") {
	group = "application"
	mainClass.set("com.gearsy.thesaurusdatacollector.ThesaurusDataCollectorApplicationKt")
	classpath = sourceSets["main"].runtimeClasspath
	args = listOf("-clearNeo4j")
	jvmArgs = listOf(
		"-Dfile.encoding=UTF-8",
		"-Dsun.stdout.encoding=UTF-8",
		"-Dsun.stderr.encoding=UTF-8"
	)
}

tasks.register<JavaExec>("runGenerateCSCSTIThesaurusVectors") {
	group = "application"
	mainClass.set("com.gearsy.neo4jthesaurus.Neo4jThesaurusApplicationKt")
	classpath = sourceSets["main"].runtimeClasspath

	val cscstiCipher: String = project.findProperty("cscstiCipher")?.toString() ?: run {
		println("Ошибка: укажите шифр рубрики CSCSTI через -PcscstiCipher=...")
		return@register
	}

	args = listOf("-generateCSCSTIThesaurusVectors", cscstiCipher)
}
