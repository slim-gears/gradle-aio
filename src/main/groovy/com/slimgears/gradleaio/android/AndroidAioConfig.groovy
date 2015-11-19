package com.slimgears.gradleaio.android

import com.slimgears.gradleaio.internal.BasicConfig
import org.gradle.api.Project

class AndroidAioConfig extends BasicConfig {
    Integer minSdkVersion = 10
    Integer targetSdkVersion = 23
    Integer compileSdkVersion = 23
    String buildToolsVersion = '23.0.1'

    String playServicesVersion = '8.3.0'
    List<String> usePlayServices = null
    List<String> useSupportLibraries = null

    String robolectricVersion = '3.0'
}
