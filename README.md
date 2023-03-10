# Wire™

[![Wire logo](https://github.com/wireapp/wire/blob/master/assets/header-small.png?raw=true)](https://wire.com/jobs/)

This repository is part of the source code of Wire. You can find more information at [wire.com](https://wire.com) or by
contacting opensource@wire.com.

You can find the published source code at [github.com/wireapp/wire](https://github.com/wireapp/wire), and the apk of the
latest release at [https://wire.com/en/download/](https://wire.com/en/download/).

For licensing information, see the attached LICENSE file and the list of third-party licenses
at [wire.com/legal/licenses/](https://wire.com/legal/licenses/).

If you compile the open source software that we make available from time to time to develop your own mobile, desktop or
web application, and cause that application to connect to our servers for any purposes, we refer to that resulting
application as an “Open Source App”. All Open Source Apps are subject to, and may only be used and/or commercialized in
accordance with, the Terms of Use applicable to the Wire Application, which can be found
at https://wire.com/legal/#terms. Additionally, if you choose to build an Open Source App, certain restrictions apply,
as follows:

a. You agree not to change the way the Open Source App connects and interacts with our servers; b. You agree not to
weaken any of the security features of the Open Source App; c. You agree not to use our servers to store data for
purposes other than the intended and original functionality of the Open Source App; d. You acknowledge that you are
solely responsible for any and all updates to your Open Source App.

For clarity, if you compile the open source software that we make available from time to time to develop your own
mobile, desktop or web application, and do not cause that application to connect to our servers for any purposes, then
that application will not be deemed an Open Source App and the foregoing will not apply to that application.

No license is granted to the Wire trademark and its associated logos, all of which will continue to be owned exclusively
by Wire Swiss GmbH. Any use of the Wire trademark and/or its associated logos is expressly prohibited without the
express prior written consent of Wire Swiss GmbH.

# Wire Detekt Rules

## Adding To Your Project

First, make sure to add Detekt to your project:

```
plugins {
    id("io.gitlab.arturbosch.detekt").version("1.19.0")
}
```

Then to add these set of rules, you have 2 options depending on your needs and level of automation you need:

## Option 1:

1. Copy the jar file (`dist/detekt-rules-1.0.0-SNAPSHOT.jar`) from this repository.
2. In your project folder, create a `detekt` folder and paste the jar file there.
3. Navigate to the `build.gradle.kts` file where you configured detekt, and add the jar file as dependency:

```
dependencies {
    detektPlugins(files("$rootDir/detekt/detekt-rules-1.0.0-SNAPSHOT.jar"))
}
```

## Option 2:

1. In your project's `build.gradle.kts` file, configure a new Ivy repository (with this you can just point to a plain
   jar file and simulate a package artifactory), you can do it by just copy-pasting the following snippet:

Typically, your root `build.gradle.kts`

```
repositories {
    val repo = ivy("https://raw.githubusercontent.com/wireapp/wire-detekt-rules/main/dist") {
        patternLayout {
            artifact("/[module]-[revision].[ext]")
        }
        metadataSources.artifact()
    }
    exclusiveContent {
        forRepositories(repo)
        filter {
            includeModule("com.wire", "detekt-rules")
        }
    }
}
```

2. Add the detektPlugin as follows, since we have a regular artifact dependency, we can use gradle artifact coordinates:

```
dependencies {
    detektPlugins("com.wire:detekt-rules:1.0.0-SNAPSHOT")
}
```

3. Tell gradle that this ivy managed artifact is a dynamic one (aka. SNAPSHOT). So we can configure it to always get the
   latest artifact. Gradle by default, on SNAPSHOT maven artifacts caches versions
   for [24 hours](https://docs.gradle.org/current/userguide/dynamic_versions.html#sec:controlling_dependency_caching_programmatically):

Add this parameter to enforce SNAPSHOT default behavior like a regular m2 artifact.

```
dependencies {
    detektPlugins("com.wire:detekt-rules:1.0.0-SNAPSHOT") {
        isChanging = true // tells gradle that this is a dynamic version, will try to get a new one using default gradle cache behavior.
    }
}
```

## Development workflow and local testing

When you are adding new rules to the project RuleSet, you can use the `./gradlew publishToMavenLocal` command to run the
project locally and test your changes.
You just need to declare `mavenLocal()` as a repository in your project's `build.gradle.kts` file:

```
repositories {
    mavenLocal()
    // other repositories omitted
}
```

## Reference documentation
- How to write custom [click here](https://detekt.github.io/detekt/extensions.html).
- Kotlin classes for reflection inspection [click here](https://github.com/JetBrains/kotlin/tree/master/compiler/psi/src/org/jetbrains/kotlin/psi)
- Usage of this RuleSet in Kalium [click here](https://github.com/wireapp/kalium/blob/develop/detekt/detekt.yml#L635)