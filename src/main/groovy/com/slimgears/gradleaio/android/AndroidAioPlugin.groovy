package com.slimgears.gradleaio.android

import me.tatarka.RetrolambdaPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.jakewharton.sdkmanager.SdkManagerPlugin
import com.neenbedankt.gradle.androidapt.AndroidAptPlugin
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class AndroidAioPlugin implements Plugin<Project> {
    final Logger log = Logging.getLogger AndroidAioPlugin

    String projectType
    boolean isApplicationProject


    class ProjectConfigurator {
        Project project
        Project rootProject
        AndroidAioConfiguration config

        ProjectConfigurator(Project project) {
            this.project = project
            this.rootProject = project.rootProject
            this.config = new AndroidAioConfiguration(project)
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

                if (isApplicationProject) {
                    signingConfigs {
                        release {
                            keyAlias project.name
                            storeFile project.file(config.keyStoreFile)
                            storePassword config.keyStorePassword
                            keyPassword config.keyPassword
                        }
                    }
                }

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
                    versionCode config.versionCode
                    versionName config.versionName
                }

                buildTypes {
                    debug {
                        minifyEnabled false
                        shrinkResources false
                        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
                    }

                    release {
                        minifyEnabled isApplicationProject
                        shrinkResources false
                        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'

                        if (isApplicationProject) {
                            signingConfig signingConfigs.release
                        }
                    }
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

        Properties getLocalProperties() {
            Properties properties = new Properties()
            properties.load(rootProject.file('local.properties').newDataInputStream())
            return properties
        }
    }

    @Override
    void apply(Project project) {
        ProjectConfigurator configurator = new ProjectConfigurator(project)

        configurator.applySdkManager()
        configurator.applyAndroid()
        configurator.applyRetrolambda()
        configurator.applyApt()
        configurator.applyUnitTests()
        configurator.applyPlayServices()
        configurator.applySupportLibraries()
    }
}
