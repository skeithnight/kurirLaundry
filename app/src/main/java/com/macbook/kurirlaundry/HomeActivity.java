package com.macbook.kurirlaundry;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.ButterKnife;
import com.macbook.kurirlaundry.adapter.RecyclerViewAdapterTransaksi;
import com.macbook.kurirlaundry.api.APIClient;
import com.macbook.kurirlaundry.api.DataService;
import com.macbook.kurirlaundry.models.Transaksi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    String token;
    String idUser;

    static String TAG = "Testing";
    private TampilDialog tampilDialog;
    //    SharedPreferences
    SharedPreferences mSPLogin;
    // RecyclerView
    RecyclerView recyclerView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        tampilDialog = new TampilDialog(this);

        // show loading
        tampilDialog.showLoading();

        getData();
    }
    //    Data Menu Pesanan
    ArrayList<Transaksi> transaksiArrayList = new ArrayList<Transaksi>();
    private RecyclerViewAdapterTransaksi mAdapter;

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
                    }else {
                        tampilDialog.showDialog("Failed", response.message(),"");
                    }

                }

                @Override
                public void onFailure(Call<ArrayList<Transaksi>> call, Throwable t) {
                    tampilDialog.dismissLoading();
                    tampilDialog.showDialog("Failed", t.getMessage(),"");
                }
            });
        }
    }
}
