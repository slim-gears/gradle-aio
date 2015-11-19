package com.slimgears.gradleaio.publishing

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

    BintrayConfig configureBintray() {
        return bintray = bintray ?: new BintrayConfig()
    }
}
