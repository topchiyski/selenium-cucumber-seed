plugins {
    java
    id("io.qameta.allure") version "4.3.0"
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val seleniumVersion = "4.51.0"
// allure-cucumber7-jvm targets Cucumber 7.x; stay on the 7 line until an adapter for 8.x exists.
val cucumberVersion = "7.34.9"
val testngVersion = "7.12.0"
val allureJavaVersion = "3.0.1"
val allureCliVersion = "2.46.1"
val restAssuredVersion = "6.3.0"
val jacksonVersion = "2.22.4"
val datafakerVersion = "2.7.0"
val logbackVersion = "1.6.6"
val applitoolsEyesVersion = "5.85.17"
val slf4jVersion = "2.0.20"

dependencies {
    testImplementation(platform("io.cucumber:cucumber-bom:$cucumberVersion"))
    testImplementation(platform("io.qameta.allure:allure-bom:$allureJavaVersion"))

    testImplementation("org.seleniumhq.selenium:selenium-java:$seleniumVersion")
    testImplementation("io.cucumber:cucumber-java")
    testImplementation("io.cucumber:cucumber-testng")
    testImplementation("io.cucumber:cucumber-picocontainer")
    testImplementation("org.testng:testng:$testngVersion")

    testImplementation("io.qameta.allure:allure-cucumber7-jvm")
    testImplementation("io.qameta.allure:allure-rest-assured")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion") // JSON (de)serialization for REST Assured
    testImplementation("net.datafaker:datafaker:$datafakerVersion")
    testImplementation("com.applitools:eyes-selenium-java5:$applitoolsEyesVersion") // visual testing (Applitools Eyes)

    // Logging: everything goes through SLF4J to Logback. Selenium logs via java.util.logging and
    // Apache HttpClient (REST Assured) via commons-logging; the bridges reroute both.
    testImplementation("org.slf4j:slf4j-api:$slf4jVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("org.slf4j:jul-to-slf4j:$slf4jVersion")
    testImplementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")
}

configurations.testRuntimeClasspath {
    exclude(group = "commons-logging", module = "commons-logging") // replaced by jcl-over-slf4j
}

allure {
    version = allureCliVersion
    adapter {
        // Dependencies are declared explicitly above; don't let the plugin add its own.
        autoconfigure = false
        aspectjWeaver = false
    }
}

// Every -D property whose key starts with one of these is forwarded to the test JVM,
// e.g. ./gradlew test -Dbrowser=firefox -Dcucumber.filter.tags=@smoke
val forwardedPropertyPrefixes = listOf(
    "env", "browser", "headless", "execution", "grid.", "base.url", "api.", "timeout.",
    "window.", "page-load.", "screenshot.", "threads", "user.", "faker.", "cucumber.", "log.", "applitools.",
)

fun Test.configureCucumberTask(suiteFile: String, runnerClass: String) {
    useTestNG {
        suites(suiteFile)
    }
    // Class scanning doesn't detect the Cucumber runner (its @Test method is inherited from a jar);
    // name it explicitly. What runs is still decided by the suite file.
    isScanForTestClasses = false
    include("**/runners/$runnerClass.class")
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    workingDir = projectDir
    ignoreFailures = project.hasProperty("ignoreFailures")
    outputs.upToDateWhen { false } // e2e tests depend on external systems; never skip as "up to date"

    System.getProperties()
        .filterKeys { key -> forwardedPropertyPrefixes.any { key.toString().startsWith(it) } }
        .forEach { (key, value) -> systemProperty(key.toString(), value) }
    systemProperty("allure.results.directory", layout.buildDirectory.dir("allure-results").get().asFile.absolutePath)

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = project.hasProperty("verbose")
    }
}

tasks.test {
    description = "Runs all Cucumber scenarios (default tag filter excludes @wip and @known-failure)."
    configureCucumberTask("src/test/resources/testng.xml", "CucumberTestRunner")
    // Start each run with a clean report; pass -PkeepAllureResults to accumulate results across runs.
    val staleDirs = listOfNotNull(
        layout.buildDirectory.dir("rerun").get().asFile,
        layout.buildDirectory.dir("allure-results").get().asFile.takeUnless { project.hasProperty("keepAllureResults") },
    )
    doFirst {
        staleDirs.forEach { it.deleteRecursively() }
    }
}

tasks.allureReport {
    clean = true // overwrite the previous report instead of failing because it exists
}

tasks.register<Test>("rerunFailed") {
    group = "verification"
    description = "Re-runs only the scenarios listed in build/rerun/failed.txt by the last test run."
    configureCucumberTask("src/test/resources/testng-rerun.xml", "RerunFailedRunner")
    systemProperty("log.file", layout.buildDirectory.file("logs/e2e-rerun.log").get().asFile.absolutePath)
    val rerunFile = layout.buildDirectory.file("rerun/failed.txt")
    onlyIf("there are failed scenarios to rerun") {
        rerunFile.get().asFile.let { it.exists() && it.readText().isNotBlank() }
    }
}

// All code lives in src/test, which the standard `javadoc` task doesn't cover.
tasks.register<Javadoc>("testJavadoc") {
    group = "documentation"
    description = "Generates Javadoc for the test framework into build/docs/testJavadoc and checks it with doclint."
    source = sourceSets.test.get().allJava
    classpath = sourceSets.test.get().compileClasspath + sourceSets.test.get().output
    setDestinationDir(layout.buildDirectory.dir("docs/testJavadoc").get().asFile)
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        windowTitle = "${project.name} test framework"
        addBooleanOption("Xdoclint:all", true)
        addStringOption("Xmaxwarns", "1000")
    }
}

val composeFile = "docker/docker-compose.yml"
val videoComposeFile = "docker/docker-compose.video.yml"

fun composeArgs(vararg args: String): List<String> {
    val files = mutableListOf("-f", composeFile)
    if (project.hasProperty("video")) files += listOf("-f", videoComposeFile)
    return listOf("docker", "compose") + files + args
}

tasks.register<Exec>("gridUp") {
    group = "selenium grid"
    description = "Starts the local Selenium Grid in Docker (add -Pvideo to record videos)."
    commandLine(composeArgs("up", "-d", "--wait"))
}

tasks.register<Exec>("gridDown") {
    group = "selenium grid"
    description = "Stops the local Selenium Grid."
    commandLine(composeArgs("down", "--remove-orphans"))
}
