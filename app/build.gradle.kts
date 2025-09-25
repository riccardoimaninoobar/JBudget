plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "it.unicam.cs.mpgc.jbudget123012"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")

    // Dipendenze per il logging
    implementation("org.slf4j:slf4j-api:2.0.12")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.12")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
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
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.named<Delete>("clean") {
    delete(rootProject.file("data"))
    delete(file("data"))
}