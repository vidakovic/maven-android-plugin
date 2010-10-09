===============================================================================
MAVEN ANDROID PLUGIN VERSION 2
===============================================================================

The project home page is at http://code.google.com/p/maven-android-plugin

All documentation is there.

Notes SDK Mojo:

Goals:
1. android:sdkInstall
2. android:sdkClean

Example configuration

[pom]
...

    <plugin>
        <groupId>com.jayway.maven.plugins.android.generation2</groupId>
        <artifactId>maven-android-plugin</artifactId>
        <version>2.5.3-SNAPSHOT</version>
        <configuration>
            <sdk>
                <platform>2.1</platform>
                <path>/tmp/.android-sdk/</path>
            </sdk>
            <install>
                <path>/tmp/.android-sdk/</path>
                <agree>true</agree>
                <verbose>true</verbose>
            </install>
            <deleteConflictingFiles>true</deleteConflictingFiles>
        </configuration>
        <extensions>true</extensions>
    </plugin>

...
[/pom]

NOTE: the nested path tag in <install> will be removed. A small change is needed  
      in AbstractAndroidMojo. 