import java.net.URI


pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = URI.create("https://jitpack.io") }
    }
}

rootProject.name = "Primal"

include(":app")

include(":core:utils")
include(":core:nips")
include(":core:app-config")
include(":core:networking-lightning")
include(":core:networking-http")
include(":core:networking-primal")
include(":core:networking-upload")
include(":core:caching")

include(":data:shared:local")

include(":data:caching:local")
include(":data:caching:remote")
include(":data:caching:repository")

include(":data:wallet:local")
include(":data:wallet:remote-primal")
include(":data:wallet:remote-nwc")
include(":data:wallet:remote-tsunami")
include(":data:wallet:repository")

include(":data:account:local")
include(":data:account:remote")
include(":data:account:repository")

include(":domain:nostr")
include(":domain:primal")
include(":domain:wallet")
include(":domain:account")

include(":paging-runtime-ios")
include(":shared")

val primalTsunamiSdkRepoPath = file("../primal-tsunami-sdk")
if (primalTsunamiSdkRepoPath.exists()) {
    includeBuild("../primal-tsunami-sdk/bindings/kmp/primal-tsunami-kmp") {
        dependencySubstitution {
            substitute(module("net.primal:tsunami-sdk-kmp")).using(project(":sdk"))
        }
    }
}
