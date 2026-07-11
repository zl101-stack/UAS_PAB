package edu.uph.m24si2.uas_pab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EdukasiActivity extends AppCompatActivity {

    // ── Data model video edukasi ────────────────────────────────────────────
    private static class VideoEdukasi {
        String judul;
        String kategori;
        String youtubeId;
        String durasi;
        String channel;
        String emoji;

        VideoEdukasi(String judul, String kategori, String youtubeId,
                     String durasi, String channel, String emoji) {
            this.judul      = judul;
            this.kategori   = kategori;
            this.youtubeId  = youtubeId;
            this.durasi     = durasi;
            this.channel    = channel;
            this.emoji      = emoji;
        }
    }

    // ── Master list semua video ─────────────────────────────────────────────
    private final List<VideoEdukasi> allVideos = new ArrayList<>();

    // ── List yang sedang ditampilkan (setelah filter) ───────────────────────
    private final List<VideoEdukasi> filteredVideos = new ArrayList<>();

    // ── Adapter & Views ─────────────────────────────────────────────────────
    private VideoAdapter   adapter;
    private TextView       tvCountVideo;
    private String         activeCategory = "Semua";

    // Chip views
    private TextView chipSemua, chipInvestasi, chipTabungan,
                     chipBudgeting, chipBebasUtang;

    // ── Chip colors ─────────────────────────────────────────────────────────
    private static final int COLOR_ACTIVE_BG   = 0xFF1A56DB;
    private static final int COLOR_ACTIVE_TEXT  = 0xFFFFFFFF;
    private static final int COLOR_INACTIVE_BG  = 0xFFFFFFFF;
    private static final int COLOR_INACTIVE_TEXT = 0xFF64748B;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edukasi);

        // ── Tombol back ──────────────────────────────────────────────────────
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ── Isi data video ───────────────────────────────────────────────────
        populateVideos();

        // ── Setup RecyclerView ───────────────────────────────────────────────
        RecyclerView rvVideo = findViewById(R.id.rvVideo);
        rvVideo.setLayoutManager(new LinearLayoutManager(this));
        rvVideo.setHasFixedSize(false);
        rvVideo.setNestedScrollingEnabled(false);

        adapter = new VideoAdapter(filteredVideos);
        rvVideo.setAdapter(adapter);

        // ── Bind counter ─────────────────────────────────────────────────────
        tvCountVideo = findViewById(R.id.tvCountVideo);

        // ── Bind chips ───────────────────────────────────────────────────────
        chipSemua      = findViewById(R.id.chipSemua);
        chipInvestasi  = findViewById(R.id.chipInvestasi);
        chipTabungan   = findViewById(R.id.chipTabungan);
        chipBudgeting  = findViewById(R.id.chipBudgeting);
        chipBebasUtang = findViewById(R.id.chipBebasUtang);

        chipSemua.setOnClickListener(v      -> applyFilter("Semua"));
        chipInvestasi.setOnClickListener(v  -> applyFilter("Investasi"));
        chipTabungan.setOnClickListener(v   -> applyFilter("Tabungan"));
        chipBudgeting.setOnClickListener(v  -> applyFilter("Budgeting"));
        chipBebasUtang.setOnClickListener(v -> applyFilter("Bebas Utang"));

        // ── Tampilkan semua video saat pertama buka ──────────────────────────
        applyFilter("Semua");
    }

    // ── Data video (10 video edukasi keuangan) ───────────────────────────────
    private void populateVideos() {
        allVideos.add(new VideoEdukasi(
                "Tips Menabung dengan Metode 50-30-20",
                "Tabungan",
                "eIho2S0ZahI",   // video edukasi keuangan
                "8:24",
                "Finansialku",
                "🐷"
        ));
        allVideos.add(new VideoEdukasi(
                "Cara Investasi Saham untuk Pemula",
                "Investasi",
                "7WdaEX-KKQM",
                "12:05",
                "Investasi Indonesia",
                "📈"
        ));
        allVideos.add(new VideoEdukasi(
                "Budgeting Bulanan yang Efektif dan Mudah",
                "Budgeting",
                "HQzoZfc3GwQ",
                "10:30",
                "Finansialku",
                "📊"
        ));
        allVideos.add(new VideoEdukasi(
                "Strategi Bebas Utang dalam 1 Tahun",
                "Bebas Utang",
                "mN7W6yNFVNI",
                "15:42",
                "Prita Ghozie",
                "💳"
        ));
        allVideos.add(new VideoEdukasi(
                "Reksa Dana untuk Pemula: Mulai dari Rp 10.000",
                "Investasi",
                "pM8e0vmRmMU",
                "9:18",
                "Duit Pintar",
                "📦"
        ));
        allVideos.add(new VideoEdukasi(
                "Dana Darurat: Berapa Bulan Pengeluaran yang Ideal?",
                "Tabungan",
                "F3tiu9yxYgE",
                "7:55",
                "Finansialku",
                "🛡️"
        ));
        allVideos.add(new VideoEdukasi(
                "Cara Membaca Laporan Keuangan Perusahaan",
                "Investasi",
                "mUPT4IPlWLU",
                "11:20",
                "Saham Gain",
                "📄"
        ));
        allVideos.add(new VideoEdukasi(
                "Zero Based Budgeting: Cara Ampuh Kontrol Pengeluaran",
                "Budgeting",
                "sI-f3JCKG1A",
                "13:00",
                "Prita Ghozie",
                "🎯"
        ));
        allVideos.add(new VideoEdukasi(
                "Tips Lunasi KPR Lebih Cepat dan Hemat Bunga",
                "Bebas Utang",
                "gvkqT_Uoahw",
                "6:45",
                "Property Insight",
                "🏠"
        ));
        allVideos.add(new VideoEdukasi(
                "Mulai Investasi dengan Modal Rp 100.000 per Bulan",
                "Investasi",
                "5MzIBMJwMzM",
                "8:10",
                "Duit Pintar",
                "💰"
        ));
    }

    // ── Filter video berdasarkan kategori ────────────────────────────────────
    private void applyFilter(String kategori) {
        activeCategory = kategori;
        filteredVideos.clear();

        for (VideoEdukasi v : allVideos) {
            if (kategori.equals("Semua") || v.kategori.equals(kategori)) {
                filteredVideos.add(v);
            }
        }

        adapter.notifyDataSetChanged();
        tvCountVideo.setText(filteredVideos.size() + " video");

        // Update tampilan chip
        resetChips();
        setActiveChip(kategori);
    }

    // ── Reset semua chip ke state tidak aktif ────────────────────────────────
    private void resetChips() {
        setChipStyle(chipSemua,      false);
        setChipStyle(chipInvestasi,  false);
        setChipStyle(chipTabungan,   false);
        setChipStyle(chipBudgeting,  false);
        setChipStyle(chipBebasUtang, false);
    }

    // ── Aktifkan chip yang dipilih ───────────────────────────────────────────
    private void setActiveChip(String kategori) {
        switch (kategori) {
            case "Semua":       setChipStyle(chipSemua,      true); break;
            case "Investasi":   setChipStyle(chipInvestasi,  true); break;
            case "Tabungan":    setChipStyle(chipTabungan,   true); break;
            case "Budgeting":   setChipStyle(chipBudgeting,  true); break;
            case "Bebas Utang": setChipStyle(chipBebasUtang, true); break;
        }
    }

    // ── Helper set style chip ────────────────────────────────────────────────
    private void setChipStyle(TextView chip, boolean active) {
        if (active) {
            chip.setBackgroundColor(COLOR_ACTIVE_BG);
            chip.setTextColor(COLOR_ACTIVE_TEXT);
        } else {
            chip.setBackgroundColor(COLOR_INACTIVE_BG);
            chip.setTextColor(COLOR_INACTIVE_TEXT);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  RecyclerView Adapter
    // ═══════════════════════════════════════════════════════════════════════
    private class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VH> {

        private final List<VideoEdukasi> list;

        VideoAdapter(List<VideoEdukasi> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_video_edukasi, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            VideoEdukasi video = list.get(position);

            h.tvEmoji.setText(video.emoji);
            h.tvJudul.setText(video.judul);
            h.tvKategori.setText(video.kategori);
            h.tvDurasi.setText(video.durasi);
            h.tvChannel.setText("📺 " + video.channel);

            // ── Warna badge kategori ─────────────────────────────────────────
            switch (video.kategori) {
                case "Investasi":
                    h.tvKategori.setTextColor(0xFF1A56DB);
                    h.tvKategori.setBackgroundColor(0xFFEEF2FF);
                    break;
                case "Tabungan":
                    h.tvKategori.setTextColor(0xFF059669);
                    h.tvKategori.setBackgroundColor(0xFFD1FAE5);
                    break;
                case "Budgeting":
                    h.tvKategori.setTextColor(0xFFD97706);
                    h.tvKategori.setBackgroundColor(0xFFFEF3C7);
                    break;
                case "Bebas Utang":
                    h.tvKategori.setTextColor(0xFFDC2626);
                    h.tvKategori.setBackgroundColor(0xFFFEE2E2);
                    break;
            }

            // ── Buka YouTube saat di-tap ─────────────────────────────────────
            h.itemView.setOnClickListener(v -> {
                String url = "https://www.youtube.com/watch?v=" + video.youtubeId;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvJudul, tvKategori, tvDurasi, tvChannel;

            VH(@NonNull View itemView) {
                super(itemView);
                tvEmoji    = itemView.findViewById(R.id.tvThumbnailEmoji);
                tvJudul    = itemView.findViewById(R.id.tvJudul);
                tvKategori = itemView.findViewById(R.id.tvKategori);
                tvDurasi   = itemView.findViewById(R.id.tvDurasi);
                tvChannel  = itemView.findViewById(R.id.tvChannel);
            }
        }
    }
}
