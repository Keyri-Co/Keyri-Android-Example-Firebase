package com.keyri.examplefirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.keyri.examplefirebase.databinding.ActivityMainBinding
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.ui.auth.AuthWithScannerActivity
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val easyKeyriAuthLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val text = if (it.resultCode == RESULT_OK) "Authenticated" else "Failed to authenticate"

            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.bFirebaseAuth.setOnClickListener {
            authWithFirebase()
        }
    }

    private fun authWithFirebase() {
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
    }

    private fun keyriAuth(publicUserId: String?, payload: String) {
        val intent = Intent(this, AuthWithScannerActivity::class.java).apply {
            putExtra(AuthWithScannerActivity.APP_KEY, "SQzJ5JLT4sEE1zWk1EJE1ZGNfwpvnaMP")
            putExtra(AuthWithScannerActivity.PUBLIC_USER_ID, publicUserId)
            putExtra(AuthWithScannerActivity.PAYLOAD, payload)
        }

        easyKeyriAuthLauncher.launch(intent)
    }
}
