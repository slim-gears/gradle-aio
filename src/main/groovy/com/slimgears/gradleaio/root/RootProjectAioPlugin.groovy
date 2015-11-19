package com.slimgears.gradleaio.root

import com.slimgears.gradleaio.internal.ConfigContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete;

class RootProjectAioPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.apply plugin: "idea"

        project.tasks.ideaProject.doLast {
            copy { from '.' into '.idea/' include '*.ipr' rename { "modules.xml" } }
            copy { from '.' into '.idea/' include '*.iws' rename { "workspace.xml" } }
            project.delete "${project.name}.ipr"
            project.delete "${project.name}.iws"
        }

        project.allprojects {
            it.extensions.create('aioConfig', ConfigContainer, it)

            it.apply plugin: "idea"
            it.repositories {
                jcenter()
                maven { url "https://jitpack.io" }
            }
        }

        project.tasks.create(name: 'clean', type: Delete) {
            delete project.rootProject.buildDir
        }
    }
}
