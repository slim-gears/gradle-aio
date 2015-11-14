package com.slimgears.gradleaio.internal

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class AbstractConfiguration {
    final Logger log

    AbstractConfiguration(Project project) {
        log = Logging.getLogger this.class
        setPropertiesFromProject(project)
    }

    void setPropertiesFromProject(Project project) {
        setPropertiesFromProject(this.class, project)
    }

    void setPropertiesFromProject(Class cls, Project project) {
        if (cls != Object) setPropertiesFromProject(cls.superclass, project)

        if (project.rootProject != project) {
            setPropertiesFromProject(cls, project.rootProject)
        }

        cls.declaredFields.each { field ->
            def value = project.properties.get(field.name)
            if (value) {
                field.setAccessible(true)
                field.set(this, value)
                log.info("Configuration property ${project.name}.$field.name: ${this.properties.get(field.name)}")
            }
        }
    }
}
