package com.slimgears.gradleaio.publishing

import org.gradle.api.Action

import java.util.concurrent.Callable

class PublishingConfig {
    class BintrayConfig {
        String user
        String key
        String organization
        String websiteUrl
        String issueTrackerUrl
        String vcsUrl
        List<String> labels
        String description
        String sonatypeUser
        String sonatypePassword
        boolean publicDownloadNumbers = true
    }

    String artifactId = null
    BintrayConfig bintray = null

    BintrayConfig bintray(Closure initializer) {
        def config = this.bintray = this.bintray ?: new BintrayConfig()
        initializer.delegate = config
        initializer()

        return config
    }
}
