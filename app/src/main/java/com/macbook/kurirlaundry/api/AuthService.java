package com.macbook.kurirlaundry.api;

import com.macbook.kurirlaundry.models.Administrator;
import com.macbook.kurirlaundry.models.Kurir;
import com.macbook.kurirlaundry.models.ResponseLogin;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthService {

    @POST("kurir/login")
    Call<ResponseLogin> postLogin(@Body Kurir kurirLogin);
//
    @GET("kurir/check-session")
    Call<Kurir> getCheckLogin(@Header("Authorization") String token);
}
