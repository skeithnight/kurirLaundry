package com.macbook.kurirlaundry.api;

import com.macbook.kurirlaundry.models.Transaksi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

import java.util.ArrayList;

public interface DataService {

    @GET("/transaksi")
    Call<ArrayList<Transaksi>> getListTransaksi(@Header("Authorization") String token);

    @POST("/transaksi")
    Call<ResponseBody> postProsesTransaksi(@Header("Authorization") String token, @Body Transaksi transaksi);

}
