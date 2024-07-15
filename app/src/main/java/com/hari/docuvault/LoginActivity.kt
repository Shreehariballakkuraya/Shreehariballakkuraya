package com.hari.docuvault

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.SignInButton

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInButton: SignInButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginpage)

        // Configure sign-in to request the user's ID, email address, and basic profile.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id)) // Replace with your server client ID
            .requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified.
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize Google Sign-In button and set the click listener
        signInButton = findViewById(R.id.btn_sign_in_google)
        signInButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            // The Task returned from this call is always completed, so there is no need to check isSuccessful().
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            Log.d("com.hari.docuvault.LoginActivity", "Signed in as: ${account?.displayName}")
            // TODO: Handle the authenticated user
            // Example:
            // - Update UI to show user's name
            // - Save user's information in SharedPreferences or a database
            // - Redirect to another activity or fragment in your app
            navigateToMainActivity()

        } catch (e: ApiException) {
            Log.w("com.hari.docuvault.LoginActivity", "signInResult:failed code=" + e.statusCode)
            // TODO: Handle sign-in failure
            // Example:
            // - Show a message to the user indicating that sign-in failed
            // - Log the error or notify an analytics service
            // - Optionally, allow the user to retry sign-in
            Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainActivity() {
        // Example: Navigate to another activity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()  // Close the login activity
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
