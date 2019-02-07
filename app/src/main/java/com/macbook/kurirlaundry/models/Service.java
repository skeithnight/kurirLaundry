package com.macbook.kurirlaundry.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Service {
    @SerializedName("service")
    @Expose
    private MenuLaundry service;
    @SerializedName("jumlah")
    @Expose
    private Integer jumlah;

    public MenuLaundry getService() {
        return service;
    }

    public void setService(MenuLaundry service) {
        this.service = service;
    }

    public Integer getJumlah() {
        return jumlah;
    }

    public void setJumlah(Integer jumlah) {
        this.jumlah = jumlah;
    }
}
