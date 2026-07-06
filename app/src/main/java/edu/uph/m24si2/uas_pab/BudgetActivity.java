package edu.uph.m24si2.uas_pab;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.uph.m24si2.uas_pab.adapter.TransactionAdapter;
import edu.uph.m24si2.uas_pab.db.TransactionRepository;

public class BudgetActivity extends AppCompatActivity {

    private String userEmail;
    private long totalPengeluaran = 0;
    private long budgetLimit = 0;

    private EditText etBudgetLimit;
    private TextView tvTargetBudget, tvTotalPengeluaran, tvSisaBudget, tvStatusPersentase;
    private ProgressBar progressBarBudget;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        // Ambil email dari intent
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) userEmail = "default";

        // Inisialisasi SharedPreferences untuk menyimpan budget secara lokal per akun
        sharedPreferences = getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE);

        // Bind Views
        ImageView btnBack = findViewById(R.id.btnBack);
        etBudgetLimit = findViewById(R.id.etBudgetLimit);
        Button btnSaveBudget = findViewById(R.id.btnSaveBudget);
        tvTargetBudget = findViewById(R.id.tvTargetBudget);
        tvTotalPengeluaran = findViewById(R.id.tvTotalPengeluaran);
        tvSisaBudget = findViewById(R.id.tvSisaBudget);
        tvStatusPersentase = findViewById(R.id.tvStatusPersentase);
        progressBarBudget = findViewById(R.id.progressBarBudget);

        // Aksi tombol kembali
        btnBack.setOnClickListener(v -> finish());

        // Aksi simpan budget
        btnSaveBudget.setOnClickListener(v -> {
            String input = etBudgetLimit.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Masukkan nominal anggaran!", Toast.LENGTH_SHORT).show();
                return;
            }
            budgetLimit = Long.parseLong(input);

            // Simpan ke SharedPreferences dengan key unik berdasarkan email
            sharedPreferences.edit().putLong("budget_" + userEmail, budgetLimit).apply();

            Toast.makeText(this, "Anggaran berhasil disimpan!", Toast.LENGTH_SHORT).show();
            etBudgetLimit.setText(""); // Kosongkan form
            calculateBudget(); // Hitung ulang layar
        });

        // Load data pertama kali dibuka
        loadData();
    }

    private void loadData() {
        // 1. Ambil target budget yang pernah disimpan
        budgetLimit = sharedPreferences.getLong("budget_" + userEmail, 0);

        // 2. Ambil total PENGELUARAN asli dari database Realm (menggunakan Repository buatanmu)
        totalPengeluaran = TransactionRepository.getTotalAmount(userEmail, "PENGELUARAN");

        // 3. Kalkulasi dan tampilkan
        calculateBudget();
    }

    private void calculateBudget() {
        // Tampilkan format Rupiah di ringkasan
        tvTargetBudget.setText(TransactionAdapter.formatRupiah(budgetLimit));
        tvTotalPengeluaran.setText(TransactionAdapter.formatRupiah(totalPengeluaran));

        long sisa = budgetLimit - totalPengeluaran;

        if (budgetLimit == 0) {
            tvSisaBudget.setText("Atur Anggaran Dulu");
            tvSisaBudget.setTextColor(Color.parseColor("#64748B")); // <-- Diubah agar pas dengan tema terang
            progressBarBudget.setProgress(0);
            tvStatusPersentase.setText("Terpakai 0%");
        } else {
            // Hitung sisa
            if (sisa < 0) {
                // Over budget
                tvSisaBudget.setText("- " + TransactionAdapter.formatRupiah(Math.abs(sisa)));
                tvSisaBudget.setTextColor(Color.parseColor("#EF4444")); // Merah (Melewati batas)
            } else {
                // Masih aman
                tvSisaBudget.setText(TransactionAdapter.formatRupiah(sisa));
                tvSisaBudget.setTextColor(Color.parseColor("#10B981")); // Hijau (Aman)
            }

            // Hitung persentase untuk Progress Bar
            int persentase = (int) ((totalPengeluaran * 100) / budgetLimit);
            if (persentase > 100) persentase = 100;

            progressBarBudget.setProgress(persentase);
            tvStatusPersentase.setText("Terpakai " + persentase + "%");
        }
    }
}