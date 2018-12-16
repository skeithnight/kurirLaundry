package com.macbook.kurirlaundry.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.macbook.kurirlaundry.MainActivity
import com.macbook.kurirlaundry.R
import com.macbook.kurirlaundry.TampilDialog
import com.macbook.kurirlaundry.api.APIClient
import com.macbook.kurirlaundry.api.AuthService
import com.macbook.kurirlaundry.models.Kurir
import com.macbook.kurirlaundry.models.ResponseLogin
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    val tampilDialog:TampilDialog = TampilDialog(baseContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.
        password_login.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        sign_in_button.setOnClickListener { attemptLogin() }
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username_login, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {

        // Reset errors.
        username_login.error = null
        password_login.error = null

        // Store values at the time of the login attempt.
        val username_loginStr = username_login.text.toString()
        val password_loginStr = password_login.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password_login, if the user entered one.
        if (!TextUtils.isEmpty(password_loginStr) && !ispassword_loginValid(password_loginStr)) {
            password_login.error = getString(R.string.error_incorrect_password)
            focusView = password_login
            cancel = true
        }

        // Check for a valid username_login address.
        if (TextUtils.isEmpty(username_loginStr)) {
            username_login.error = getString(R.string.error_field_required)
            focusView = username_login
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            val kurir:Kurir = Kurir()
            kurir.username = username_loginStr
            kurir.password = password_loginStr
            showProgress(true)
//            val gson: Gson = Gson()
//            Toast.makeText(this@LoginActivity, gson.toJson(customer),Toast.LENGTH_LONG).show()
            getToken(kurir)
        }
    }

    private fun ispassword_loginValid(password_login: String): Boolean {
        //TODO: Replace this with your own logic
        return password_login.length > 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    private fun getToken(kurir: Kurir) {

        val service = APIClient.getClient().create(AuthService::class.java)

        service.postLogin(kurir).enqueue(object : Callback<ResponseLogin> {
            override fun onFailure(call: Call<ResponseLogin>?, t: Throwable?) {
                showProgress(false)
                TampilDialog(this@LoginActivity).showDialog("Failed",t.toString(),"")
            }

            override fun onResponse(call: Call<ResponseLogin>?, response: Response<ResponseLogin>?) {
                if (response != null) {
                    if (response.isSuccessful) {
                        val responseLogin: ResponseLogin = response.body()
                        val mSPLogin: SharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
                        val editor = mSPLogin.edit()

                        editor.putString("token", responseLogin.token)
                        editor.commit()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    }else{
                        showProgress(false)
                        TampilDialog(this@LoginActivity).showDialog("Failed",response.message(),"")
                    }
                }
            }

        })
    }
}
