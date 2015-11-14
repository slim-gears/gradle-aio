package com.slimgears.gradleaio.android

import com.slimgears.gradleaio.internal.BasicConfiguration
import org.gradle.api.Project

class AndroidAioConfiguration extends BasicConfiguration {
    Integer minSdkVersion = 10
    Integer targetSdkVersion = 23
    Integer compileSdkVersion = 23
    String buildToolsVersion = '23.0.1'

    Integer versionCode
    String versionName

    String keyStoreFile
    String keyStorePassword
    String keyPassword

    String playServicesVersion = '8.3.0'
    List<String> usePlayServices
    List<String> useSupportLibraries

    AndroidAioConfiguration(Project project) {
        super(project)
        setPropertiesFromProject(AndroidAioConfiguration, project)
    }
}
