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

Add SDK dependency to your build.gradle file and sync project:

```kotlin
dependencies {
    // ...
    implementation("com.keyri:keyrisdk:$latestKeyriVersion")
    implementation("com.keyri:scanner:$latestKeyriVersion")
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

                    var refreshToken = ""
                    var accessToken = ""

                    authResult.user?.zzf()?.let {
                        val jsonObject = JSONObject(it)

                        refreshToken = jsonObject.getString("refresh_token")
                        accessToken = jsonObject.getString("access_token")
                    }

                    val email = authResult.user?.email

                    val payload = JSONObject().apply {
                        put("uid", authResult.user?.uid)
                        put("emailVerified", authResult.user?.isEmailVerified)
                        put("isAnonymous", authResult.user?.isAnonymous)
                        put("providerData", providerData)
                        put("refreshToken", refreshToken)
                        put("accessToken", accessToken)
                        put("expirationTime", tokenResult.expirationTimestamp)
                    }.toString()

                    Log.e("payload", payload)

                    keyriAuth(email, payload)
                }
            }
    }
    .addOnFailureListener {
        showMessage(it.message)

        it.message?.let { errorMessage ->
            copyMessageToClipboard(errorMessage)
            Log.e("Keyri Firebase example", errorMessage)
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
    easyKeyriAuth(
        content = this,
        easyKeyriAuthLauncher = easyKeyriAuthLauncher,
        appKey = "SQzJ5JLT4sEE1zWk1EJE1ZGNfwpvnaMP",
        payload = payload,
        publicUserId = publicUserId
    )
}
```
