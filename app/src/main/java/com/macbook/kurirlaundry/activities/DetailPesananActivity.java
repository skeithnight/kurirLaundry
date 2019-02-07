package com.macbook.kurirlaundry.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.macbook.kurirlaundry.R;
import com.macbook.kurirlaundry.TampilDialog;
import com.macbook.kurirlaundry.api.APIClient;
import com.macbook.kurirlaundry.api.DataService;
import com.macbook.kurirlaundry.models.MenuLaundry;
import com.macbook.kurirlaundry.models.Service;
import com.macbook.kurirlaundry.models.Transaksi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DetailPesananActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static String TAG = "Testing";
    Transaksi transaksi;
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
    @BindView(R.id.total_tagihan)
    TextView totaltagihan;
    @BindView(R.id.btn_detail_pesanan)
    Button btnDetailPesan;
    private String token, idUser;
    private int MY_PERMISSIONS_REQUEST_Location = 1;
    private TampilDialog tampilDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pesanan);
        ButterKnife.bind(this);
        tampilDialog = new TampilDialog(this);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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


        // double into currency
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        ArrayList<String> list = new ArrayList<>();
        double totalTagihan = 0;

        Resources res = getResources();

        for (Service item : transaksi.getMenuLaundry()
                ) {
            if (item.getService() != null) {

                String text = res.getString(R.string.format_layanan_laundry, item.getService().getJenis(), formatRupiah.format((int)item.getService().getHarga()), item.getService().getSatuan(),item.getJumlah());
//                Log.i(TAG, "setData: "+text);
                list.add(text);
            }
            totalTagihan += (item.getService() == null) ? 0 : item.getService().getHarga();
        }

        totaltagihan.setText(formatRupiah.format((double) totalTagihan));

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
        } else if (transaksi.getStatus().equals("OTW")) {
            btnDetailPesan.setVisibility(View.VISIBLE);
            btnDetailPesan.setText("Ambil Barang");
        } else if (transaksi.getStatus().equals("Ambil-barang")) {
            btnDetailPesan.setVisibility(View.VISIBLE);
            btnDetailPesan.setText("Sampai Laundry");
        } else if (transaksi.getStatus().equals("Selesai-laundry")) {
            btnDetailPesan.setVisibility(View.VISIBLE);
            btnDetailPesan.setText("Antar Barang");
        } else if (transaksi.getStatus().equals("Antar")) {
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
        } else if (transaksi.getStatus().equals("OTW")) {
            transaksi.setStatus("Ambil-barang");
        } else if (transaksi.getStatus().equals("Ambil-barang")) {
            transaksi.setStatus("sampai-laundry");
        } else if (transaksi.getStatus().equals("Selesai-laundry")) {
            transaksi.setStatus("Antar");
        } else if (transaksi.getStatus().equals("Antar")) {
            transaksi.setStatus("Done");
        }

        DataService dataService = APIClient.getClient().create(DataService.class);
        dataService.postProsesTransaksi("Bearer " + token, transaksi).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                tampilDialog.dismissLoading();
                if (response.isSuccessful()) {
                    String message = "";
                    tampilDialog.showDialog("Information", "Berhasil input data!", "main-activity");
                } else {
                    tampilDialog.showDialog("Failed", response.message(), "");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                tampilDialog.dismissLoading();
                tampilDialog.showDialog("Failed", t.getMessage(), "");
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng sydney = new LatLng(transaksi.getLatitude(), transaksi.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title("Lokasi penjemputan"));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_Location);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

        //Moving the camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        //Animating the camera
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @OnClick(R.id.btn_navigate)
    public void navigateLocation() {
        // Create a Uri from an intent string. Use the result to create an Intent.
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + transaksi.getLatitude() + "," + transaksi.getLongitude()+"(Lokasi jemput)");

// Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
// Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

// Attempt to start an activity that can handle the Intent
        startActivity(mapIntent);
    }
}
