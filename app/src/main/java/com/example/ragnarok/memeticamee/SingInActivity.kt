package com.example.ragnarok.memeticamee

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import com.example.ragnarok.memeticamee.service.MyFirebaseInstanceIDService
import com.example.ragnarok.memeticamee.util.FirestoreUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_sing_in.*
import org.jetbrains.anko.*
import org.jetbrains.anko.design.longSnackbar

class SingInActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 1

    private val signInProviders =
            listOf(AuthUI.IdpConfig.EmailBuilder()
                    .setAllowNewAccounts(true)
                    .setRequireName(true)
                    .build())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_in)

        val progressBar: ProgressBar = this.progressBar
        progressBar.visibility = View.GONE

        account_sign_in.setOnClickListener {
            val intent = AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(signInProviders)
                    .setLogo(R.drawable.ic_message_black_24dp)
                    .build()
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                progressBar.visibility = View.VISIBLE
                FirestoreUtil.initCurrentUserIfFirstTime {
                    startActivity(intentFor<MainActivity>().newTask().clearTask())

                    val registrationToken = FirebaseInstanceId.getInstance().token
                    MyFirebaseInstanceIDService.addTokenToFirestore(registrationToken)

                    progressBar.visibility = View.GONE
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response == null) return

                when(response.error?.errorCode) {
                    ErrorCodes.NO_NETWORK ->
                        constraint_layout.longSnackbar("No network")
                    ErrorCodes.UNKNOWN_ERROR ->
                        constraint_layout.longSnackbar("Unknown error")
                }
            }
        }
    }
}
