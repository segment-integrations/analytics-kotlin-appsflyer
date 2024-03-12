plugins {
    id("com.android.library")
    kotlin("android")

    id("org.jetbrains.kotlin.plugin.serialization")
    id("mvn-publish")
}

val VERSION_NAME: String by project

android {
    compileSdk = 33

    defaultConfig {
        multiDexEnabled = true
        minSdk = 16
        targetSdk = 33

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("proguard-consumer-rules.pro")

        buildConfigField("String", "VERSION_NAME", "\"$VERSION_NAME\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("com.segment.analytics.kotlin:android:1.14.2")
    implementation("androidx.multidex:multidex:2.0.1")

    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.lifecycle:lifecycle-process:2.4.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.4.1")
}

// Partner Dependencies
dependencies {
    implementation("com.appsflyer:af-android-sdk:6.13.0")
    implementation ("com.android.installreferrer:installreferrer:2.2")
}

// Test Dependencies
dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    testImplementation("io.mockk:mockk:1.12.4")
    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Add Roboelectric dependencies.
    testImplementation("org.robolectric:robolectric:4.7.3")
    testImplementation("androidx.test:core:1.5.0")

    // Add JUnit4 legacy dependencies.
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")

    // For JSON Object testing
    testImplementation("org.json:json:20200518")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// required for mvn-publish
// too bad we can't move it into mvn-publish plugin because `android`is only accessible here
tasks {
    val sourceFiles = android.sourceSets.getByName("main").java.srcDirs

    register<Javadoc>("withJavadoc") {
        isFailOnError = false

        setSource(sourceFiles)

        // add Android runtime classpath
        android.bootClasspath.forEach { classpath += project.fileTree(it) }

        // add classpath for all dependencies
        android.libraryVariants.forEach { variant ->
            variant.javaCompileProvider.get().classpath.files.forEach { file ->
                classpath += project.fileTree(file)
            }
        }
    }

    register<Jar>("withJavadocJar") {
        archiveClassifier.set("javadoc")
        dependsOn(named("withJavadoc"))
        val destination = named<Javadoc>("withJavadoc").get().destinationDir
        from(destination)
    }

    register<Jar>("withSourcesJar") {
        archiveClassifier.set("sources")
        from(sourceFiles)
    }
}