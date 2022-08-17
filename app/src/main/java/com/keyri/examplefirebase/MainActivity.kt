package com.keyri.examplefirebase

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.keyri.examplefirebase.databinding.ActivityMainBinding
import com.keyrico.scanner.easyKeyriAuth
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

    private fun showMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun copyMessageToClipboard(message: String) {
        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clip = ClipData.newPlainText("Keyri Firebase example", message)

        clipboard.setPrimaryClip(clip)
    }
}
