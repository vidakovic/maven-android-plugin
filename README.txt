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
            </sdk>
            <deleteConflictingFiles>true</deleteConflictingFiles>
            
            <!-- BEGIN: SDK installation config -->
            <revision>06</revision>
            <os>linux</os>
            <architecture>86</architecture>
            <overwrite>false</overwrite>
            <agree>true</agree>
            <verbose>true</verbose>
            <!-- END: SDK installation config -->
        </configuration>
        <extensions>true</extensions>
    </plugin>

...
[/pom]