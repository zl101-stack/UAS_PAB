package edu.uph.m24si2.uas_pab;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import edu.uph.m24si2.uas_pab.adapter.TransactionAdapter;
import edu.uph.m24si2.uas_pab.db.SavingRepository;
import edu.uph.m24si2.uas_pab.db.TransactionRepository;

public class dashboard extends AppCompatActivity {

    // State privasi saldo: true = tampilkan angka, false = sembunyikan
    private boolean saldoVisible = true;

    // Nilai saldo yang di-cache agar tidak perlu query ulang saat toggle
    private long cachedSaldo      = 0;
    private long cachedPemasukan  = 0;
    private long cachedPengeluaran = 0;

    private TextView  tvSaldo;
    private TextView  tvRingkasanPemasukan;
    private TextView  tvRingkasanPengeluaran;
    private ImageButton btnToggleSaldo;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // ── Ambil data dari Intent login ──────────────────────────────────────
        Intent intent = getIntent();
        String userNama = intent.getStringExtra("USER_NAMA");
        userEmail       = intent.getStringExtra("USER_EMAIL");

        // ── Bind views header ─────────────────────────────────────────────────
        TextView tvWelcomeName   = findViewById(R.id.tvWelcomeName);
        TextView tvAvatarInitials = findViewById(R.id.tvAvatarInitials);

        if (userNama != null && !userNama.isEmpty()) {
            tvWelcomeName.setText("Selamat Datang, " + userNama);
            tvAvatarInitials.setText(userNama.substring(0, 1).toUpperCase());
        }

        // ── Bind views saldo ──────────────────────────────────────────────────
        tvSaldo               = findViewById(R.id.tvSaldo);
        tvRingkasanPemasukan  = findViewById(R.id.tvRingkasanPemasukan);
        tvRingkasanPengeluaran = findViewById(R.id.tvRingkasanPengeluaran);
        btnToggleSaldo        = findViewById(R.id.btnToggleSaldo);

        // Toggle mata: ganti ikon dan tampilan saldo
        btnToggleSaldo.setOnClickListener(v -> {
            saldoVisible = !saldoVisible;
            refreshSaldoDisplay();
        });

        // ── Bind CardView menu ────────────────────────────────────────────────
        CardView cardPemasukan      = findViewById(R.id.cardPemasukan);
        CardView cardPengeluaran    = findViewById(R.id.cardPengeluaran);
        CardView cardBudget         = findViewById(R.id.cardBudget);
        CardView cardTargetTabungan = findViewById(R.id.cardTargetTabungan);
        CardView cardGrafik         = findViewById(R.id.cardGrafik);
        CardView cardNotifikasi     = findViewById(R.id.cardNotifikasi);
        CardView cardProfile        = findViewById(R.id.cardProfile);
        CardView cardVideo          = findViewById(R.id.cardVideo);

        cardPemasukan.setOnClickListener(v -> {
            Intent i = new Intent(this, PemasukanActivity.class);
            i.putExtra("USER_EMAIL", userEmail);
            startActivity(i);
        });

        cardPengeluaran.setOnClickListener(v -> {
            Intent i = new Intent(this, PengeluaranActivity.class);
            i.putExtra("USER_EMAIL", userEmail);
            startActivity(i);
        });

        // ── BAGIAN YANG DIGANTI: Navigasi ke Halaman Budget ────────────────────
        cardBudget.setOnClickListener(v -> {
            Intent i = new Intent(this, BudgetActivity.class);
            i.putExtra("USER_EMAIL", userEmail); // Mengirim email pengguna ke halaman budget
            startActivity(i);
        });
        // ────────────────────────────────────────────────────────────────────────

        cardTargetTabungan.setOnClickListener(v -> {
            Intent i = new Intent(this, SavingTargetActivity.class);
            i.putExtra("USER_EMAIL", userEmail);
            startActivity(i);
        });

        cardGrafik.setOnClickListener(v -> {
            Intent i = new Intent(this, GrafikActivity.class);
            i.putExtra("USER_EMAIL", userEmail);
            startActivity(i);
        });

        cardNotifikasi.setOnClickListener(v -> {
            Intent i = new Intent(this, NotifikasiActivity.class);
            i.putExtra("USER_EMAIL", userEmail);
            startActivity(i);
        });

        cardProfile.setOnClickListener(v -> {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("USER_EMAIL", userEmail);
            startActivity(i);
        });

        cardVideo.setOnClickListener(v ->
                Toast.makeText(this, "Fitur Video Edukasi Youtube", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh saldo setiap kali kembali dari PemasukanActivity / PengeluaranActivity
        loadSaldo();
    }

    // ─── Hitung & tampilkan saldo ─────────────────────────────────────────────

    /**
     * Ambil total pemasukan & pengeluaran dari Realm, hitung saldo efektif,
     * kemudian render ke UI sesuai state saldoVisible.
     */
    private void loadSaldo() {
        if (userEmail == null) return;

        cachedPemasukan   = TransactionRepository.getTotalAmount(userEmail, "PEMASUKAN");
        cachedPengeluaran = TransactionRepository.getTotalAmount(userEmail, "PENGELUARAN");
        long totalTabungan = SavingRepository.getTotalSaved(userEmail);

        // ── TAMBAHAN FITUR: Ambil limit budget bulan ini untuk memotong saldo ──
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-yyyy", java.util.Locale.getDefault());
        String monthKey = sdf.format(java.util.Calendar.getInstance().getTime());
        android.content.SharedPreferences prefs = getSharedPreferences("BudgetPrefs", android.content.Context.MODE_PRIVATE);
        long currentBudgetLimit = prefs.getLong("limit_" + userEmail + "_" + monthKey, 0);

        // ── UPDATE RUMUS SALDO: Pemasukan - Pengeluaran + Tabungan - Budget ──
        cachedSaldo       = cachedPemasukan - cachedPengeluaran + totalTabungan - currentBudgetLimit;

        refreshSaldoDisplay();
    }

    /**
     * Render nilai saldo ke TextView berdasarkan state privasi (saldoVisible).
     * Dipanggil saat loadSaldo() dan saat tombol mata di-tap.
     */
    private void refreshSaldoDisplay() {
        if (saldoVisible) {
            // Tampilkan angka nyata
            String saldoText = TransactionAdapter.formatRupiah(Math.abs(cachedSaldo));
            // Saldo negatif → beri tanda minus
            tvSaldo.setText(cachedSaldo < 0 ? "- " + saldoText : saldoText);
            tvSaldo.setTextColor(
                    cachedSaldo < 0
                            ? getColor(R.color.expense_red)
                            : 0xFFFFFFFF
            );

            tvRingkasanPemasukan.setText(
                    TransactionAdapter.formatRupiah(cachedPemasukan));
            tvRingkasanPengeluaran.setText(
                    TransactionAdapter.formatRupiah(cachedPengeluaran));

            // Ikon mata terbuka
            btnToggleSaldo.setImageResource(R.drawable.ic_eye_open);

        } else {
            // Sembunyikan semua angka
            tvSaldo.setText("Rp ••••••");
            tvSaldo.setTextColor(0xFFFFFFFF);
            tvRingkasanPemasukan.setText("••••••");
            tvRingkasanPengeluaran.setText("••••••");

            // Ikon mata tertutup
            btnToggleSaldo.setImageResource(R.drawable.ic_eye_closed);
        }
    }
}