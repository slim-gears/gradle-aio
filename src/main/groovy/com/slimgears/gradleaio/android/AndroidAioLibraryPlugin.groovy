package com.slimgears.gradleaio.android

import com.slimgears.gradleaio.internal.ProjectPlugin
import org.gradle.api.Project

class AndroidAioLibraryPlugin extends ProjectPlugin<AndroidAioLibraryConfigurator> {
    AndroidAioLibraryPlugin() {
        super({project -> new AndroidAioLibraryConfigurator(project)})
    }

    static class AndroidAioLibraryConfigurator extends AbstractAndroidAioConfigurator {
        AndroidAioLibraryConfigurator(Project project) {
            super(project, 'library')
        }

        @Override void applyAndroid() {
            super.applyAndroid()
            project.android {
                buildTypes {
                    debug {
                        minifyEnabled false
                        shrinkResources false
                    }

                    release {
                        minifyEnabled false
                        shrinkResources false
                    }
                }
            }
        }
    }
}
