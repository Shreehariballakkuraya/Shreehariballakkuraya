package com.hari.docuvault

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class LoginActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var signInButton: SignInButton
    private lateinit var profileImageView: ImageView

    private val Req_Code: Int = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginpage)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize Google Sign-In button and set the click listener
        signInButton = findViewById(R.id.btn_sign_in_google)
        signInButton.setOnClickListener {
            Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show()
            signInGoogle()
        }

        // Initialize Profile ImageView
        profileImageView = findViewById(R.id.profileImageView)

        // Check for existing sign-in
        checkExistingSignIn()
    }

    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    // Handle result from Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                updateUI(account)
            } else {
                // Google Sign In failed, show toast
                Toast.makeText(this, "Google Sign In failed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.w("LoginActivity", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, "Sign-in failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            // Login successful
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
            // Continue with your existing logic
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Save user information
                    SavedPreference.setEmail(this, account.email.toString())
                    SavedPreference.setUsername(this, account.displayName.toString())

                    // Update profile picture
                    account.photoUrl?.let { photoUrl ->
                        Picasso.get().load(photoUrl).into(profileImageView)
                        profileImageView.visibility = ImageView.VISIBLE
                    } ?: run {
                        profileImageView.visibility = ImageView.GONE
                    }

                    // Create user ID in Firestore
                    val userId = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener
                    val firestore = FirebaseFirestore.getInstance()
                    val userRef = firestore.collection("users").document(userId)

                    val userData = hashMapOf(
                        "email" to account.email,
                        "displayName" to account.displayName,
                        "photoUrl" to account.photoUrl.toString()
                    )

                    userRef.set(userData).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            // Data saved successfully
                            Toast.makeText(this, "User data saved", Toast.LENGTH_SHORT).show()
                        } else {
                            // Handle failure
                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Redirect to HomeActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Login unsuccessful
            Toast.makeText(this, "Login unsuccessful", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkExistingSignIn() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            // User is already signed in
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
