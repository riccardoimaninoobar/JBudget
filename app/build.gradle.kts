plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "it.unicam.cs.mpgc.jbudget123012"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}


repositories {
    mavenCentral()
}

dependencies {
    // Dipendenze per la persistenza
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.hibernate.orm:hibernate-core:6.4.4.Final")

    // Driver per il database H2
    runtimeOnly("com.h2database:h2:2.2.224")

    // Dipendenze per JavaFX
    implementation("org.openjfx:javafx-controls:25.0.1")
    implementation("org.openjfx:javafx-fxml:25.0.1")

    // Dipendenze per il logging
    implementation("org.slf4j:slf4j-api:2.0.12")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.12")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Necessario per il parsing di persistance.xml
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.5")
}

application {
    mainClass.set("it.unicam.cs.mpgc.jbudget123012.view.MainApp")
}

tasks.test {
    useJUnitPlatform()
}

javafx {
    version = "25.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.named<Delete>("clean") {
    delete(rootProject.file("data"))
    delete(file("data"))
}