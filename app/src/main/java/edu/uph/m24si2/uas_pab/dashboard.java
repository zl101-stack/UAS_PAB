package edu.uph.m24si2.uas_pab;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Binding TextView untuk Nama dan Avatar
        TextView tvWelcomeName = findViewById(R.id.tvWelcomeName);
        TextView tvAvatarInitials = findViewById(R.id.tvAvatarInitials);

        // 1. AMBIL DATA DARI INTENT LOGIN
        Intent intent = getIntent();
        String userNama = intent.getStringExtra("USER_NAMA");

        // 2. SET TEKS NAMA & INISIAL JIKA DATA TIDAK KOSONG
        if (userNama != null && !userNama.isEmpty()) {
            // Ubah teks sapaan
            tvWelcomeName.setText("Selamat Datang, " + userNama);

            // Ambil 1 huruf pertama dari nama untuk dijadikan inisial Avatar
            String inisial = userNama.substring(0, 1).toUpperCase();
            tvAvatarInitials.setText(inisial);
        }

        // Binding CardView Menu dari Layout
        CardView cardPemasukan = findViewById(R.id.cardPemasukan);
        CardView cardPengeluaran = findViewById(R.id.cardPengeluaran);
        CardView cardBudget = findViewById(R.id.cardBudget);
        CardView cardTargetTabungan = findViewById(R.id.cardTargetTabungan);
        CardView cardGrafik = findViewById(R.id.cardGrafik);
        CardView cardNotifikasi = findViewById(R.id.cardNotifikasi);
        CardView cardProfile = findViewById(R.id.cardProfile);
        CardView cardVideo = findViewById(R.id.cardVideo);

        // Aksi klik menu
        cardPemasukan.setOnClickListener(v ->
                Toast.makeText(this, "Fitur Input Pemasukan", Toast.LENGTH_SHORT).show());

        cardPengeluaran.setOnClickListener(v ->
                Toast.makeText(this, "Fitur Input Pengeluaran", Toast.LENGTH_SHORT).show());

        cardBudget.setOnClickListener(v ->
                Toast.makeText(this, "Fitur Budget Bulanan", Toast.LENGTH_SHORT).show());

        cardTargetTabungan.setOnClickListener(v ->
                Toast.makeText(this, "Fitur Target Tabungan", Toast.LENGTH_SHORT).show());

        cardGrafik.setOnClickListener(v ->
                Toast.makeText(this, "Fitur Grafik Keuangan", Toast.LENGTH_SHORT).show());

        cardNotifikasi.setOnClickListener(v ->
                Toast.makeText(this, "Fitur Notifikasi", Toast.LENGTH_SHORT).show());

        cardProfile.setOnClickListener(v ->
                Toast.makeText(this, "Fitur Profile", Toast.LENGTH_SHORT).show());

        cardVideo.setOnClickListener(v ->
                Toast.makeText(this, "Fitur Video Edukasi Youtube", Toast.LENGTH_SHORT).show());
    }
}