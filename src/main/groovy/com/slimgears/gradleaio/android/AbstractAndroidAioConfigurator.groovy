package com.slimgears.gradleaio.android
import com.jakewharton.sdkmanager.SdkManagerPlugin
import com.neenbedankt.gradle.androidapt.AndroidAptPlugin
import com.slimgears.gradleaio.internal.AbstractProjectConfigurator
import me.tatarka.RetrolambdaPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

class AbstractAndroidAioConfigurator extends AbstractProjectConfigurator {
    final String projectType
    @Lazy AndroidAioConfig config = { configByType(AndroidAioConfig) }()

    AbstractAndroidAioConfigurator(Project project, String projectType) {
        super(project)
        this.projectType = projectType
    }

    void applySdkManager() {
        log.info("Applying sdk manager configuration")
        project.apply plugin: SdkManagerPlugin
    }

    void applyRetrolambda() {
        log.info("Applying retrolambda configuration")
        project.apply plugin: RetrolambdaPlugin

        project.sourceCompatibility = JavaVersion.VERSION_1_8

        project.retrolambda {
            jdk System.getenv("JAVA_HOME")
            oldJdk System.getenv("JAVA_HOME")
            javaVersion JavaVersion.VERSION_1_7
            defaultMethods false
            incremental true
        }
    }

    void applyAndroid() {
        log.info("Applying android configuration")

        project.apply plugin: "com.android.$projectType"

        project.android {
            compileSdkVersion config.compileSdkVersion
            buildToolsVersion config.buildToolsVersion

            packagingOptions {
                exclude 'META-INF/DEPENDENCIES'
                exclude 'META-INF/services/javax.annotation.processing.Processor'
                exclude 'META-INF/LICENSE'
                exclude 'META-INF/LICENSE.txt'
                exclude 'META-INF/NOTICE'
                exclude 'META-INF/NOTICE.txt'
            }

            compileOptions {
                sourceCompatibility JavaVersion.VERSION_1_8
                targetCompatibility JavaVersion.VERSION_1_8
            }

            lintOptions {
                checkReleaseBuilds true
                abortOnError false
            }

            defaultConfig {
                minSdkVersion config.minSdkVersion
                targetSdkVersion config.targetSdkVersion
            }
        }

        project.repositories {
            maven {
                url "${getLocalProperties().getProperty('sdk.dir')}/extras/android/m2repository/"
            }
        }
    }

    void applyApt() {
        log.info("Applying android apt configuration")

        project.apply plugin: AndroidAptPlugin
        project.configurations {
            apt
        }
    }

    void applyUnitTests() {
        log.info("Applying unit test configuration")

        project.android.testOptions.unitTests.all {
            afterTest { descriptor, result ->
                println "TEST ${result.resultType} - ${descriptor.name}"
            }
        }

        project.dependencies {
            testCompile "junit:junit:$config.junitVersion"
            testCompile "org.mockito:mockito-core:$config.mockitoVersion"
            testCompile "org.robolectric:shadows-httpclient:$config.robolectricVersion"
            testCompile "org.robolectric:robolectric:$config.robolectricVersion"
        }
    }

    void applyPlayServices() {
        if (config.usePlayServices && !config.usePlayServices.isEmpty()) {
            project.apply plugin: "com.google.gms.google-services"

            config.usePlayServices.each { service ->
                project.dependencies {
                    compile "com.google.android.gms:play-services-$service:$config.playServicesVersion"
                }
            }
        }
    }

    void applySupportLibraries() {
        if (config.useSupportLibraries && !config.useSupportLibraries.isEmpty()) {
            config.useSupportLibraries.each { lib ->
                project.dependencies {
                    compile "com.android.support:$lib:$config.buildToolsVersion"
                }
            }
        }
    }

    void applySourceJar() {
        project.tasks.create(name: 'sourceJar', type: Jar) {
            classifier = 'sources'
            from project.android.sourceSets.main.java.srcDirs
        }
    }

    Properties getLocalProperties() {
        Properties properties = new Properties()
        properties.load(rootProject.file('local.properties').newDataInputStream())
        return properties
    }

    @Override
    void apply() {
        applySdkManager()
        applyAndroid()
        applyRetrolambda()
        applyApt()
        applyUnitTests()
        applyPlayServices()
        applySupportLibraries()
        applySourceJar()
    }
}
