package com.slimgears.gradleaio.root
import com.slimgears.gradleaio.internal.ConfigContainer
import groovy.xml.XmlUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class RootProjectAioPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.allprojects {
            it.extensions.create('aioConfig', ConfigContainer, it)

            it.apply plugin: "idea"
            it.repositories {
                jcenter()
                maven { url "https://jitpack.io" }
            }
        }

        project.idea.workspace.iws.withXml { provider ->
            def runManagerNode = provider.asNode().find { it.@name == 'RunManager'}
            def defaultJunitConfig = runManagerNode.find { it.@type == 'JUnit' && it.@default }
            defaultJunitConfig.find { it.@name == 'TEST_OBJECT' }.@value = "package"
            defaultJunitConfig.find { it.@name == 'WORKING_DIRECTORY' }.@value = '$MODULE_DIR$'
            defaultJunitConfig.find { it.@name == 'VM_PARAMETERS'}.@value = '-ea'
            defaultJunitConfig.find { it.@name == 'PASS_PARENT_ENVS'}.@value = true
            defaultJunitConfig.find { it.@name == 'TEST_SEARCH_SCOPE' }.value.@defaultName = 'singleModule'

            project.allprojects {
                def configNode = runManagerNode.appendNode('configuration', [default: false, name: "$it.name test", type: 'JUnit', factoryName: 'JUnit'])
                configNode.appendNode('module', [name: it.name])
                configNode.appendNode('option', [name: 'TEST_OBJECT', value: 'package'])
                configNode.appendNode('option', [name: 'VM_PARAMETERS', value: '-ea'])
                configNode.appendNode('option', [name: 'WORKING_DIRECTORY', value: '$MODULE_DIR$'])
                configNode.appendNode('option', [name: 'PASS_PARENT_ENVS', value: true])
                configNode.appendNode('option', [name: 'TEST_SEARCH_SCOPE'])
                        .appendNode('value', [defaultName: 'singleModule'])
            }
        }

        project.tasks.ideaProject.doLast {
            def rootNode = new Node(null, 'project', [version: 4])
            def modulesNode = rootNode
                    .appendNode('component', [name: 'ProjectModuleManager'])
                    .appendNode('modules')

            def rootUri = project.projectDir.toURI()
            project.allprojects {
                def projectUri = it.projectDir.toURI()
                def relativeDir = projectUri.toString().substring(rootUri.toString().length())
                modulesNode.appendNode('module', [
                        fileurl: "file://\$PROJECT_DIR\$/${relativeDir}${it.name}.iml",
                        filepath: "\$PROJECT_DIR\$/${relativeDir}${it.name}.iml"])
            }

            project.file('.idea').mkdir()
            def filePrinter = new PrintWriter("$project.projectDir/.idea/modules.xml")
            def xmlPrinter = new XmlNodePrinter(filePrinter)
            filePrinter.println('<?xml version="1.0" encoding="UTF-8"?>')
            xmlPrinter.print(rootNode)
            project.delete "${project.name}.ipr"
        }

        project.tasks.ideaWorkspace.doLast {
            project.copy { from '.' into '.idea/' include "${project.name}.iws" rename { "workspace.xml" } }
            project.delete "${project.name}.iws"
        }
    }
}
