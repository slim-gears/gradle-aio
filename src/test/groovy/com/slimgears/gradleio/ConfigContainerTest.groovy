package com.slimgears.gradleio

import com.slimgears.gradleaio.android.AndroidAioApplicationConfig
import com.slimgears.gradleaio.android.AndroidAioConfig
import com.slimgears.gradleaio.internal.BasicConfig
import com.slimgears.gradleaio.internal.ConfigContainer
import com.slimgears.gradleaio.publishing.PublishingConfig
import com.slimgears.gradleaio.root.RootProjectAioPlugin
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

    @Test void configurationClassHierarchy_shouldReturnCorrectValues() {
        assertNotNull(configContainer)

        rootProject.extensions.findByType(ConfigContainer).configByType(BasicConfig).with {
            junitVersion = '4.10'
        }

        configContainer.configByType(AndroidAioConfig).with {
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

    @Test void configurationProjectHierarchy_shouldReturnCorrectValues() {
        rootProject = ProjectBuilder.builder().build()
        project = ProjectBuilder.builder().withParent(rootProject).build()

        rootProject.apply plugin: RootProjectAioPlugin

        rootProject.aioConfig {
            publishingAio {
                bintrayUser = 'user'
                bintrayKey = 'key'
            }
        }

        project.aioConfig {
            publishingAio {
                description = 'package description'
            }
        }


        PublishingConfig config = project.aioConfig.configByType(PublishingConfig)
        assertEquals('key', config.bintrayKey)
        assertEquals('user', config.bintrayUser)
        assertEquals('package description', config.description)
    }

    @Test void configurationClosureBinding_usesInnerScopeFirst() {
        project.with {
            ext {
                bintrayUser = 'outerUser'
            }
            configContainer.publishingAio {
                bintrayUser = 'innerUser'
            }
        }

        assertEquals('innerUser', configContainer.configByType(PublishingConfig).bintrayUser)
        assertEquals('outerUser', project.bintrayUser)
    }

    @Test void publishingConfigGithubValues() {
        configContainer.publishingAio {
            githubUser = 'test-user'
            repository = 'test-repo'
        }

        PublishingConfig config = configContainer.configByType(PublishingConfig)

        assertEquals('https://github.com/test-user/test-repo', config.websiteUrl)
        assertEquals('https://github.com/test-user/test-repo/issues', config.issueTrackerUrl)
        assertEquals('https://github.com/test-user/test-repo.git', config.vcsUrl)
    }
}
