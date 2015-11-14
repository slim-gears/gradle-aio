package com.slimgears.gradleaio.internal

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class AbstractConfiguration {
    final Logger log

    AbstractConfiguration() {
        log = Logging.getLogger this.class
    }

    protected <T extends AbstractConfiguration> void setPropertiesFromProject(Class<T> cls, Project project) {
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
