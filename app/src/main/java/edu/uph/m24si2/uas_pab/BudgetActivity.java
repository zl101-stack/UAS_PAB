package edu.uph.m24si2.uas_pab;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import edu.uph.m24si2.uas_pab.adapter.TransactionAdapter;
import edu.uph.m24si2.uas_pab.db.TransactionRepository;

public class BudgetActivity extends AppCompatActivity {

    // Daftar kategori — sama persis dengan PengeluaranActivity
    private static final String[][] CATEGORIES = {
            {"Makan & Minum",  "makan"},
            {"Transportasi",   "transport"},
            {"Belanja",        "belanja"},
            {"Tagihan",        "tagihan"},
            {"Kesehatan",      "kesehatan"},
            {"Hiburan",        "hiburan"},
            {"Pendidikan",     "pendidikan"},
            {"Lainnya",        "lainnya_out"},
    };

    private String userEmail;
    private Calendar currentCalendar;
    private SharedPreferences sharedPreferences;

    private TextView             tvMonthYear, tvSisaBudget, tvTerpakai;
    private ProgressBar          progressBarBudget;
    private EditText             etBudgetLimit, etUseBudget;
    private AutoCompleteTextView spinnerKategoriBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null) userEmail = "default";

        sharedPreferences  = getSharedPreferences("BudgetPrefs", Context.MODE_PRIVATE);
        currentCalendar    = Calendar.getInstance();

        // ── Bind views ────────────────────────────────────────────────────────
        ImageView btnBack     = findViewById(R.id.btnBack);
        tvMonthYear           = findViewById(R.id.tvMonthYear);
        ImageView btnPrevMonth = findViewById(R.id.btnPrevMonth);
        ImageView btnNextMonth = findViewById(R.id.btnNextMonth);
        tvSisaBudget          = findViewById(R.id.tvSisaBudget);
        tvTerpakai            = findViewById(R.id.tvTerpakai);
        progressBarBudget     = findViewById(R.id.progressBarBudget);
        etBudgetLimit         = findViewById(R.id.etBudgetLimit);
        Button btnSaveBudget  = findViewById(R.id.btnSaveBudget);
        etUseBudget           = findViewById(R.id.etUseBudget);
        Button btnUseBudget   = findViewById(R.id.btnUseBudget);
        Button btnResetBudget = findViewById(R.id.btnResetBudget);
        spinnerKategoriBudget = findViewById(R.id.spinnerKategoriBudget);

        // ── Setup dropdown kategori ───────────────────────────────────────────
        String[] labels = new String[CATEGORIES.length];
        for (int i = 0; i < CATEGORIES.length; i++) labels[i] = CATEGORIES[i][0];

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, labels);
        spinnerKategoriBudget.setAdapter(catAdapter);
        spinnerKategoriBudget.setThreshold(0);
        spinnerKategoriBudget.setOnClickListener(v -> {
            hideKeyboard();
            spinnerKategoriBudget.showDropDown();
        });
        spinnerKategoriBudget.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                hideKeyboard();
                spinnerKategoriBudget.showDropDown();
            }
        });

        // ── Navigasi ─────────────────────────────────────────────────────────
        btnBack.setOnClickListener(v -> finish());
        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            loadMonthData();
        });
        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            loadMonthData();
        });

        // ── Simpan Budget → langsung potong saldo ────────────────────────────
        btnSaveBudget.setOnClickListener(v -> {
            hideKeyboard();
            String input = etBudgetLimit.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Masukkan nominal target budget!", Toast.LENGTH_SHORT).show();
                return;
            }

            long limit         = Long.parseLong(input);
            long saldoSekarang = getSaldoSekarang();

            // Validasi: budget tidak boleh melebihi total saldo
            if (limit > saldoSekarang) {
                new AlertDialog.Builder(this)
                        .setTitle("⚠ Budget Melebihi Saldo")
                        .setMessage("Budget Anda Melebihi Total Saldo!\n\n" +
                                "Total Saldo    : " + TransactionAdapter.formatRupiah(saldoSekarang) + "\n" +
                                "Budget Input   : " + TransactionAdapter.formatRupiah(limit) + "\n\n" +
                                "Silakan masukkan nominal yang tidak melebihi total saldo Anda.")
                        .setPositiveButton("Oke", null)
                        .show();
                return;
            }

            // Simpan limit ke SharedPreferences
            saveBudgetLimit(limit);

            // Catat sebagai PENGELUARAN → saldo dashboard otomatis berkurang
            String tanggal = getTanggalHari();
            String bulanTahun = new SimpleDateFormat("MMMM yyyy", new Locale("id", "ID"))
                    .format(currentCalendar.getTime());
            TransactionRepository.addTransaction(
                    userEmail,
                    "PENGELUARAN",
                    limit,
                    "Budget",
                    "ic_budget",
                    tanggal,
                    "Budget bulanan " + bulanTahun
            );

            etBudgetLimit.setText("");
            Toast.makeText(this,
                    "Budget " + TransactionAdapter.formatRupiah(limit) + " disimpan! Saldo berkurang.",
                    Toast.LENGTH_SHORT).show();
            loadMonthData();
        });

        // ── Kurangi Dana → wajib pilih kategori → masuk histori pengeluaran ──
        btnUseBudget.setOnClickListener(v -> {
            hideKeyboard();
            String input    = etUseBudget.getText().toString().trim();
            String kategori = spinnerKategoriBudget.getText().toString().trim();

            if (input.isEmpty()) {
                Toast.makeText(this, "Masukkan nominal dana yang dipakai!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (kategori.isEmpty()) {
                Toast.makeText(this, "Pilih kategori pengeluaran terlebih dahulu!", Toast.LENGTH_SHORT).show();
                spinnerKategoriBudget.showDropDown();
                return;
            }

            long used = Long.parseLong(input);

            // Cek budget sudah diatur
            String monthKey    = getMonthKey();
            long limit         = sharedPreferences.getLong("limit_" + userEmail + "_" + monthKey, 0);
            long sudahDipakai  = sharedPreferences.getLong("used_"  + userEmail + "_" + monthKey, 0);
            long sisaBudget    = limit - sudahDipakai;

            if (limit == 0) {
                Toast.makeText(this, "Atur target budget dulu sebelum memakai dana!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (used > sisaBudget) {
                Toast.makeText(this,
                        "Melebihi sisa budget!\nSisa budget: " + TransactionAdapter.formatRupiah(sisaBudget),
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Cek saldo mencukupi
            long saldoSekarang = getSaldoSekarang();
            if (used > saldoSekarang) {
                new AlertDialog.Builder(this)
                        .setTitle("⚠ Saldo Tidak Cukup")
                        .setMessage("Saldo Anda tidak mencukupi!\n\n" +
                                "Saldo saat ini : " + TransactionAdapter.formatRupiah(saldoSekarang) + "\n" +
                                "Dana yang dipakai: " + TransactionAdapter.formatRupiah(used))
                        .setPositiveButton("Oke", null)
                        .show();
                return;
            }

            // Cari icon kategori
            String categoryIcon = "lainnya_out";
            for (String[] cat : CATEGORIES) {
                if (cat[0].equals(kategori)) { categoryIcon = cat[1]; break; }
            }

            // Catat di histori pengeluaran tapi TIDAK kurangi saldo
            // (saldo sudah dipotong saat budget dibuat)
            TransactionRepository.addBudgetTransaction(
                    userEmail,
                    used,
                    kategori,
                    categoryIcon,
                    getTanggalHari(),
                    "Pemakaian budget - " + kategori
            );

            // Update tracker used di SharedPreferences
            addBudgetUsed(used);

            etUseBudget.setText("");
            spinnerKategoriBudget.setText("", false);
            Toast.makeText(this,
                    "Dana dipakai! Budget berkurang " + TransactionAdapter.formatRupiah(used),
                    Toast.LENGTH_SHORT).show();
            loadMonthData();
        });

        btnResetBudget.setOnClickListener(v -> {
            String monthKey = getMonthKey();
            long limit = sharedPreferences.getLong("limit_" + userEmail + "_" + monthKey, 0);

            String pesan = "Reset semua data budget bulan ini ke 0?";
            if (limit > 0) {
                pesan = "Budget " + TransactionAdapter.formatRupiah(limit) +
                        " akan dikembalikan penuh ke total saldo.\n\nLanjutkan reset?";
            }

            new AlertDialog.Builder(this)
                    .setTitle("Reset Budget?")
                    .setMessage(pesan)
                    .setPositiveButton("Ya", (d, w) -> {
                        String bulanTahun = new SimpleDateFormat("MMMM yyyy",
                                new Locale("id", "ID")).format(currentCalendar.getTime());

                        // Hapus histori pengeluaran budget bulan ini
                        // Saldo otomatis kembali karena pengeluarannya dihapus
                        TransactionRepository.deleteBudgetTransactions(userEmail, bulanTahun);

                        // Reset data SharedPreferences
                        sharedPreferences.edit()
                                .putLong("limit_" + userEmail + "_" + monthKey, 0)
                                .putLong("used_"  + userEmail + "_" + monthKey, 0)
                                .apply();

                        Toast.makeText(this,
                                limit > 0
                                        ? "Budget direset! Dana " + TransactionAdapter.formatRupiah(limit) + " telah dikembalikan ke saldo."
                                        : "Budget bulan ini direset!",
                                Toast.LENGTH_LONG).show();
                        loadMonthData();
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        });

        loadMonthData();
    }

    // ─── Helper tanggal ───────────────────────────────────────────────────────

    private String getTanggalHari() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(currentCalendar.getTime());
    }

    private String getMonthKey() {
        return new SimpleDateFormat("MM-yyyy", Locale.getDefault())
                .format(currentCalendar.getTime());
    }

    // ─── Load & render data bulan ─────────────────────────────────────────────

    private void loadMonthData() {
        tvMonthYear.setText(new SimpleDateFormat("MMMM yyyy", new Locale("id", "ID"))
                .format(currentCalendar.getTime()));

        String monthKey = getMonthKey();
        long limit = sharedPreferences.getLong("limit_" + userEmail + "_" + monthKey, 0);
        long used  = sharedPreferences.getLong("used_"  + userEmail + "_" + monthKey, 0);

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
                tvSisaBudget.setTextColor(Color.parseColor("#EF4444"));
            } else {
                tvSisaBudget.setText(TransactionAdapter.formatRupiah(sisa));
                tvSisaBudget.setTextColor(Color.parseColor("#10B981"));
            }
            tvTerpakai.setText("Terpakai " + TransactionAdapter.formatRupiah(used)
                    + " / " + TransactionAdapter.formatRupiah(limit));

            progressBarBudget.setMax(100);
            int progress = (int) ((used * 100) / limit);
            if (progress > 100) progress = 100;
            progressBarBudget.setProgress(progress);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void saveBudgetLimit(long limit) {
        sharedPreferences.edit()
                .putLong("limit_" + userEmail + "_" + getMonthKey(), limit)
                .apply();
    }

    private void addBudgetUsed(long amount) {
        String key     = "used_" + userEmail + "_" + getMonthKey();
        long current   = sharedPreferences.getLong(key, 0);
        sharedPreferences.edit().putLong(key, current + amount).apply();
    }

    /** Saldo bersih = total pemasukan - total pengeluaran dari Realm. */
    private long getSaldoSekarang() {
        long pemasukan   = TransactionRepository.getTotalAmount(userEmail, "PEMASUKAN");
        long pengeluaran = TransactionRepository.getTotalAmount(userEmail, "PENGELUARAN");
        return pemasukan - pengeluaran;
    }

    /** Tutup soft keyboard. */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
