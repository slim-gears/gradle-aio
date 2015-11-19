package com.slimgears.gradleaio.internal

import org.gradle.api.Project;

interface ProjectConfiguratorFactory<Configurator extends AbstractProjectConfigurator> {
    Configurator createConfigurator(Project project);
}
