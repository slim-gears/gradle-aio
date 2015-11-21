package com.slimgears.gradleaio.groovy
import com.slimgears.gradleaio.internal.AbstractProjectConfigurator
import com.slimgears.gradleaio.internal.ProjectPlugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

class GroovyAioPlugin extends ProjectPlugin<GroovyAioConfigurator> {
    GroovyAioPlugin() {
        super({project -> new GroovyAioConfigurator(project) });
    }

    static class GroovyAioConfigurator extends AbstractProjectConfigurator {
        public GroovyAioConfigurator(Project project) {
            super(project);
        }

        @Override
        public void apply() {
            project.apply plugin: 'groovy'
            project.tasks.create(name: 'sourceJar', type: Jar) {
                classifier = 'sources'
                from project.sourceSets.main.allSource
            }
        }
    }
}
