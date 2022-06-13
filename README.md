# Overview

This module contains example of implementation [Keyri](https://keyri.com) with Firebase
Authentication.

## Contents

* [Requirements](#Requirements)
* [Permissions](#Permissions)
* [Keyri Integration](#Keyri-Integration)
* [Firebase Authentication Integration](#Firebase-Authentication-Integration)
* [Authentication](#Authentication)

## Requirements

* Android API level 23 or higher
* AndroidX compatibility
* Kotlin coroutines compatibility

Note: Your app does not have to be written in kotlin to integrate this SDK, but must be able to
depend on kotlin functionality.

## Permissions

Open your app's `AndroidManifest.xml` file and add the following permission:

```xml

<uses-permission android:name="android.permission.INTERNET" />
```

## Keyri Integration

* Add the JitPack repository to your root build.gradle file:

```groovy
allprojects {
    repositories {
        // ...
        maven { url "https://jitpack.io" }
    }
}
```

* Add SDK dependency to your build.gradle file and sync project:

```kotlin
dependencies {
    // ...
    implementation("com.github.Keyri-Co:keyri-android-whitelabel-sdk:$latestKeyriVersion")
}
```

## Firebase Authentication Integration

Check [Get Started with Firebase Authentication on Android](https://firebase.google.com/docs/auth/android/start?hl=en#kotlin+ktx)
article to integrate Firebase Authentication SDK into your app.

## Authentication

* Here is example of Firebase Authentication with google.com OAuthProvider:

```kotlin
val googleProvider = OAuthProvider.newBuilder("google.com")
    .build()

Firebase.auth.startActivityForSignInWithProvider(this, googleProvider)
    .addOnSuccessListener { authResult ->
        authResult.user
            ?.getIdToken(false)
            ?.addOnSuccessListener { tokenResult ->
                tokenResult.token?.let { token ->
                    val email = authResult.user?.email
                    val keyri = Keyri()

                    val signature = keyri.getUserSignature(email, email)

                    val payload = JSONObject().apply {
                        put("token", token)
                        put("provider", "firebase:google.com") // Optional
                        put("timestamp", System.currentTimeMillis()) // Optional
                        put("associationKey", keyri.getAssociationKey(email)) // Optional
                        put("userSignature", signature) // Optional
                    }.toString()

                    // Public user ID (email) is optional
                    keyriAuth(email, payload)
                }
            }
    }
```

* Authenticate with Keyri. In the next showing `AuthWithScannerActivity` with providing
  `publicUserId` and `payload`.

```kotlin
private val easyKeyriAuthLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Process authentication result
    }

private fun keyriAuth(publicUserId: String?, payload: String) {
    val intent = Intent(this, AuthWithScannerActivity::class.java).apply {
        putExtra(AuthWithScannerActivity.APP_KEY, BuildConfig.APP_KEY)
        putExtra(AuthWithScannerActivity.PUBLIC_USER_ID, publicUserId)
        putExtra(AuthWithScannerActivity.PAYLOAD, payload)
    }

    easyKeyriAuthLauncher.launch(intent)
}
```
