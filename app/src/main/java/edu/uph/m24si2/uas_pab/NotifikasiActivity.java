package edu.uph.m24si2.uas_pab;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import edu.uph.m24si2.uas_pab.model.NotifikasiItem;
import edu.uph.m24si2.uas_pab.model.NotifikasiItem.Tipe;

public class NotifikasiActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private DrawerLayout drawerLayout;
    private LinearLayout llContainer;
    private TextView     tvJumlahBelumDibaca;
    private TextView     chipSemua, chipPeringatan, chipInfo, chipSukses, chipTips;
    private TextView     sidebarBadgeSemua, sidebarBadgeBintang;

    // ── Data ──────────────────────────────────────────────────────────────────
    private final List<NotifikasiItem> semuaNotifikasi = new ArrayList<>();

    /**
     * "SEMUA", "PERINGATAN", "INFO", "SUKSES", "TIPS", "DIBINTANGI"
     */
    private String filterAktif = "SEMUA";

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifikasi);

        // Bind views
        drawerLayout        = findViewById(R.id.drawerLayout);
        llContainer         = findViewById(R.id.llNotifikasiContainer);
        tvJumlahBelumDibaca = findViewById(R.id.tvJumlahBelumDibaca);
        chipSemua           = findViewById(R.id.chipSemua);
        chipPeringatan      = findViewById(R.id.chipPeringatan);
        chipInfo            = findViewById(R.id.chipInfo);
        chipSukses          = findViewById(R.id.chipSukses);
        chipTips            = findViewById(R.id.chipTips);
        sidebarBadgeSemua   = findViewById(R.id.sidebarBadgeSemua);
        sidebarBadgeBintang = findViewById(R.id.sidebarBadgeBintang);

        // ── Toolbar ──────────────────────────────────────────────────────────
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView btnOpenSidebar = findViewById(R.id.btnOpenSidebar);
        btnOpenSidebar.setOnClickListener(v ->
                drawerLayout.openDrawer(Gravity.END));

        TextView tvTandaiSemua = findViewById(R.id.tvTandaiSemua);
        tvTandaiSemua.setOnClickListener(v -> tandaiSemuaDibaca());

        // ── Filter chips ─────────────────────────────────────────────────────
        chipSemua.setOnClickListener(v      -> setFilter("SEMUA"));
        chipPeringatan.setOnClickListener(v -> setFilter("PERINGATAN"));
        chipInfo.setOnClickListener(v       -> setFilter("INFO"));
        chipSukses.setOnClickListener(v     -> setFilter("SUKSES"));
        chipTips.setOnClickListener(v       -> setFilter("TIPS"));

        // ── Sidebar menu ─────────────────────────────────────────────────────
        findViewById(R.id.sidebarSemua).setOnClickListener(v -> {
            setFilter("SEMUA");
            drawerLayout.closeDrawers();
        });
        findViewById(R.id.sidebarDibintangi).setOnClickListener(v -> {
            setFilter("DIBINTANGI");
            drawerLayout.closeDrawers();
        });
        findViewById(R.id.sidebarHapusSemua).setOnClickListener(v -> {
            konfirmasiHapusSemua();
        });
        findViewById(R.id.sidebarTandaiSemua).setOnClickListener(v -> {
            tandaiSemuaDibaca();
            drawerLayout.closeDrawers();
        });

        // ── Load data ─────────────────────────────────────────────────────────
        isiDataDummy();
        refreshSemua();
    }

    // ─── Data dummy ───────────────────────────────────────────────────────────

    private void isiDataDummy() {
        // ── Bulan lalu ────────────────────────────────────────────────────────
        tambah("Anggaran Bulanan Terlampaui!",
                "Pengeluaran bulan Juni mencapai Rp 4.250.000, melebihi anggaran Rp 3.500.000 (121%). Pertimbangkan untuk mengurangi pengeluaran kategori Hiburan dan makan di luar agar bulan depan bisa lebih terkontrol.",
                "28 Juni 2026", Tipe.PERINGATAN, true);

        tambah("Pengingat: Catat Transaksi Harian",
                "Kamu belum mencatat transaksi selama 5 hari berturut-turut. Catat sekarang agar laporan keuangan tetap akurat dan kamu bisa memantau kondisi keuanganmu dengan baik.",
                "25 Juni 2026", Tipe.INFO, true);

        tambah("Target Tabungan Darurat Tercapai! \uD83C\uDF89",
                "Selamat! Kamu berhasil mencapai target tabungan darurat sebesar Rp 10.000.000. Ini adalah pencapaian luar biasa! Pertahankan konsistensinya dan pertimbangkan untuk meningkatkan target berikutnya.",
                "20 Juni 2026", Tipe.SUKSES, true);

        tambah("Tips: Atur Pengeluaran Makan",
                "Pengeluaran kategori Makanan kamu bulan ini sudah 78% dari total pengeluaran. Coba siapkan bekal dari rumah setidaknya 3x seminggu — bisa hemat hingga Rp 600.000 per bulan!",
                "18 Juni 2026", Tipe.TIPS, true);

        tambah("Pengeluaran Transportasi Melonjak",
                "Pengeluaran transportasi minggu ini Rp 320.000, naik 65% dibanding minggu lalu (Rp 194.000). Pertimbangkan menggunakan kendaraan umum atau carpooling bersama rekan untuk menekan biaya.",
                "15 Juni 2026", Tipe.PERINGATAN, true);

        tambah("Pemasukan Bulan Mei Lebih Tinggi",
                "Total pemasukan Mei 2026 sebesar Rp 6.800.000, naik Rp 800.000 dibanding April (Rp 6.000.000). Bagus sekali! Alokasikan kelebihan ini untuk tabungan atau investasi agar uangmu bekerja lebih keras.",
                "1 Juni 2026", Tipe.SUKSES, true);

        // ── Minggu lalu ───────────────────────────────────────────────────────
        tambah("Budget Hiburan Hampir Habis",
                "Kamu sudah menggunakan 87% budget hiburan minggu ini (Rp 435.000 dari Rp 500.000). Sisa budget hanya Rp 65.000 untuk 3 hari ke depan. Pertimbangkan aktivitas hiburan gratis agar tidak over budget.",
                "5 Juli 2026", Tipe.PERINGATAN, true);

        tambah("Pengingat Cicilan Bulanan",
                "Cicilan langganan streaming senilai Rp 54.000 akan jatuh tempo besok (6 Juli 2026). Pastikan saldo rekeningmu mencukupi untuk menghindari denda keterlambatan.",
                "4 Juli 2026", Tipe.INFO, true);

        tambah("Tips: Manfaatkan Promo Akhir Bulan",
                "Akhir bulan biasanya banyak promo belanja online dan offline. Rencanakan daftar kebutuhan bulananmu sekarang dan beli saat promo aktif — bisa hemat hingga 30% dari harga normal.",
                "3 Juli 2026", Tipe.TIPS, true);

        tambah("Tabungan Liburan Bertambah",
                "Setor tabungan liburan Rp 500.000 berhasil dicatat. Total tabungan liburan kamu kini Rp 3.200.000 dari target Rp 5.000.000 (64%). Tinggal Rp 1.800.000 lagi — terus semangat!",
                "2 Juli 2026", Tipe.SUKSES, true);

        // ── Hari ini / kemarin (belum dibaca) ────────────────────────────────
        tambah("Pengeluaran Hari Ini Sudah Tinggi",
                "Total pengeluaran hari ini sudah mencapai Rp 285.000 dari batas harian Rp 400.000. Kamu masih punya sisa budget harian Rp 115.000. Pantau terus agar tidak melewati batas!",
                "Hari ini", Tipe.PERINGATAN, false);

        tambah("Jangan Lupa Catat Transaksi!",
                "Sudah jam 8 malam, yuk catat semua pengeluaran dan pemasukan hari ini sebelum tidur. Kebiasaan mencatat rutin akan membantu kamu memahami pola pengeluaran dan mengambil keputusan keuangan yang lebih baik.",
                "Hari ini", Tipe.INFO, false);

        tambah("Tips: Investasi Reksa Dana",
                "Dengan saldo lebih Rp 1.200.000 bulan ini, kamu bisa mulai investasi reksa dana pasar uang. Modal mulai Rp 10.000 saja, hasil lebih tinggi dari tabungan biasa, dan bisa dicairkan kapan saja!",
                "Kemarin", Tipe.TIPS, false);

        tambah("Target Tabungan Laptop Hampir Tercapai!",
                "Tabungan laptop kamu sudah Rp 7.800.000 dari target Rp 8.500.000 (92%). Tinggal Rp 700.000 lagi untuk mencapai target! Dengan konsistensi yang sama, target bisa tercapai dalam 2 minggu ke depan.",
                "Kemarin", Tipe.SUKSES, false);

        tambah("Pengeluaran Kategori Belanja Meningkat",
                "Pengeluaran belanja minggu ini Rp 890.000, naik 40% dari minggu lalu (Rp 635.000). Coba buat daftar belanja sebelum ke toko dan patuhi daftar tersebut agar tidak tergoda pembelian impulsif.",
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
        // Reset semua chip ke nonaktif
        resetChip(chipSemua);
        resetChip(chipPeringatan);
        resetChip(chipInfo);
        resetChip(chipSukses);
        resetChip(chipTips);

        // Aktifkan chip yang sesuai (DIBINTANGI tidak punya chip, lewat sidebar)
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
            boolean cocok;
            if (filter.equals("SEMUA")) {
                cocok = true;
            } else if (filter.equals("DIBINTANGI")) {
                cocok = item.isDiBintangi();
            } else {
                cocok = item.getTipe().name().equals(filter);
            }
            if (cocok) filtered.add(item);
        }

        if (filtered.isEmpty()) {
            tampilkanKosong(filter.equals("DIBINTANGI")
                    ? "Belum ada notifikasi dibintangi"
                    : "Tidak ada notifikasi");
            return;
        }

        // Kelompokkan berdasarkan waktu
        List<NotifikasiItem> terbaru   = new ArrayList<>();
        List<NotifikasiItem> mingguIni = new ArrayList<>();
        List<NotifikasiItem> bulanLalu = new ArrayList<>();

        for (NotifikasiItem item : filtered) {
            String w = item.getWaktu();
            if (w.equals("Hari ini") || w.equals("Kemarin")) {
                terbaru.add(item);
            } else if (w.contains("Juli")) {
                mingguIni.add(item);
            } else {
                bulanLalu.add(item);
            }
        }

        if (!terbaru.isEmpty())   tambahSeksi("Terbaru", terbaru);
        if (!mingguIni.isEmpty()) tambahSeksi("Minggu Lalu", mingguIni);
        if (!bulanLalu.isEmpty()) tambahSeksi("Bulan Lalu", bulanLalu);
    }

    private void tambahSeksi(String label, List<NotifikasiItem> items) {
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

        for (NotifikasiItem item : items) tambahItemView(item);
    }

    private void tambahItemView(NotifikasiItem item) {
        View v = LayoutInflater.from(this)
                .inflate(R.layout.item_notifikasi, llContainer, false);

        TextView tvIcon    = v.findViewById(R.id.tvIcon);
        TextView tvJudul   = v.findViewById(R.id.tvJudul);
        TextView tvPesan   = v.findViewById(R.id.tvPesan);
        TextView tvWaktu   = v.findViewById(R.id.tvWaktu);
        TextView tvBadge   = v.findViewById(R.id.tvBadgeBaru);
        View     indicator = v.findViewById(R.id.viewIndicator);

        // Konfigurasi tipe
        String emoji; int warnaBg; int warnaInd;
        switch (item.getTipe()) {
            case PERINGATAN:
                emoji = "⚠"; warnaBg = 0xFFFEE2E2; warnaInd = 0xFFEF4444; break;
            case SUKSES:
                emoji = "✓"; warnaBg = 0xFFDCFCE7; warnaInd = 0xFF16A34A; break;
            case TIPS:
                emoji = "💡"; warnaBg = 0xFFFEF9C3; warnaInd = 0xFFEAB308; break;
            default: // INFO
                emoji = "ℹ"; warnaBg = 0xFFDBEAFE; warnaInd = 0xFF1A56DB; break;
        }

        tvIcon.setText(emoji);
        tvIcon.setBackgroundTintList(ColorStateList.valueOf(warnaBg));
        indicator.setBackgroundColor(warnaInd);
        tvJudul.setText(item.getJudul());
        tvPesan.setText(item.getPesan());
        tvWaktu.setText(item.getWaktu());

        // Badge & background belum dibaca
        if (!item.isSudahDibaca()) {
            tvBadge.setVisibility(View.VISIBLE);
            v.setBackgroundColor(Color.parseColor("#F0F7FF"));
        } else {
            tvBadge.setVisibility(View.GONE);
            v.setBackgroundColor(Color.WHITE);
        }

        // Ikon bintang kecil di item jika sudah dibintangi
        if (item.isDiBintangi()) {
            tvWaktu.setText("⭐  " + item.getWaktu());
        }

        // Klik → buka bottom sheet detail
        v.setOnClickListener(view -> {
            item.setSudahDibaca(true);
            tvBadge.setVisibility(View.GONE);
            v.setBackgroundColor(Color.WHITE);
            updateBadgeBelumDibaca();
            bukaDetailNotifikasi(item);
        });

        llContainer.addView(v);
    }

    // ─── Bottom Sheet Detail ──────────────────────────────────────────────────

    private void bukaDetailNotifikasi(NotifikasiItem item) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_notifikasi, null);
        sheet.setContentView(sheetView);

        // Konfigurasi tipe
        String emoji; int warnaBg;
        switch (item.getTipe()) {
            case PERINGATAN: emoji = "⚠"; warnaBg = 0xFFFEE2E2; break;
            case SUKSES:     emoji = "✓"; warnaBg = 0xFFDCFCE7; break;
            case TIPS:       emoji = "💡"; warnaBg = 0xFFFEF9C3; break;
            default:         emoji = "ℹ"; warnaBg = 0xFFDBEAFE; break;
        }

        TextView bsIcon         = sheetView.findViewById(R.id.bsIcon);
        TextView bsJudul        = sheetView.findViewById(R.id.bsJudul);
        TextView bsWaktu        = sheetView.findViewById(R.id.bsWaktu);
        TextView bsPesan        = sheetView.findViewById(R.id.bsPesan);
        LinearLayout btnBintang = sheetView.findViewById(R.id.btnBintang);
        LinearLayout btnHapus   = sheetView.findViewById(R.id.btnHapus);
        TextView tvIconBintang  = sheetView.findViewById(R.id.tvIconBintang);
        TextView tvLabelBintang = sheetView.findViewById(R.id.tvLabelBintang);

        bsIcon.setText(emoji);
        bsIcon.setBackgroundTintList(ColorStateList.valueOf(warnaBg));
        bsJudul.setText(item.getJudul());
        bsWaktu.setText(item.getWaktu());
        bsPesan.setText(item.getPesan());

        // State bintang
        updateTombolBintang(tvIconBintang, tvLabelBintang, item.isDiBintangi());

        // Tombol Bintangi
        btnBintang.setOnClickListener(v -> {
            boolean newState = !item.isDiBintangi();
            item.setDiBintangi(newState);
            updateTombolBintang(tvIconBintang, tvLabelBintang, newState);
            updateSidebarBadgeBintang();
            tampilkanNotifikasi(filterAktif); // refresh list
            String msg = newState ? "Ditambahkan ke Dibintangi ⭐" : "Dihapus dari Dibintangi";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // Tombol Hapus
        btnHapus.setOnClickListener(v -> {
            sheet.dismiss();
            konfirmasiHapusItem(item);
        });

        sheet.show();
    }

    private void updateTombolBintang(TextView icon, TextView label, boolean dibintangi) {
        if (dibintangi) {
            icon.setText("★");
            label.setText("Dibintangi");
        } else {
            icon.setText("☆");
            label.setText("Bintangi");
        }
    }

    // ─── Hapus ────────────────────────────────────────────────────────────────

    private void konfirmasiHapusItem(NotifikasiItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Notifikasi")
                .setMessage("Hapus notifikasi \"" + item.getJudul() + "\"?")
                .setPositiveButton("Hapus", (d, w) -> {
                    semuaNotifikasi.remove(item);
                    refreshSemua();
                    Toast.makeText(this, "Notifikasi dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void konfirmasiHapusSemua() {
        if (semuaNotifikasi.isEmpty()) {
            Toast.makeText(this, "Tidak ada notifikasi", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawers();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Hapus Semua Notifikasi")
                .setMessage("Yakin ingin menghapus semua " + semuaNotifikasi.size() + " notifikasi?")
                .setPositiveButton("Hapus Semua", (d, w) -> {
                    semuaNotifikasi.clear();
                    filterAktif = "SEMUA";
                    updateChipStyle();
                    refreshSemua();
                    drawerLayout.closeDrawers();
                    Toast.makeText(this, "Semua notifikasi dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", (d, w) -> drawerLayout.closeDrawers())
                .show();
    }

    // ─── Tandai semua dibaca ──────────────────────────────────────────────────

    private void tandaiSemuaDibaca() {
        for (NotifikasiItem item : semuaNotifikasi) item.setSudahDibaca(true);
        updateBadgeBelumDibaca();
        tampilkanNotifikasi(filterAktif);
        Toast.makeText(this, "Semua notifikasi ditandai dibaca", Toast.LENGTH_SHORT).show();
    }

    // ─── Refresh helpers ──────────────────────────────────────────────────────

    private void refreshSemua() {
        updateBadgeBelumDibaca();
        updateSidebarBadgeBintang();
        updateSidebarBadgeSemua();
        tampilkanNotifikasi(filterAktif);
    }

    private void updateBadgeBelumDibaca() {
        int count = 0;
        for (NotifikasiItem item : semuaNotifikasi)
            if (!item.isSudahDibaca()) count++;

        tvJumlahBelumDibaca.setText(count > 0
                ? count + " notifikasi belum dibaca"
                : "Semua notifikasi sudah dibaca");

        sidebarBadgeSemua.setText(String.valueOf(semuaNotifikasi.size()));
    }

    private void updateSidebarBadgeSemua() {
        sidebarBadgeSemua.setText(String.valueOf(semuaNotifikasi.size()));
    }

    private void updateSidebarBadgeBintang() {
        int count = 0;
        for (NotifikasiItem item : semuaNotifikasi)
            if (item.isDiBintangi()) count++;

        if (count > 0) {
            sidebarBadgeBintang.setVisibility(View.VISIBLE);
            sidebarBadgeBintang.setText(String.valueOf(count));
        } else {
            sidebarBadgeBintang.setVisibility(View.GONE);
        }
    }

    // ─── Empty state ──────────────────────────────────────────────────────────

    private void tampilkanKosong(String pesan) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(48), 0, 0);
        tv.setLayoutParams(lp);
        tv.setText(pesan);
        tv.setTextColor(Color.parseColor("#94A3B8"));
        tv.setTextSize(14f);
        tv.setGravity(Gravity.CENTER);
        llContainer.addView(tv);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
