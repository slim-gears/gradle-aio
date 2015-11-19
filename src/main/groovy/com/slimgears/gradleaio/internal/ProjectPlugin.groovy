package com.slimgears.gradleaio.internal

import org.gradle.api.Plugin
import org.gradle.api.Project;

class ProjectPlugin<Configurator extends AbstractProjectConfigurator> implements Plugin<Project> {
    ProjectConfiguratorFactory<Configurator> configuratorFactory

    ProjectPlugin(ProjectConfiguratorFactory<Configurator> factory) {
        this.configuratorFactory = factory
    }

    @Override
    void apply(Project project) {
        configuratorFactory.createConfigurator(project).apply()
    }
}
