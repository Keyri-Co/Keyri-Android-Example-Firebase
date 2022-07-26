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
    implementation("com.github.Keyri-Co.keyri-android-whitelabel-sdk:keyrisdk:$latestKeyriVersion")
    implementation("com.github.Keyri-Co.keyri-android-whitelabel-sdk:scanner:$latestKeyriVersion")
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
        Firebase.auth.getAccessToken(false)
            .addOnSuccessListener { result ->
                (result as? GetTokenResult)?.let { tokenResult ->
                    val claims = JSONObject().apply {
                        tokenResult.claims.entries.forEach {
                            put(it.key, it.value)
                        }
                    }

                    val tokenData = JSONObject().apply {
                        put("token", tokenResult.token)

                        authResult.user?.zzf()?.let {
                            put("tokenData", JSONObject(it))
                        }

                        put("claims", claims)
                        put("authTimestamp", tokenResult.authTimestamp)
                        put("issuedAtTimestamp", tokenResult.issuedAtTimestamp)
                        put("expirationTimestamp", tokenResult.expirationTimestamp)
                        put("signInProvider", tokenResult.signInProvider)
                        put("signInSecondFactor", tokenResult.signInSecondFactor)
                    }

                    val metadata = JSONObject().apply {
                        val creationTimestamp = authResult.user?.metadata?.creationTimestamp
                        val lastSignInTimestamp =
                            authResult.user?.metadata?.lastSignInTimestamp

                        put("creationTimestamp", creationTimestamp)
                        put("lastSignInTimestamp", lastSignInTimestamp)
                    }

                    val providerData = JSONArray()

                    authResult.user?.providerData?.forEach {
                        val userInfo = JSONObject().apply {
                            put("displayName", it.displayName)
                            put("providerId", it.providerId)
                            put("isEmailVerified", it.isEmailVerified)
                            put("email", it.email)
                            put("phoneNumber", it.phoneNumber)
                            put("photoUrl", it.photoUrl)
                            put("uid", it.uid)
                        }

                        providerData.put(userInfo)
                    }

                    val user = JSONObject().apply {
                        put("email", authResult.user?.email)
                        put("token", tokenData)
                        put("isAnonymous", authResult.user?.isAnonymous)
                        put("metadata", metadata)
                        put("providerData", providerData)
                        put("tenantId", authResult.user?.tenantId)
                        put("displayName", authResult.user?.displayName)
                        put("isEmailVerified", authResult.user?.isEmailVerified)
                        put("phoneNumber", authResult.user?.phoneNumber)
                        put("photoUrl", authResult.user?.photoUrl)
                        put("providerId", authResult.user?.providerId)
                        put("uid", authResult.user?.uid)
                    }

                    val profile = JSONObject().apply {
                        authResult.additionalUserInfo?.profile?.entries?.forEach {
                            put(it.key, it.value)
                        }
                    }

                    val additionalUserInfo = JSONObject().apply {
                        put("username", authResult.additionalUserInfo?.username)
                        put("profile", profile)
                        put("providerId", authResult.additionalUserInfo?.providerId)
                        put("isNewUser", authResult.additionalUserInfo?.isNewUser)
                    }

                    val credential = JSONObject().apply {
                        put("provider", authResult.credential?.provider)
                        put("signInMethod", authResult.credential?.signInMethod)
                    }

                    val data = JSONObject().apply {
                        put("user", user)
                        put("additionalUserInfo", additionalUserInfo)
                        put("credential", credential)
                    }

                    val email = authResult.user?.email
                    val keyri = Keyri()

                    val signingData = JSONObject().apply {
                        put("timestamp", System.currentTimeMillis()) // Optional
                        put("email", authResult.user?.email) // Optional
                        put("uid", authResult.user?.uid) // Optional
                    }.toString()

                    val signature = keyri.getUserSignature(email, signingData)

                    val payload = JSONObject().apply {
                        put("data", data)
                        put("signingData", signingData)
                        put("userSignature", signature) // Optional
                        put("associationKey", keyri.getAssociationKey(email)) // Optional
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
