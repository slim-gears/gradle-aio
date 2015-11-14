package com.slimgears.gradleaio.java

import me.tatarka.RetrolambdaPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Copy

class JavaAioPlugin implements Plugin<Project> {
    final Logger log = Logging.getLogger JavaAioPlugin

    class ProjectConfigurator {
        Project project
        Project rootProject
        JavaAioConfiguration config

        ProjectConfigurator(Project project) {
            this.project = project
            this.rootProject = project.rootProject
            this.config = new JavaAioConfiguration(project)
        }

        void applyJava() {
            project.apply plugin: 'java'
        }

        void applyRetrolambda() {
            log.info("Applying retrolambda configuration")
            project.apply plugin: RetrolambdaPlugin

            project.retrolambda {
                jdk System.getenv("JAVA_HOME")
                oldJdk System.getenv("JAVA_HOME")
                javaVersion JavaVersion.VERSION_1_7
                defaultMethods false
                incremental true
            }
        }

        void applyApt() {
            log.info("Applying java apt configuration")
            project.apply plugin: "net.ltgt.apt"
        }

        void applyUnitTests() {
            log.info("Applying unit test configuration")

            if (project.tasks.hasProperty('processTestResources')) {
                project.tasks.create(name: 'copyTestResources', type: Copy) {
                    from "${project.projectDir}/src/test/resources"
                    into "${project.buildDir}/classes/test"
                }
                project.tasks.processTestResources.dependsOn project.tasks.copyTestResources
            }

            if (project.tasks.hasProperty('processResources')) {
                project.tasks.create(name: 'copyResources', type: Copy) {
                    from "${project.projectDir}/src/main/resources"
                    into "${project.buildDir}/classes/main"
                }
                project.tasks.processResources.dependsOn project.tasks.copyResources
            }

            project.test {
                afterTest { descriptor, result ->
                    println "TEST ${result.resultType} - ${descriptor.name}"
                }
            }

            project.dependencies {
                testCompile "junit:junit:$config.junitVersion"
                testCompile "org.mockito:mockito-core:$config.mockitoVersion"
            }
        }
    }

    @Override
    void apply(Project project) {
        ProjectConfigurator configurator = new ProjectConfigurator(project)

        configurator.applyJava()
        configurator.applyRetrolambda()
        configurator.applyApt()
        configurator.applyUnitTests()
    }
}
