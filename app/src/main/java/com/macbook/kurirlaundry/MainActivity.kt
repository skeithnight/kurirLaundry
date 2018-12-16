package com.macbook.kurirlaundry

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import butterknife.ButterKnife
import com.macbook.kurirlaundry.activities.LoginActivity
import com.macbook.kurirlaundry.adapter.RecyclerViewAdapterTransaksi
import com.macbook.kurirlaundry.api.APIClient
import com.macbook.kurirlaundry.api.DataService
import com.macbook.kurirlaundry.controller.Authorization
import com.macbook.kurirlaundry.models.Transaksi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private var tampilDialog: TampilDialog? = null
    private var authorization: Authorization? = null


    override fun onStart() {
        super.onStart()
        authorization = Authorization(applicationContext)
        tampilDialog = TampilDialog(applicationContext)
        if (!authorization!!.CheckSession()) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val authorization = Authorization(this)
//        authorization.logout()
    }
}
