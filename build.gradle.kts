plugins {
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version  "1.9.25"
	kotlin("plugin.serialization") version "1.9.10"
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
	implementation("io.ktor:ktor-client-core:3.1.1")
	implementation("io.ktor:ktor-client-cio:3.1.1")
	implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
	implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-neo4j:3.4.4")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.ai:spring-ai-neo4j-store-spring-boot-starter")
	implementation("org.springframework.ai:spring-ai-transformers-spring-boot-starter")
	implementation("io.github.bonigarcia:webdrivermanager:5.9.2")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.seleniumhq.selenium:selenium-java:4.28.1")
	implementation("com.microsoft.onnxruntime:onnxruntime_gpu:1.20.0")
	implementation("org.apache.commons:commons-math3:3.6.1")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
	implementation("ai.djl.huggingface:tokenizers:0.32.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
	implementation("net.mikera:vectorz:0.67.0")
	implementation("org.postgresql:postgresql:42.7.5")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}

kotlin {
	sourceSets.main {
		kotlin.srcDir("src/main/kotlin")
	}
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<JavaExec> {
	jvmArgs = listOf(
		"-Dfile.encoding=UTF-8",
		"-Dsun.stdout.encoding=UTF-8",
		"-Dsun.stderr.encoding=UTF-8"
	)
}

tasks.register<JavaExec>("runGenerateTermThesaurusEmbeddings") {
	group = "application"
	classpath = sourceSets["main"].runtimeClasspath
	mainClass.set("com.gearsy.scitechsearchengine.ScienceTechnologySearchEngineApplicationKt")
	args = listOf("-generate_term_thesaurus_embeddings")
}

tasks.register<JavaExec>("runImportTermThesaurus") {
	group = "application"
	classpath = sourceSets["main"].runtimeClasspath
	mainClass.set("com.gearsy.scitechsearchengine.ScienceTechnologySearchEngineApplicationKt")
	args = listOf("-import_term_thesaurus")
}

tasks.register<JavaExec>("runGetQueryRelevantRubricTermList") {
	group = "application"
	classpath = sourceSets["main"].runtimeClasspath
	mainClass.set("com.gearsy.scitechsearchengine.ScienceTechnologySearchEngineApplicationKt")
	args = listOf("-get_query_relevant_rubric_term_list")
}

tasks.register<JavaExec>("runMakeECatalogSearchRequest") {
	group = "application"
	mainClass.set("com.gearsy.scitechsearchengine.ScienceTechnologySearchEngineApplicationKt")
	classpath = sourceSets["main"].runtimeClasspath
	args = listOf("-make_e-catalog_request")
}

tasks.register<JavaExec>("runMakeYandexSearchApiRequest") {
	group = "application"
	classpath = sourceSets["main"].runtimeClasspath
	mainClass.set("com.gearsy.scitechsearchengine.ScienceTechnologySearchEngineApplicationKt")
	args = listOf("-make_yandex_search_api_request")
}

tasks.register<JavaExec>("runSearchConveyor") {
	group = "application"
	classpath = sourceSets["main"].runtimeClasspath
	mainClass.set("com.gearsy.scitechsearchengine.ScienceTechnologySearchEngineApplicationKt")
	args = listOf("-run_search_conveyor")
}
