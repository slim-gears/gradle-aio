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
            log.info('Applying maven local publish')

            project.apply plugin: 'maven-publish'

            project.publishing {
                repositories {
                    mavenLocal()
                }

                publications {
                    maven(MavenPublication) {
                        if (project.components.hasProperty('java')) {
                            from project.components.java
                        }

                        if (project.plugins.hasPlugin('com.android.library')) {
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
                        }

                        artifact (project.tasks.sourceJar)
                    }
                }

                project.tasks.create(name: 'install', dependsOn: 'publishMavenPublicationToMavenLocal') << {
                    log.info "Installing $project.name"
                }
            }
        }

        void applyBintray() {
            if (!config.bintrayUser || !config.bintrayKey) return

            log.info('Applying bintray publish')

            project.apply plugin: BintrayPlugin
            project.bintray {
                user = config.bintrayUser
                key = config.bintrayKey
                publications = ['maven'] //When uploading Maven-based publication files
                dryRun = false //Whether to run this as dry-run, without deploying
                publish = !project.version.toString().contains('SNAPSHOT')

                pkg {
                    repo = config.repository ?: project.rootProject.name
                    name = project.name
                    userOrg = config.organization //An optional organization name when the repo belongs to one of the user's orgs
                    websiteUrl = config.websiteUrl
                    issueTrackerUrl = config.issueTrackerUrl
                    vcsUrl = config.vcsUrl
                    licenses = config.licenses
                    publicDownloadNumbers = true
                    desc = config.description
                    labels = config.labels
                    version {
                        name = project.version //Bintray logical version name
                        if (config.sonatypeUser && config.sonatypePassword) {
                            mavenCentralSync {
                                sync = true //Optional (true by default). Determines whether to sync the version to Maven Central.
                                user = config.sonatypeUser //OSS user token
                                password = config.sonatypePassword //OSS user password
                                close = '1' //Optional property. By default the staging repository is closed and artifacts are released to Maven Central. You can optionally turn this behaviour off (by puting 0 as value) and release the version manually.
                            }
                        }
                    }
                }
            }

            project.tasks.create(name: 'upload', dependsOn: 'bintrayUpload')
        }

        @Override
        void apply() {
            if (!config.repository) config.repository = project.rootProject.name

            applyMavenLocal()
            applyBintray()
        }
    }
}
