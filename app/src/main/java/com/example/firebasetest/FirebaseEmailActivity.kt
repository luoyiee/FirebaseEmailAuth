package com.example.firebasetest

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.emailauth.R
import com.google.firebase.Firebase
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.actionCodeSettings
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest

class FirebaseEmailActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var statusTextView: TextView
    private lateinit var createAccountButton: Button
    private lateinit var signInButton: Button
    private lateinit var signOutButton: Button
    private lateinit var verifyEmailButton: Button
    private lateinit var passwordResetButton: Button
    private lateinit var emailUpdateButton: Button
    private lateinit var passwordUpdateButton: Button
    private lateinit var actionCodeSettings: ActionCodeSettings

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_email)
        auth = Firebase.auth
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        statusTextView = findViewById(R.id.statusTextView)
        createAccountButton = findViewById(R.id.createAccountButton)
        signInButton = findViewById(R.id.signInButton)
        signOutButton = findViewById(R.id.signOutButton)
        verifyEmailButton = findViewById(R.id.verifyEmailButton)
        passwordResetButton = findViewById(R.id.passwordResetButton)
        emailUpdateButton = findViewById(R.id.emailUpdateButton)
        passwordUpdateButton = findViewById(R.id.passwordUpdateButton)

        actionCodeSettings = actionCodeSettings {
            // URL you want to redirect back to. The domain (www.example.com) for this
            // URL must be whitelisted in the Firebase Console.
            url = "https://emailauth-a25c6.firebaseapp.com/finishSignUp?cartId=1234"
            // This must be true
            handleCodeInApp = true
//            setIOSBundleId("com.example.ios")
            setAndroidPackageName(
                "com.example.emailauth",
                true, // installIfNotAvailable
                "12", // minimumVersion
            )
        }

        createAccountButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            createAccount(email, password)
        }

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            signIn(email, password)
        }

        signOutButton.setOnClickListener {
            auth.signOut()
            updateUI(null)
        }

        verifyEmailButton.setOnClickListener {
//            sendEmailVerification()
            sendEmailVerificationWithEmail()
        }

        passwordResetButton.setOnClickListener {
            sendPasswordReset()
        }
        emailUpdateButton.setOnClickListener {
            updateEmail()
        }
        passwordUpdateButton.setOnClickListener {
            updatePassword()
        }
    }

    public override fun onStart() {
        super.onStart()
        // auth.addAuthStateListener(authStateListener)
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }


    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                    Toast.makeText(
                        baseContext,
                        "Account created successfully.",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    // sign in fails
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // sign in fails
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun updateProfile() {
        // [START update_profile]
        val user = Firebase.auth.currentUser

        val profileUpdates = userProfileChangeRequest {
            displayName = "Jane Q. User"
            photoUri = Uri.parse("https://example.com/jane-q-user/profile.jpg")
        }

        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Profile update successfully.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    Log.d(TAG, "User profile updated.")
                }
            }
        // [END update_profile]
    }

    private fun updateEmail() {
        // [START update_email]
        val user = Firebase.auth.currentUser
        val newEmail = emailEditText.text.toString()
        user!!.verifyBeforeUpdateEmail(newEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Email update successfully.",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d(TAG, "User email address updated.")
                }
            }
        // [END update_email]
    }

    private fun updatePassword() {
        // [START update_password]
        val user = Firebase.auth.currentUser
        val newPassword = passwordEditText.text.toString()

        user!!.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Password update successfully.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    Log.d(TAG, "User password updated.")
                }
            }
        // [END update_password]
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Verification email sent to ${user.email}",
                        Toast.LENGTH_SHORT,
                    ).show()
                } else {
                    Log.e(TAG, "sendEmailVerification", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun sendEmailVerificationWithEmail() {
        Firebase.auth.sendSignInLinkToEmail(emailEditText.text.toString(), actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                }
            }
    }

    private fun sendEmailVerificationWithContinueUrl() {
        // [START send_email_verification_with_continue_url]
        val auth = Firebase.auth
        val user = auth.currentUser!!

        val url = "http://www.example.com/verify?uid=" + user.uid
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl(url)
            .setIOSBundleId("com.example.ios")
            // The default for this is populated with the current android package name.
            .setAndroidPackageName("com.example.android", false, null)
            .build()

        user.sendEmailVerification(actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                }
            }

        // [END send_email_verification_with_continue_url]
        // [START localize_verification_email]
        auth.setLanguageCode("fr")
        // To apply the default app language instead of explicitly setting it.
        // auth.useAppLanguage()
        // [END localize_verification_email]
    }

    private fun sendPasswordReset() {
        val emailAddress = emailEditText.text.toString()
        // [START send_password_reset]
//        val emailAddress = "user@example.com"
        if (emailAddress.trim().isNotEmpty()) {
            Firebase.auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                    }
                }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            statusTextView.text =
                "Signed in as:\n${user.email}\nEmail verified: ${user.isEmailVerified}"
//            signOutButton.isEnabled = true
            verifyEmailButton.isEnabled = !user.isEmailVerified
//            createAccountButton.isEnabled = false
//            signInButton.isEnabled = false
//            passwordResetButton.isEnabled = false
//            emailUpdateButton.isEnabled = false
//            passwordUpdateButton.isEnabled = true
        } else {
            statusTextView.text = "Not signed in"
//            signOutButton.isEnabled = false
            verifyEmailButton.isEnabled = true
//            createAccountButton.isEnabled = true
//            signInButton.isEnabled = true
//            passwordResetButton.isEnabled = true
//            emailUpdateButton.isEnabled = true
//            passwordUpdateButton.isEnabled = false
        }
    }

    companion object {
        private const val TAG = "EmailPassword"
    }
}
