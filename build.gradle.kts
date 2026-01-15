plugins {
    java
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.portswigger.burp.extensions:montoya-api:2025.5")
    implementation("com.upokecenter:cbor:4.5.2")
    testImplementation(platform("org.junit:junit-bom:6.0.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    // Set the output JAR name
    archiveBaseName.set("CBOR_Wrangler")
    archiveVersion.set("")  // Remove version from filename
    
    // Include dependencies in the JAR
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    
    // Avoid duplicate files
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}