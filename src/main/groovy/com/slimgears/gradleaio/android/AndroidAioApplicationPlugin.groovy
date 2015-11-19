package com.slimgears.gradleaio.android

import com.slimgears.gradleaio.internal.ProjectPlugin
import org.gradle.api.Project

class AndroidAioApplicationPlugin extends ProjectPlugin<AndroidAioApplicationConfigurator> {
    AndroidAioApplicationPlugin() {
        super({project -> new AndroidAioApplicationConfigurator(project)})
    }

    static class AndroidAioApplicationConfigurator extends AbstractAndroidAioConfigurator {
        @Lazy AndroidAioApplicationConfig config = { configByType(AndroidAioApplicationConfig) }()

        AndroidAioApplicationConfigurator(Project project) {
            super(project, 'application')
        }

        void applySigningConfig() {
            log.info("Applying signing")

            if (config.keyStoreFile) {
                project.android {
                    signingConfigs {
                        release {
                            keyAlias project.name
                            storeFile project.file(config.keyStoreFile)
                            storePassword config.keyStorePassword
                            keyPassword config.keyPassword
                        }
                    }

                    buildTypes {
                        release {
                            signingConfig signingConfigs.release
                        }
                    }
                }
            }
        }

        void applyVersion() {
            log.info("Applying apk version")

            project.android {
                defaultConfig {
                    versionCode config.versionCode
                    versionName config.versionName
                }
            }
        }

        void applyMinification() {
            log.info("Applying minification")

            project.android {
                buildTypes {
                    debug {
                        minifyEnabled false
                        shrinkResources false
                    }

                    release {
                        minifyEnabled true
                        shrinkResources true
                        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
                    }
                }
            }
        }

        @Override
        void applyAndroid() {
            super.applyAndroid()
            applySigningConfig()
            applyVersion()
            applyMinification()
        }
    }
}
