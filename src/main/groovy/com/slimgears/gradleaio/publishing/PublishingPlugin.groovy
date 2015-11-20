package com.slimgears.gradleaio.publishing

import com.jfrog.bintray.gradle.BintrayPlugin
import com.slimgears.gradleaio.internal.AbstractProjectConfigurator
import com.slimgears.gradleaio.internal.ProjectPlugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class PublishingPlugin extends ProjectPlugin<PublishingConfigurator> {
    PublishingPlugin() {
        super({project -> new PublishingConfigurator(project)})
    }

    static class PublishingConfigurator extends AbstractProjectConfigurator {
        @Lazy PublishingConfig config = { configByType(PublishingConfig) }()

        PublishingConfigurator(Project project) {
            super(project)
        }

        void applyMavenLocal() {
            project.apply plugin: 'maven-publish'

            project.publishing {
                repositories {
                    mavenLocal()
                }

                publications {
                    maven(MavenPublication) {
                        pom.withXml {
                            def dependenciesNode = asNode().appendNode('dependencies')

                            //Iterate over the compile dependencies (we don't want the test ones), adding a <dependency> node for each
                            project.configurations.compile.allDependencies.each {
                                def dependencyNode = dependenciesNode.appendNode('dependency')
                                dependencyNode.appendNode('groupId', it.group)
                                dependencyNode.appendNode('artifactId', it.name)
                                dependencyNode.appendNode('version', it.version)
                            }
                        }

                        artifact "${project.buildDir}/outputs/aar/${project.name}-release.aar"
                        artifact (project.sourceJar)
                    }
                }
            }
        }

        void applyBintray() {
            if (config.bintray) {
                project.apply plugin: BintrayPlugin
                project.bintray {
                    user = config.bintray.user
                    key = config.bintray.key
                    publications = ['maven'] //When uploading Maven-based publication files
                    dryRun = false //Whether to run this as dry-run, without deploying
                    publish = true //If version should be auto published after an upload

                    pkg {
                        repo = rootProject.name
                        name = project.name
                        userOrg = config.bintray.organization //An optional organization name when the repo belongs to one of the user's orgs
                        websiteUrl = config.bintray.websiteUrl
                        issueTrackerUrl = config.bintray.issueTrackerUrl
                        vcsUrl = config.bintray.vcsUrl
                        licenses = ['Apache-2.0']
                        publicDownloadNumbers = config.bintray.publicDownloadNumbers
                        desc = config.bintray.description
                        labels = config.bintray.labels
                        version {
                            name = project.version //Bintray logical version name
                            if (config.bintray.sonatypeUser && config.bintray.sonatypePassword) {
                                mavenCentralSync {
                                    sync = true //Optional (true by default). Determines whether to sync the version to Maven Central.
                                    user = config.bintray.sonatypeUser //OSS user token
                                    password = config.bintray.sonatypePassword //OSS user password
                                    close = '1' //Optional property. By default the staging repository is closed and artifacts are released to Maven Central. You can optionally turn this behaviour off (by puting 0 as value) and release the version manually.
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        void apply() {
            applyMavenLocal()
            applyBintray()
        }
    }
}
