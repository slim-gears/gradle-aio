package com.slimgears.gradleio
import com.neenbedankt.gradle.androidapt.AndroidAptPlugin
import com.slimgears.gradleaio.android.AndroidAioApplicationPlugin
import com.slimgears.gradleaio.android.AndroidAioConfig
import com.slimgears.gradleaio.android.AndroidAioLibraryPlugin
import com.slimgears.gradleaio.internal.ConfigContainer
import com.slimgears.gradleaio.java.JavaAioPlugin
import com.slimgears.gradleaio.publishing.PublishingPlugin
import com.slimgears.gradleaio.root.RootProjectAioPlugin
import me.tatarka.RetrolambdaPlugin
import net.ltgt.gradle.apt.AptPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4)
class AioPluginsTest {
    Project project

    @Before void setUp() {
        project = ProjectBuilder.builder()
                .withName('test-project')
                .build()

        project.apply plugin: RootProjectAioPlugin

        def configContainer = project.extensions.findByType(ConfigContainer)

        configContainer.androidAio {
                minSdkVersion = 16
                targetSdkVersion = 24
                useSupportLibraries = ['recyclerview-v7']
                usePlayServices = ['identity', 'plus', 'auth']
        }

        configContainer.androidAppAio {
            keyStoreFile = 'test-file'
            keyStorePassword = 'test-password'
            keyPassword = 'test-password'
        }

        configContainer.publishingAio {
            artifactId = 'test-artifact'
            bintray {
                user = 'test-user'
                key = 'test-key'
            }
        }
    }

    @Test void applyAndroidLibraryAio_appliesRequiredPlugins() {
        project.apply plugin: AndroidAioLibraryPlugin
        Assert.assertTrue(project.plugins.hasPlugin('com.android.library'))
        Assert.assertFalse(project.plugins.hasPlugin('com.android.application'))
        Assert.assertTrue(project.plugins.hasPlugin('com.google.gms.google-services'))
        Assert.assertTrue(project.plugins.hasPlugin(AndroidAptPlugin))
        Assert.assertTrue(project.plugins.hasPlugin(RetrolambdaPlugin))
        Assert.assertTrue(project.tasks.sourceJar != null)
    }

    @Test void applyAndroidApplicationAio_appliesRequiredPlugins() {
        project.apply plugin: AndroidAioApplicationPlugin
        Assert.assertTrue(project.pluginManager.hasPlugin('com.android.application'))
        Assert.assertFalse(project.pluginManager.hasPlugin('com.android.library'))
        Assert.assertTrue(project.plugins.hasPlugin(AndroidAptPlugin))
        Assert.assertTrue(project.plugins.hasPlugin(RetrolambdaPlugin))
        Assert.assertTrue(project.tasks.hasProperty('sourceJar').asBoolean())
    }

    @Test void applyJavaAio_appliesRequiredPlugins() {
        project.apply plugin: JavaAioPlugin

        Assert.assertTrue(project.plugins.hasPlugin('java'))
        Assert.assertTrue(project.plugins.hasPlugin(AptPlugin))
        Assert.assertTrue(project.plugins.hasPlugin(RetrolambdaPlugin))
        Assert.assertTrue(project.tasks.hasProperty('sourceJar').asBoolean())
    }

    @Test void applyJavaAioPublishingAio_appliesRequiredPlugins() {
        project.apply plugin: JavaAioPlugin
        project.apply plugin: PublishingPlugin

        Assert.assertTrue(project.plugins.hasPlugin('maven-publish'))
        Assert.assertTrue(project.plugins.hasPlugin('com.jfrog.bintray'))
    }

    @Test void applyAndroidAioPublishingAio_appliesRequiredPlugins() {
        project.apply plugin: AndroidAioLibraryPlugin
        project.apply plugin: PublishingPlugin

        Assert.assertTrue(project.plugins.hasPlugin('maven-publish'))
        Assert.assertTrue(project.plugins.hasPlugin('com.jfrog.bintray'))
    }

    @Test void applyAndroidApplicationAioWithoutRootAio_shouldSucceed() {
        def localProject = ProjectBuilder.builder().build()
        localProject.apply plugin: AndroidAioApplicationPlugin
    }

    @Test void configurationUsingExtension_shouldNotThrow() {
        project.aioConfig {
            androidAio {
                minSdkVersion = 14
            }
        }

        Assert.assertEquals(14,
                project.extensions
                    .findByType(ConfigContainer)
                    .configByType(AndroidAioConfig)
                    .minSdkVersion)
    }
}
