package edu.uph.m24si2.uas_pab;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import edu.uph.m24si2.uas_pab.model.NotifikasiItem;
import edu.uph.m24si2.uas_pab.model.NotifikasiItem.Tipe;

public class NotifikasiActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private LinearLayout llContainer;
    private TextView     tvJumlahBelumDibaca;
    private TextView     chipSemua, chipPeringatan, chipInfo, chipSukses, chipTips;

    // ── Data ──────────────────────────────────────────────────────────────────
    private final List<NotifikasiItem> semuaNotifikasi = new ArrayList<>();
    private String filterAktif = "SEMUA";

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifikasi);

        // Bind views
        llContainer         = findViewById(R.id.llNotifikasiContainer);
        tvJumlahBelumDibaca = findViewById(R.id.tvJumlahBelumDibaca);
        chipSemua           = findViewById(R.id.chipSemua);
        chipPeringatan      = findViewById(R.id.chipPeringatan);
        chipInfo            = findViewById(R.id.chipInfo);
        chipSukses          = findViewById(R.id.chipSukses);
        chipTips            = findViewById(R.id.chipTips);

        // Tombol kembali
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Tandai semua dibaca
        TextView tvTandaiSemua = findViewById(R.id.tvTandaiSemua);
        tvTandaiSemua.setOnClickListener(v -> {
            for (NotifikasiItem item : semuaNotifikasi) item.setSudahDibaca(true);
            updateBadgeBelumDibaca();
            tampilkanNotifikasi(filterAktif);
        });

        // Filter chip listeners
        chipSemua.setOnClickListener(v      -> setFilter("SEMUA"));
        chipPeringatan.setOnClickListener(v -> setFilter("PERINGATAN"));
        chipInfo.setOnClickListener(v       -> setFilter("INFO"));
        chipSukses.setOnClickListener(v     -> setFilter("SUKSES"));
        chipTips.setOnClickListener(v       -> setFilter("TIPS"));

        // Isi data dummy
        isiDataDummy();

        // Tampilkan awal
        updateBadgeBelumDibaca();
        tampilkanNotifikasi("SEMUA");
    }

    // ─── Data dummy ───────────────────────────────────────────────────────────

    private void isiDataDummy() {
        // ── Bulan lalu ────────────────────────────────────────────────────────
        tambah("Anggaran Bulanan Terlampaui!",
                "Pengeluaran bulan Juni mencapai Rp 4.250.000, melebihi anggaran Rp 3.500.000 (121%). Pertimbangkan untuk mengurangi pengeluaran kategori Hiburan.",
                "28 Juni 2026", Tipe.PERINGATAN, true);

        tambah("Pengingat: Catat Transaksi Harian",
                "Kamu belum mencatat transaksi selama 5 hari berturut-turut. Catat sekarang agar laporan keuangan tetap akurat.",
                "25 Juni 2026", Tipe.INFO, true);

        tambah("Target Tabungan Darurat Tercapai! 🎉",
                "Selamat! Kamu berhasil mencapai target tabungan darurat sebesar Rp 10.000.000. Pertahankan konsistensinya!",
                "20 Juni 2026", Tipe.SUKSES, true);

        tambah("Tips: Atur Pengeluaran Makan",
                "Pengeluaran kategori Makanan kamu bulan ini sudah 78% dari total pengeluaran. Coba siapkan bekal dari rumah untuk menghemat.",
                "18 Juni 2026", Tipe.TIPS, true);

        tambah("Pengeluaran Transportasi Melonjak",
                "Pengeluaran transportasi minggu ini Rp 320.000, naik 65% dibanding minggu lalu. Pertimbangkan kendaraan umum atau carpooling.",
                "15 Juni 2026", Tipe.PERINGATAN, true);

        tambah("Pemasukan Bulan Mei Lebih Tinggi",
                "Total pemasukan Mei 2026 sebesar Rp 6.800.000, naik Rp 800.000 dibanding April. Alokasikan kelebihan untuk tabungan!",
                "1 Juni 2026", Tipe.SUKSES, true);

        // ── Minggu lalu ───────────────────────────────────────────────────────
        tambah("Budget Hiburan Hampir Habis",
                "Kamu sudah menggunakan 87% budget hiburan minggu ini (Rp 435.000 dari Rp 500.000). Sisa Rp 65.000 untuk 3 hari ke depan.",
                "5 Juli 2026", Tipe.PERINGATAN, true);

        tambah("Pengingat Cicilan Bulanan",
                "Cicilan langganan streaming senilai Rp 54.000 akan jatuh tempo besok. Pastikan saldo kamu mencukupi.",
                "4 Juli 2026", Tipe.INFO, true);

        tambah("Tips: Manfaatkan Promo Akhir Bulan",
                "Akhir bulan biasanya banyak promo belanja. Rencanakan kebutuhan bulananmu sekarang dan beli saat promo untuk hemat hingga 30%.",
                "3 Juli 2026", Tipe.TIPS, true);

        tambah("Tabungan Liburan Bertambah",
                "Setor tabungan liburan Rp 500.000 berhasil dicatat. Total tabungan liburan kamu kini Rp 3.200.000 dari target Rp 5.000.000 (64%).",
                "2 Juli 2026", Tipe.SUKSES, true);

        // ── Hari ini / kemarin (belum dibaca) ────────────────────────────────
        tambah("Pengeluaran Hari Ini Sudah Tinggi",
                "Total pengeluaran hari ini sudah mencapai Rp 285.000. Kamu masih punya sisa budget harian Rp 115.000.",
                "Hari ini", Tipe.PERINGATAN, false);

        tambah("Jangan Lupa Catat Transaksi!",
                "Sudah jam 8 malam, yuk catat semua pengeluaran dan pemasukan hari ini sebelum tidur agar laporan keuangan tetap rapi.",
                "Hari ini", Tipe.INFO, false);

        tambah("Tips: Investasi Reksa Dana",
                "Dengan saldo lebih Rp 1.200.000 bulan ini, kamu bisa mulai investasi reksa dana pasar uang. Modal mulai Rp 10.000 saja!",
                "Kemarin", Tipe.TIPS, false);

        tambah("Target Tabungan Laptop Hampir Tercapai!",
                "Tabungan laptop kamu sudah Rp 7.800.000 dari target Rp 8.500.000 (92%). Tinggal Rp 700.000 lagi, semangat!",
                "Kemarin", Tipe.SUKSES, false);

        tambah("Pengeluaran Kategori Belanja Meningkat",
                "Pengeluaran belanja minggu ini Rp 890.000, naik 40% dari minggu lalu. Coba buat daftar belanja sebelum ke toko agar tidak boros.",
                "Kemarin", Tipe.PERINGATAN, false);
    }

    private void tambah(String judul, String pesan, String waktu,
                        Tipe tipe, boolean sudahDibaca) {
        NotifikasiItem item = new NotifikasiItem(judul, pesan, waktu, tipe);
        item.setSudahDibaca(sudahDibaca);
        semuaNotifikasi.add(item);
    }

    // ─── Filter ───────────────────────────────────────────────────────────────

    private void setFilter(String tipe) {
        filterAktif = tipe;
        updateChipStyle();
        tampilkanNotifikasi(tipe);
    }

    private void updateChipStyle() {
        resetChip(chipSemua);
        resetChip(chipPeringatan);
        resetChip(chipInfo);
        resetChip(chipSukses);
        resetChip(chipTips);

        switch (filterAktif) {
            case "SEMUA":      aktifkanChip(chipSemua);      break;
            case "PERINGATAN": aktifkanChip(chipPeringatan); break;
            case "INFO":       aktifkanChip(chipInfo);       break;
            case "SUKSES":     aktifkanChip(chipSukses);     break;
            case "TIPS":       aktifkanChip(chipTips);       break;
        }
    }

    private void resetChip(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_chip_nonaktif);
        chip.setTextColor(Color.parseColor("#64748B"));
    }

    private void aktifkanChip(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_chip_aktif);
        chip.setTextColor(Color.WHITE);
    }

    // ─── Render list ──────────────────────────────────────────────────────────

    private void tampilkanNotifikasi(String filter) {
        llContainer.removeAllViews();

        List<NotifikasiItem> filtered = new ArrayList<>();
        for (NotifikasiItem item : semuaNotifikasi) {
            if (filter.equals("SEMUA") || item.getTipe().name().equals(filter)) {
                filtered.add(item);
            }
        }

        if (filtered.isEmpty()) {
            tampilkanKosong();
            return;
        }

        // Kelompokkan: Hari ini & Kemarin → Minggu ini → Bulan lalu
        List<NotifikasiItem> hariIni   = new ArrayList<>();
        List<NotifikasiItem> mingguIni = new ArrayList<>();
        List<NotifikasiItem> bulanLalu = new ArrayList<>();

        for (NotifikasiItem item : filtered) {
            String w = item.getWaktu();
            if (w.equals("Hari ini") || w.equals("Kemarin")) {
                hariIni.add(item);
            } else if (w.contains("Juli")) {
                mingguIni.add(item);
            } else {
                bulanLalu.add(item);
            }
        }

        if (!hariIni.isEmpty()) {
            tambahSeksi("Terbaru", hariIni);
        }
        if (!mingguIni.isEmpty()) {
            tambahSeksi("Minggu Lalu", mingguIni);
        }
        if (!bulanLalu.isEmpty()) {
            tambahSeksi("Bulan Lalu", bulanLalu);
        }
    }

    private void tambahSeksi(String label, List<NotifikasiItem> items) {
        // Label seksi
        TextView tvLabel = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(16), dp(8), dp(16), dp(4));
        tvLabel.setLayoutParams(lp);
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.parseColor("#94A3B8"));
        tvLabel.setTextSize(11f);
        tvLabel.setTypeface(null, Typeface.BOLD);
        tvLabel.setAllCaps(true);
        llContainer.addView(tvLabel);

        for (NotifikasiItem item : items) {
            tambahItemView(item);
        }
    }

    private void tambahItemView(NotifikasiItem item) {
        View v = LayoutInflater.from(this).inflate(R.layout.item_notifikasi, llContainer, false);

        TextView  tvIcon      = v.findViewById(R.id.tvIcon);
        TextView  tvJudul     = v.findViewById(R.id.tvJudul);
        TextView  tvPesan     = v.findViewById(R.id.tvPesan);
        TextView  tvWaktu     = v.findViewById(R.id.tvWaktu);
        TextView  tvBadge     = v.findViewById(R.id.tvBadgeBaru);
        View      indicator   = v.findViewById(R.id.viewIndicator);

        // Konfigurasi berdasarkan tipe
        String emoji;
        int    warnaBg;
        int    warnaIndicator;

        switch (item.getTipe()) {
            case PERINGATAN:
                emoji          = "⚠";
                warnaBg        = Color.parseColor("#FEE2E2");
                warnaIndicator = Color.parseColor("#EF4444");
                break;
            case SUKSES:
                emoji          = "✓";
                warnaBg        = Color.parseColor("#DCFCE7");
                warnaIndicator = Color.parseColor("#16A34A");
                break;
            case TIPS:
                emoji          = "💡";
                warnaBg        = Color.parseColor("#FEF9C3");
                warnaIndicator = Color.parseColor("#EAB308");
                break;
            default: // INFO
                emoji          = "ℹ";
                warnaBg        = Color.parseColor("#DBEAFE");
                warnaIndicator = Color.parseColor("#1A56DB");
                break;
        }

        tvIcon.setText(emoji);
        tvIcon.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(warnaBg));
        indicator.setBackgroundColor(warnaIndicator);

        tvJudul.setText(item.getJudul());
        tvPesan.setText(item.getPesan());
        tvWaktu.setText(item.getWaktu());

        // Tampilkan badge "Baru" jika belum dibaca
        if (!item.isSudahDibaca()) {
            tvBadge.setVisibility(View.VISIBLE);
            // Background card sedikit kebiruan untuk yang belum dibaca
            v.setBackgroundColor(Color.parseColor("#F0F7FF"));
        } else {
            tvBadge.setVisibility(View.GONE);
            v.setBackgroundColor(Color.WHITE);
        }

        // Klik item → tandai sudah dibaca
        v.setOnClickListener(view -> {
            item.setSudahDibaca(true);
            tvBadge.setVisibility(View.GONE);
            v.setBackgroundColor(Color.WHITE);
            updateBadgeBelumDibaca();
        });

        llContainer.addView(v);
    }

    private void tampilkanKosong() {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(48), 0, 0);
        tv.setLayoutParams(lp);
        tv.setText("Tidak ada notifikasi");
        tv.setTextColor(Color.parseColor("#94A3B8"));
        tv.setTextSize(14f);
        tv.setGravity(android.view.Gravity.CENTER);
        llContainer.addView(tv);
    }

    // ─── Badge counter ────────────────────────────────────────────────────────

    private void updateBadgeBelumDibaca() {
        int count = 0;
        for (NotifikasiItem item : semuaNotifikasi) {
            if (!item.isSudahDibaca()) count++;
        }
        if (count > 0) {
            tvJumlahBelumDibaca.setText(count + " notifikasi belum dibaca");
        } else {
            tvJumlahBelumDibaca.setText("Semua notifikasi sudah dibaca");
        }
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
