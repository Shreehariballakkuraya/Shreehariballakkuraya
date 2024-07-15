package com.hari.docuvault

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class GoogleSignInActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_sign_in)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Start sign-in intent
        signIn()
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult> { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Signed in successfully
                val account = task.getResult(ApiException::class.java)
                handleSignInSuccess(account)
            } catch (e: ApiException) {
                // Handle sign-in failure
                Log.w("GoogleSignInActivity", "signInResult:failed code=" + e.statusCode)
                handleSignInFailure(e)
            }
        }
    })

    private fun handleSignInSuccess(account: GoogleSignInAccount?) {
        // TODO: handle the authenticated user
        // For example, navigate to another activity or store user details
        Log.d("GoogleSignInActivity", "Signed in successfully: ${account?.displayName}")
        navigateToHomePage()
    }

    private fun handleSignInFailure(exception: ApiException) {
        // TODO: handle sign-in failure
        Log.w("GoogleSignInActivity", "Sign-in failed: ${exception.localizedMessage}")
        // Optionally, you can show a message to the user or retry sign-in
    }

    private fun navigateToHomePage() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
