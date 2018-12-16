package com.macbook.kurirlaundry.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.macbook.kurirlaundry.MainActivity;
import com.macbook.kurirlaundry.TampilDialog;
import com.macbook.kurirlaundry.activities.LoginActivity;
import com.macbook.kurirlaundry.api.APIClient;
import com.macbook.kurirlaundry.api.AuthService;
import com.macbook.kurirlaundry.models.Kurir;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Authorization {

    private static String TAG = "Testing";
    //    SharedPreferences
    SharedPreferences mSPLogin;
    Context context;
    TampilDialog tampilDialog;

    public Authorization(Context context) {
        this.context = context;
    }

    private void initializeSP() {
        mSPLogin = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        tampilDialog = new TampilDialog(context);
    }

    public boolean CheckSession() {
        initializeSP();
        String token = mSPLogin.getString("token", null);
        if (token != null) {
            checkToken(token);
            return true;
        }
        return false;
    }

    private void checkToken(String token) {
        AuthService authService = APIClient.getClient().create(AuthService.class);
        authService.getCheckLogin("Bearer "+token).enqueue(new Callback<Kurir>() {
            @Override
            public void onResponse(Call<Kurir> call, Response<Kurir> response) {
                if (!response.isSuccessful()){
                    Intent intent = new Intent(context,LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                }else {
                    SharedPreferences.Editor editor = mSPLogin.edit();
                    editor.putString("id",response.body().getId());
                    editor.putString("name",response.body().getNama());
                    editor.putString("username",response.body().getUsername());
                    editor.commit();
                }
            }

            @Override
            public void onFailure(Call<Kurir> call, Throwable t) {
                Log.i(TAG, "onFailure: "+t.getMessage());
                Intent intent = new Intent(context,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        });
    }

    public void logout(){
        initializeSP();
        mSPLogin.edit().clear().commit();
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
}
