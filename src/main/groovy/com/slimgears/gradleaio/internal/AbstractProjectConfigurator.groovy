package com.slimgears.gradleaio.internal

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging;

abstract class AbstractProjectConfigurator {
    final Logger log = Logging.getLogger AbstractProjectConfigurator
    private final ConfigContainer configContainer

    protected final Project project
    protected final Project rootProject

    AbstractProjectConfigurator(Project project) {
        this.project = project
        this.rootProject = project.rootProject
        this.configContainer = project.extensions.findByType(ConfigContainer) ?: new ConfigContainer(project)
    }

    protected <C> C configByType(Class<C> type) {
        return configContainer.configByType(type)
    }

    abstract void apply();
}
