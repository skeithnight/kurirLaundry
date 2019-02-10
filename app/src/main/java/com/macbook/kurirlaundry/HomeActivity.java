package com.macbook.kurirlaundry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.macbook.kurirlaundry.activities.LoginActivity;
import com.macbook.kurirlaundry.adapter.RecyclerViewAdapterTransaksi;
import com.macbook.kurirlaundry.api.APIClient;
import com.macbook.kurirlaundry.api.DataService;
import com.macbook.kurirlaundry.models.Transaksi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    static String TAG = "Testing";
    String token;
    String idUser;
    //    SharedPreferences
    SharedPreferences mSPLogin;
    // RecyclerView
    RecyclerView recyclerView;
    //    Data Menu Pesanan
    ArrayList<Transaksi> transaksiArrayList = new ArrayList<Transaksi>();
    private TampilDialog tampilDialog;
    private Toolbar mTopToolbar;
    private RecyclerViewAdapterTransaksi mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        tampilDialog = new TampilDialog(this);

        // show loading
        tampilDialog.showLoading();


        mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);

        getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favorite) {

            initializeSP();
            mSPLogin.edit().clear().commit();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeSP() {
        mSPLogin = getSharedPreferences("Login", Context.MODE_PRIVATE);
    }

    private void getData() {
        mSPLogin = this.getSharedPreferences("Login", Context.MODE_PRIVATE);
        token = mSPLogin.getString("token", null);
        idUser = mSPLogin.getString("id", null);
        if (token != null) {

            DataService dataService = APIClient.getClient().create(DataService.class);
            dataService.getListTransaksi("Bearer " + token).enqueue(new Callback<ArrayList<Transaksi>>() {
                @Override
                public void onResponse(Call<ArrayList<Transaksi>> call, Response<ArrayList<Transaksi>> response) {
                    tampilDialog.dismissLoading();
                    if (response.isSuccessful()) {
                        transaksiArrayList = response.body();

                        // initiate RecyclerView
                        recyclerView = (RecyclerView) findViewById(R.id.rv_list_pesanan);
                        recyclerView.setVisibility(View.VISIBLE);

                        mAdapter = new RecyclerViewAdapterTransaksi(transaksiArrayList);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(HomeActivity.this);
                        recyclerView.setLayoutManager(mLayoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        tampilDialog.showDialog("Failed", response.message(), "");
                    }

                }

                @Override
                public void onFailure(Call<ArrayList<Transaksi>> call, Throwable t) {
                    tampilDialog.dismissLoading();
                    tampilDialog.showDialog("Failed", t.getMessage(), "");
                }
            });
        }
    }
}
