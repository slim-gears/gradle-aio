package com.slimgears.gradleaio.internal

import org.gradle.api.Project;

class BasicConfiguration extends AbstractConfiguration {
    String junitVersion = '4.12'
    String mockitoVersion = '2.0.31-beta'
    String robolectricVersion = '3.0'

    BasicConfiguration(Project project) {
        super(project)
    }
}
