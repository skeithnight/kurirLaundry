package com.macbook.kurirlaundry.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.gson.Gson;
import com.macbook.kurirlaundry.R;
import com.macbook.kurirlaundry.TampilDialog;
import com.macbook.kurirlaundry.api.APIClient;
import com.macbook.kurirlaundry.api.DataService;
import com.macbook.kurirlaundry.models.MenuLaundry;
import com.macbook.kurirlaundry.models.Transaksi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DetailPesananActivity extends AppCompatActivity {

    private static String TAG = "Testing";
    Transaksi transaksi;
    private String token, idUser;

    private TampilDialog tampilDialog;
    //    SharedPreferences
    SharedPreferences mSPLogin;

    @BindView(R.id.id_pesanan)
    EditText idPesanan;
    @BindView(R.id.nama_pemesan)
    EditText namaPemesan;
    @BindView(R.id.waktu_pemesanan)
    EditText waktuPemesanan;
    @BindView(R.id.listLayanan__detail_pesan)
    ListView lvLayananPesan;
    @BindView(R.id.btn_detail_pesanan)
    Button btnDetailPesan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pesanan);ButterKnife.bind(this);
        tampilDialog = new TampilDialog(this);

        getData();
    }

    private void getData() {
        transaksi = new Transaksi();
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            transaksi = null;
        } else {
            Log.i(TAG, "onCreate: " + extras.getString("transaksi", null));
            Gson gson = new Gson();
            transaksi = gson.fromJson(extras.getString("transaksi", null), Transaksi.class);
            Log.i(TAG, "onCreate: " + gson.toJson(transaksi));
            setData();
        }
    }

    private void setData() {
        idPesanan.setText(transaksi.getId() == null ? "" : transaksi.getId());
        namaPemesan.setText(transaksi.getCustomer().getNama() == null ? "" : transaksi.getCustomer().getNama());
        waktuPemesanan.setText(getDate(transaksi.getWaktuPesan() == null ? 0 : transaksi.getWaktuPesan()));

        ArrayList<String> list = new ArrayList<>();
        double totalTagihan = 0;
        for (MenuLaundry item : transaksi.getMenuLaundry()
                ) {
            list.add(item.getJenis() + " | Rp. " + item.getHarga() + "/" + item.getSatuan());
            totalTagihan += item.getHarga();
        }

        String[] arrayPilihan = list.toArray(new String[list.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1, arrayPilihan
        );

        // set data
        lvLayananPesan.setAdapter(adapter);

        if (transaksi.getStatus().equals("Jemput")) {
            btnDetailPesan.setVisibility(View.VISIBLE);
            btnDetailPesan.setText("Jemput");
        }else if (transaksi.getStatus().equals("OTW")) {
            btnDetailPesan.setVisibility(View.VISIBLE);
            btnDetailPesan.setText("Ambil Barang");
        }else if (transaksi.getStatus().equals("Ambil-barang")) {
            btnDetailPesan.setVisibility(View.VISIBLE);
            btnDetailPesan.setText("Sampai Laundry");
        }else if (transaksi.getStatus().equals("Selesai-laundry")){
            btnDetailPesan.setVisibility(View.VISIBLE);
            btnDetailPesan.setText("Antar Barang");
        }else if (transaksi.getStatus().equals("Antar")){
            btnDetailPesan.setVisibility(View.VISIBLE);
            btnDetailPesan.setText("Selesai");
        }

    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    @OnClick(R.id.btn_detail_pesanan)
    public void processPesanan() {
        tampilDialog.showLoading();
        mSPLogin = getSharedPreferences("Login", Context.MODE_PRIVATE);
        token = mSPLogin.getString("token", null);

        // set status
        if (transaksi.getStatus().equals("Jemput")) {
            transaksi.setStatus("OTW");
        }else if (transaksi.getStatus().equals("OTW")) {
            transaksi.setStatus("Ambil-barang");
        }else if (transaksi.getStatus().equals("Ambil-barang")) {
            transaksi.setStatus("sampai-laundry");
        }else if (transaksi.getStatus().equals("Selesai-laundry")){
            transaksi.setStatus("Antar");
        }else if (transaksi.getStatus().equals("Antar")){
            transaksi.setStatus("Done");
        }

        DataService dataService = APIClient.getClient().create(DataService.class);
        dataService.postProsesTransaksi("Bearer " + token,transaksi).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                tampilDialog.dismissLoading();
                if (response.isSuccessful()) {
                    String message = "";
                    tampilDialog.showDialog("Information", "Berhasil input data!","main-activity");
                }else {
                    tampilDialog.showDialog("Failed", response.message(),"");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                tampilDialog.dismissLoading();
                tampilDialog.showDialog("Failed", t.getMessage(),"");
            }
        });
    }
}
