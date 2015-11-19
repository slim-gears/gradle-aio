package com.slimgears.gradleio

import com.slimgears.gradleaio.android.AndroidAioApplicationConfig
import com.slimgears.gradleaio.android.AndroidAioConfig
import com.slimgears.gradleaio.internal.BasicConfig
import com.slimgears.gradleaio.internal.ConfigContainer
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import static org.junit.Assert.*

@RunWith(JUnit4.class)
class ConfigContainerTest {
    Project rootProject
    Project project
    ConfigContainer configContainer

    @Before void setUp() {
        rootProject = ProjectBuilder.builder().withName('root-project').build()
        project = ProjectBuilder.builder().withParent(rootProject).withName('child-project').build()
        rootProject.allprojects.each { p -> p.extensions.create('aioConfig', ConfigContainer, p) }
        configContainer = project.extensions.findByType(ConfigContainer)
    }

    @Test void configurationHierarchy_shouldReturnCorrectValues() {
        assertNotNull(configContainer)

        rootProject.extensions.findByType(ConfigContainer).configure(BasicConfig).with {
            junitVersion = '4.10'
        }

        configContainer.configure(AndroidAioConfig).with {
            minSdkVersion = 1
            targetSdkVersion = 24
        }

        AndroidAioConfig aioConfig = configContainer.configByType(AndroidAioConfig)
        assertEquals(1, aioConfig.minSdkVersion)
        assertEquals(24, aioConfig.targetSdkVersion)

        BasicConfig basicConfig = configContainer.configByType(BasicConfig)
        assertEquals('4.10', basicConfig.junitVersion)

        AndroidAioApplicationConfig appConfig = configContainer.configByType(AndroidAioApplicationConfig)
        assertEquals(1, appConfig.minSdkVersion)
        assertEquals(24, appConfig.targetSdkVersion)
        assertEquals('4.10', appConfig.junitVersion)
    }
}
