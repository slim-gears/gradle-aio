package com.slimgears.gradleaio.publishing

class PublishingConfig {
    private String _websiteUrl
    private String _issueTrackerUrl
    private String _githubUser
    private String _organization
    private String _vcsUrl

    String bintrayUser = System.getenv('BINTRAY_USER')
    String bintrayKey = System.getenv('BINTRAY_KEY')
    String githubOrg

    String repository
    List<String> labels
    List<String> licenses = ['Apache-2.0']
    String description
    String sonatypeUser
    String sonatypePassword

    boolean getIsGithubProject() {
        return githubUser && repository
    }

    void setWebsiteUrl(String url) {
        _websiteUrl = url
    }

    String getWebsiteUrl() {
        return this._websiteUrl ?: isGithubProject ? "https://github.com/${githubUser}/${repository}" : null
    }

    void setGithubUser(String user) {
        _githubUser = user
    }

    String getGithubUser() {
        return this._githubUser ?: this.githubOrg
    }

    void setIssueTrackerUrl(String url) {
        _issueTrackerUrl = url
    }

    String getIssueTrackerUrl() {
        return this._issueTrackerUrl ?: isGithubProject ? "https://github.com/${githubUser}/${repository}/issues" : null
    }

    void setOrganization(String org) {
        _organization = org
    }

    String getOrganization() {
        return this._organization ?: this.githubOrg
    }

    void setVcsUrl(String url) {
        _vcsUrl = url
    }

    String getVcsUrl() {
        return this._vcsUrl ?: isGithubProject ? "https://github.com/${githubUser}/${repository}.git" : null
    }
}
