Gradle Android and Java All-In-One
==================================

This plugin's purpose is to reduce a gradle boilerplate for typical `build.gradle` scripts for
and java gradle projects.

This plugins automatically applies the following plugins for android project:
* Android SDK manager plugin - automated resolving of all required android libraries and SDKs
* Android plugin
* Retrolambda plugin
* Android APT plugin
* When needed, it applies also *Google Play Services* plugin
* Unit tests:
  - mockito
  - robolectric
  - junit

For java projects it applies followin:
* Retrolambda plugin
* Java APT plugin
* Unit tests:
  - mockito
  - junit

Usage
-----

### For android:

##### Root project
```groovy
ext {
    versionCode = System.getenv('BUILD_NUMBER') as Integer ?: 10000
    versionName = "0.1-$versionCode"

    /* Optional */ playServicesVersion = 'play-services-version'

    /* Optional */ minSdkVersion = 10
    /* Optional */ targetSdkVersion = 23
    /* Optional */ compileSdkVersion = 23
    /* Optional */ buildToolsVersion = '23.0.1'
    /* Optional */ playServicesVersion = '8.3.0'

    /* Optional */ junitVersion = '4.12'
    /* Optional */ mockitoVersion = '2.0.31'
    /* Optional */ robolectricVersion = '3.0'
}

buildscripts {
    repositories {
        jcenter()
        maven { url "https://plugins.gradle.org/m2/" }
        maven { url "https://jitpack.io" }
    }
    dependencies {
        classpath 'com.github.slim-gears:gradle-aio:master-SNAPSHOT'
    }
}
```

##### Android library project
```groovy
ext {
   /* Optional */ usePlayServices = ['identity', 'plus']
}

apply plugin: 'android-lib-aio'
```

##### For android application
```groovy
ext {
    keyStoreFile = 'path-to-key-store-file'
    keyStorePassword = 'key-store-password'
    keyPassword = 'key-password'

    /* Optional */ usePlayServices = ['identity', 'plus']
}

apply plugin: 'android-app-aio'
```

### For java library
```groovy
ext {
    /* Optional */ junitVersion = '4.12'
    /* Optional */ mockitoVersion = '2.0.31'
}

apply plugin: 'java-lib-aio'
```
