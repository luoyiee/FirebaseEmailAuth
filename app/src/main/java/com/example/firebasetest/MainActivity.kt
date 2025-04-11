package com.example.firebasetest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.example.emailauth.R
import com.google.firebase.Firebase
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.actionCodeSettings
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var statusTextView: TextView
    private lateinit var verifyEmailButton: Button
    private lateinit var actionCodeSettings: ActionCodeSettings

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
        emailEditText = findViewById(R.id.emailEditText)
        statusTextView = findViewById(R.id.statusTextView)
        verifyEmailButton = findViewById(R.id.verifyEmailButton)
        actionCodeSettings = actionCodeSettings {
            // URL you want to redirect back to. The domain (www.example.com) for this
            // URL must be whitelisted in the Firebase Console.
//            url = "https://justalk.com/finishSignUp?cartId=1234"
            url = "https://emailauth-a25c6.firebaseapp.com/finishSignUp?cartId=1234"

            // This must be true
            handleCodeInApp = true
            setIOSBundleId("com.example.ios")
            setAndroidPackageName(
                "com.example.emailauth",
                true, // installIfNotAvailable
                "12", // minimumVersion
            )
            linkDomain = "emailauth-a25c6.firebaseapp.com"
        }

        verifyEmailButton.setOnClickListener {
            sendEmailVerificationWithEmail()
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun sendEmailVerificationWithEmail() {
        Firebase.auth.sendSignInLinkToEmail(emailEditText.text.toString(), actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            statusTextView.text =
                "Signed in as:\n${user.email}\nEmail verified: ${user.isEmailVerified}"
            verifyEmailButton.isEnabled = !user.isEmailVerified
        } else {
            statusTextView.text = "Not signed in"
            verifyEmailButton.isEnabled = true
        }
    }

    companion object {
        private const val TAG = "EmailPassword"
    }


    private fun verifySignInLink() {
        // [START auth_verify_sign_in_link]
        val auth = Firebase.auth
        val intent = intent
        val emailLink = intent.data.toString()

        // Confirm the link is a sign-in with email link.
        if (auth.isSignInWithEmailLink(emailLink)) {
            // Retrieve this from wherever you stored it
            val email = "someemail@domain.com"

            // The client SDK will parse the code from the link for you.
            auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Successfully signed in with email link!")
                        val result = task.result
                        // You can access the new user via result.getUser()
                        // Additional user info profile *not* available via:
                        // result.getAdditionalUserInfo().getProfile() == null
                        // You can check if the user is new or existing:
                        // result.getAdditionalUserInfo().isNewUser()
                    } else {
                        Log.e(TAG, "Error signing in with email link", task.exception)
                    }
                }
        }
        // [END auth_verify_sign_in_link]
    }
}
