package com.keyri.examplefirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.keyri.examplefirebase.databinding.ActivityMainBinding
import com.keyrico.keyrisdk.Keyri
import com.keyrico.keyrisdk.ui.auth.AuthWithScannerActivity
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
    }

    private fun keyriAuth(publicUserId: String?, payload: String) {
        val intent = Intent(this, AuthWithScannerActivity::class.java).apply {
            putExtra(AuthWithScannerActivity.APP_KEY, "IT7VrTQ0r4InzsvCNJpRCRpi1qzfgpaj")
            putExtra(AuthWithScannerActivity.PUBLIC_USER_ID, publicUserId)
            putExtra(AuthWithScannerActivity.PAYLOAD, payload)
        }

        easyKeyriAuthLauncher.launch(intent)
    }
}
