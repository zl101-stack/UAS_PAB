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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.uph.m24si2.uas_pab.adapter.TransactionAdapter;

public class BudgetActivity extends AppCompatActivity {

    private String userEmail;
    private Calendar currentCalendar;
    private SharedPreferences sharedPreferences;

    private TextView tvMonthYear, tvSisaBudget, tvTerpakai;
    private ProgressBar progressBarBudget;
    private EditText etBudgetLimit, etUseBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) userEmail = "default";

        sharedPreferences = getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE);
        currentCalendar = Calendar.getInstance();

        ImageView btnBack = findViewById(R.id.btnBack);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        ImageView btnPrevMonth = findViewById(R.id.btnPrevMonth);
        ImageView btnNextMonth = findViewById(R.id.btnNextMonth);

        tvSisaBudget = findViewById(R.id.tvSisaBudget);
        tvTerpakai = findViewById(R.id.tvTerpakai);
        progressBarBudget = findViewById(R.id.progressBarBudget);

        etBudgetLimit = findViewById(R.id.etBudgetLimit);
        Button btnSaveBudget = findViewById(R.id.btnSaveBudget);

        etUseBudget = findViewById(R.id.etUseBudget);
        Button btnUseBudget = findViewById(R.id.btnUseBudget);

        btnBack.setOnClickListener(v -> finish());

        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            loadMonthData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            loadMonthData();
        });

        // Simpan / Edit Target Budget
        btnSaveBudget.setOnClickListener(v -> {
            String input = etBudgetLimit.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Masukkan nominal target budget!", Toast.LENGTH_SHORT).show();
                return;
            }
            long limit = Long.parseLong(input);
            saveBudgetLimit(limit);
            etBudgetLimit.setText("");
            Toast.makeText(this, "Target budget berhasil disimpan!", Toast.LENGTH_SHORT).show();
            loadMonthData();
        });

        // Kurangi Dana Budget (Pemakaian)
        btnUseBudget.setOnClickListener(v -> {
            String input = etUseBudget.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Masukkan nominal dana yang dipakai!", Toast.LENGTH_SHORT).show();
                return;
            }
            long used = Long.parseLong(input);
            addBudgetUsed(used);
            etUseBudget.setText("");
            Toast.makeText(this, "Dana berhasil dikurangi dari budget!", Toast.LENGTH_SHORT).show();
            loadMonthData();
        });

        loadMonthData();
    }

    private String getMonthKey() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
        return sdf.format(currentCalendar.getTime());
    }

    private void loadMonthData() {
        SimpleDateFormat sdfDisplay = new SimpleDateFormat("MMMM yyyy", new Locale("id", "ID"));
        tvMonthYear.setText(sdfDisplay.format(currentCalendar.getTime()));

        String monthKey = getMonthKey();
        long limit = sharedPreferences.getLong("limit_" + userEmail + "_" + monthKey, 0);
        long used = sharedPreferences.getLong("used_" + userEmail + "_" + monthKey, 0);

        // AUTO-HEAL: Kalau ada data minus dari error sebelumnya, paksa jadi 0
        if (used < 0) {
            used = 0;
            sharedPreferences.edit().putLong("used_" + userEmail + "_" + monthKey, 0).apply();
        }

        long sisa = limit - used;

        if (limit == 0) {
            tvSisaBudget.setText("Atur Dulu");
            tvSisaBudget.setTextColor(Color.parseColor("#64748B"));
            tvTerpakai.setText("Terpakai Rp 0 / Rp 0");
            progressBarBudget.setMax(100);
            progressBarBudget.setProgress(0);
        } else {
            if (sisa < 0) {
                tvSisaBudget.setText("- " + TransactionAdapter.formatRupiah(Math.abs(sisa)));
                tvSisaBudget.setTextColor(Color.parseColor("#EF4444")); // Merah (Over)
            } else {
                tvSisaBudget.setText(TransactionAdapter.formatRupiah(sisa));
                tvSisaBudget.setTextColor(Color.parseColor("#10B981")); // Hijau (Aman)
            }

            tvTerpakai.setText("Terpakai " + TransactionAdapter.formatRupiah(used) + " / " + TransactionAdapter.formatRupiah(limit));

            progressBarBudget.setMax(100);
            int progress = (int) ((used * 100) / limit);
            if (progress > 100) progress = 100;
            progressBarBudget.setProgress(progress);
        }
    }

    private void saveBudgetLimit(long limit) {
        String monthKey = getMonthKey();
        sharedPreferences.edit().putLong("limit_" + userEmail + "_" + monthKey, limit).apply();
    }

    private void addBudgetUsed(long amount) {
        String monthKey = getMonthKey();
        long currentUsed = sharedPreferences.getLong("used_" + userEmail + "_" + monthKey, 0);
        sharedPreferences.edit().putLong("used_" + userEmail + "_" + monthKey, currentUsed + amount).apply();
    }
}