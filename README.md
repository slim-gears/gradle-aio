Gradle Android and Java All-In-One
==================================
[![Build Status](https://travis-ci.org/slim-gears/gradle-aio.svg?branch=master)](https://travis-ci.org/slim-gears/gradle-aio)
----------------------------------


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
buildscripts {
    repositories {
        jcenter()
        maven { url "https://plugins.gradle.org/m2/" }
        maven { url "https://jitpack.io" }
    }
    dependencies {
        classpath 'com.github.slim-gears:gradle-aio:0.4.5'
    }
}

apply plugin: 'root-project-aio'
```

##### Android library project
```groovy
apply plugin: 'android-lib-aio'
```

##### For android application
```groovy
aioConfig {
  androidAppAio {
    versionCode = System.getenv('BUILD_NUMBER') as Integer ?: 0
    versionName = "0.1-$versionCode"
  
    keyStoreFile = 'path-to-key-store-file'
    keyStorePassword = 'key-store-password'
    keyPassword = 'key-password'
  }
}

apply plugin: 'android-app-aio'
```

### For java library
```groovy
apply plugin: 'java-lib-aio'
```
