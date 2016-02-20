package com.slimgears.gradleaio.root
import com.slimgears.gradleaio.internal.ConfigContainer
import groovy.xml.XmlUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class RootProjectAioPlugin implements Plugin<Project> {
    void writeXml(Node rootNode, String path) {
        def filePrinter = new PrintWriter(path)
        def xmlPrinter = new XmlNodePrinter(filePrinter)
        filePrinter.println('<?xml version="1.0" encoding="UTF-8"?>')
        xmlPrinter.print(rootNode)
    }
    
    Node createProjectRootNode() {
        return new Node(null, 'project', [version: 4])
    }
    
    String trimPath(String path) {
        return path.endsWith('/') ? path.substring(0, path.length() - 1) : path
    }
    
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
            def modulesRootNode = createProjectRootNode()
            def modulesNode = modulesRootNode
                    .appendNode('component', [name: 'ProjectModuleManager'])
                    .appendNode('modules')
            
            def gradleRootNode = createProjectRootNode()
            def gradleNode = gradleRootNode
                    .appendNode('component', [name: 'GradleSettings'])
                    .appendNode('option', [name: 'linkedExternalProjectsSettings'])
                    .appendNode('GradleProjectSettings')

            def rootProjectUri = project.projectDir.toURI().toString()
            def gradleHomeUri = project.gradle.gradleHomeDir.toURI().toString()
            def isWrapper = gradleHomeUri.startsWith(rootProjectUri)
            def gradleHomeDir = isWrapper 
                ? '$PROJECT_DIR$' + File.separator + gradleHomeUri.substring(rootProjectUri.length())
                : project.gradle.gradleHomeDir
            
            gradleNode.appendNode('option', [name: 'distributionType', value: isWrapper ? 'DEFAULT_WRAPPED' : 'LOCAL'])
            gradleNode.appendNode('option', [name: 'externalProjectPath', value: '$PROJECT_DIR$'])
            gradleNode.appendNode('option', [name: 'gradleHome', value: gradleHomeDir])
            def gradleModulesSetNode = gradleNode
                    .appendNode('option', [name: 'modules'])
                    .appendNode('set')
            def gradleMyModulesSetNode = gradleNode
                    .appendNode('option', [name: 'myModules'])
                    .appendNode('set')
                    
            project.allprojects {
                def projectUri = it.projectDir.toURI().toString()
                def relativeDir = projectUri.toString().substring(rootProjectUri.length())
                def projectDir = trimPath("\$PROJECT_DIR\$/${relativeDir}")
                modulesNode.appendNode('module', [
                        fileurl: "file://\$PROJECT_DIR\$/${relativeDir}${it.name}.iml",
                        filepath: "\$PROJECT_DIR\$/${relativeDir}${it.name}.iml"])
                
                gradleModulesSetNode.appendNode('option', [value: projectDir])
                gradleMyModulesSetNode.appendNode('option', [value: projectDir])
            }

            project.file('.idea').mkdir()
            writeXml(modulesRootNode, "$project.projectDir/.idea/modules.xml")
            writeXml(gradleRootNode, "$project.projectDir/.idea/gradle.xml")
            project.delete "${project.name}.ipr"
        }

        project.tasks.ideaWorkspace.doLast {
            project.copy { from '.' into '.idea/' include "${project.name}.iws" rename { "workspace.xml" } }
            project.delete "${project.name}.iws"
        }
    }
}
