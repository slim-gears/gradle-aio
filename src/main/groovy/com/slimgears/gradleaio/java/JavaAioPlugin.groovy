package com.slimgears.gradleaio.java

import com.slimgears.gradleaio.internal.AbstractProjectConfigurator
import com.slimgears.gradleaio.internal.ProjectPlugin
import me.tatarka.RetrolambdaPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

class JavaAioPlugin extends ProjectPlugin<JavaAioConfigurator> {
    JavaAioPlugin() {
        super({project -> new JavaAioConfigurator(project)})
    }

    static class JavaAioConfigurator extends AbstractProjectConfigurator {
        @Lazy JavaAioConfig config = { configByType(JavaAioConfig) }()

        JavaAioConfigurator(Project project) {
            super(project)
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

        @Override
        void apply() {
            applyJava()
            applyRetrolambda()
            applyApt()
            applyUnitTests()
        }
    }
}
