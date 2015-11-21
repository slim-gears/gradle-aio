package com.slimgears.gradleaio.internal
import com.slimgears.gradleaio.android.AndroidAioApplicationConfig
import com.slimgears.gradleaio.android.AndroidAioConfig
import com.slimgears.gradleaio.java.JavaAioConfig
import com.slimgears.gradleaio.publishing.PublishingConfig
import org.gradle.api.Project

class ConfigContainer {
    final Project project
    final Map<Class, Object> configMap = new HashMap<>()

    public ConfigContainer(Project project) {
        this.project = project
    }

    public ConfigContainer getParentContainer() {
        return project.parent ? project.parent.extensions.findByType(this.class) : null
    }

    public <C> C configure(Class<C> type, Closure initializer) {
        C config = configByType(type)
        initializer.resolveStrategy = Closure.DELEGATE_FIRST
        initializer.delegate = config
        initializer()
        return config
    }

    public AndroidAioConfig androidAio(Closure initializer) {
        return configure(AndroidAioConfig, initializer)
    }

    public AndroidAioApplicationConfig androidAppAio(Closure initializer) {
        return configure(AndroidAioApplicationConfig, initializer)
    }

    public BasicConfig basicAio(Closure initializer) {
        return configure(BasicConfig, initializer)
    }

    public JavaAioConfig javaAio(Closure initializer) {
        return configure(JavaAioConfig, initializer)
    }

    public PublishingConfig publishingAio(Closure initializer) {
        return configure(PublishingConfig, initializer)
    }

    public <C> C configByType(Class<C> configClass) {
        if (configMap.containsKey(configClass)) {
            return (C)configMap.get(configClass)
        }

        C config = createConfig(configClass)
        configMap.put(configClass, config)
        return config
    }

    private <C> C createConfig(Class<C> type) {
        C config = parentContainer ? cloneConfig(parentContainer.configByType(type)) : type.newInstance()
        mergeProperties(type.superclass, config)
        mergeProperties(project, config)
        return config
    }

    private <C> C mergeProperties(Class type, C config) {
        if (type == Object) return config

        mergeProperties(type.superclass, config)
        Object configPart = configByType(type)
        mergeProperties(configPart, config)

        return config
    }

    private static <C> C cloneConfig(C config) {
        Class configClass = config.class
        C clonedConfig = configClass.newInstance()

        mergeProperties(config, clonedConfig)

        return clonedConfig
    }

    private static void mergeProperties(Object src, Object dest) {
        dest.properties.keySet()
            .findAll { String prop -> prop != 'class' && src.hasProperty(prop) && src[prop] }
            .each { String prop -> dest[prop] = src[prop] }
    }
}
