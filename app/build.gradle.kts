import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.dnscontrol"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dnscontrol"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "0.3"
        
        val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
    }
    
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val date = SimpleDateFormat("yyyy-MM-dd").format(Date())
            output.outputFileName = "dns-control-${variant.versionName}-${date}.apk"
        }
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

// Ensure build time is regenerated on each build
tasks.matching { it.name.contains("generateDebugBuildConfig") || it.name.contains("generateReleaseBuildConfig") }
    .configureEach {
        outputs.upToDateWhen { false }
    }

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
}
