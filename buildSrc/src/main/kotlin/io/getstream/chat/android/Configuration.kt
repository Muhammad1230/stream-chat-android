package io.getstream.chat.android

object Configuration {
    const val compileSdk = 31
    const val targetSdk = 31
    const val minSdk = 21
    const val majorVersion = 4
    const val minorVersion = 27
    const val patchVersion = 2
    const val versionName = "$majorVersion.$minorVersion.$patchVersion"
    const val snapshotVersionName = "$majorVersion.$minorVersion.${patchVersion + 1}-SNAPSHOT"
    const val artifactGroup = "io.getstream"
}
