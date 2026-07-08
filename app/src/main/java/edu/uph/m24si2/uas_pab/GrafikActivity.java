package edu.uph.m24si2.uas_pab;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.uph.m24si2.uas_pab.adapter.TransactionAdapter;
import edu.uph.m24si2.uas_pab.db.TransactionRepository;

public class GrafikActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private TabLayout   tabPeriode;
    private BarChart    barChart;
    private LineChart   lineChart;
    private TextView    tvChartTitle;
    private TextView    tvLineChartTitle;
    private TextView    tvTotalPemasukan;
    private TextView    tvTotalPengeluaran;
    private TextView    tvSelisih;
    private LinearLayout llDetailContainer;

    // ── State ─────────────────────────────────────────────────────────────────
    private String userEmail;

    /** 0 = Harian, 1 = Mingguan, 2 = Bulanan */
    private int currentTab = 0;

    // ── Warna ─────────────────────────────────────────────────────────────────
    private static final int COLOR_PEMASUKAN   = Color.parseColor("#1A56DB");
    private static final int COLOR_PENGELUARAN = Color.parseColor("#EF4444");
    private static final int COLOR_POSITIVE    = Color.parseColor("#22C55E");
    private static final int COLOR_NEGATIVE    = Color.parseColor("#EF4444");

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);

        // Ambil email dari intent
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind views
        tabPeriode         = findViewById(R.id.tabPeriode);
        barChart           = findViewById(R.id.barChart);
        lineChart          = findViewById(R.id.lineChart);
        tvChartTitle       = findViewById(R.id.tvChartTitle);
        tvLineChartTitle   = findViewById(R.id.tvLineChartTitle);
        tvTotalPemasukan   = findViewById(R.id.tvTotalPemasukan);
        tvTotalPengeluaran = findViewById(R.id.tvTotalPengeluaran);
        tvSelisih          = findViewById(R.id.tvSelisih);
        llDetailContainer  = findViewById(R.id.llDetailContainer);

        // Setup tab
        tabPeriode.addTab(tabPeriode.newTab().setText("Harian"));
        tabPeriode.addTab(tabPeriode.newTab().setText("Mingguan"));
        tabPeriode.addTab(tabPeriode.newTab().setText("Bulanan"));

        tabPeriode.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadCharts();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Load data awal (Harian)
        loadCharts();
    }

    // ─── Load & render semua grafik ───────────────────────────────────────────

    private void loadCharts() {
        Map<String, Long> pemasukanMap;
        Map<String, Long> pengeluaranMap;
        String barTitle, lineTitle;

        switch (currentTab) {
            case 1:
                pemasukanMap   = TransactionRepository.getWeeklyData(userEmail, "PEMASUKAN");
                pengeluaranMap = TransactionRepository.getWeeklyData(userEmail, "PENGELUARAN");
                barTitle       = "Pemasukan vs Pengeluaran (8 Minggu Terakhir)";
                lineTitle      = "Tren 8 Minggu Terakhir";
                break;
            case 2:
                pemasukanMap   = TransactionRepository.getMonthlyData(userEmail, "PEMASUKAN");
                pengeluaranMap = TransactionRepository.getMonthlyData(userEmail, "PENGELUARAN");
                barTitle       = "Pemasukan vs Pengeluaran (6 Bulan Terakhir)";
                lineTitle      = "Tren 6 Bulan Terakhir";
                break;
            default: // Harian
                pemasukanMap   = TransactionRepository.getDailyData(userEmail, "PEMASUKAN");
                pengeluaranMap = TransactionRepository.getDailyData(userEmail, "PENGELUARAN");
                barTitle       = "Pemasukan vs Pengeluaran (7 Hari Terakhir)";
                lineTitle      = "Tren 7 Hari Terakhir";
                break;
        }

        tvChartTitle.setText(barTitle);
        tvLineChartTitle.setText(lineTitle);

        String[] labels = pemasukanMap.keySet().toArray(new String[0]);

        renderBarChart(pemasukanMap, pengeluaranMap, labels);
        renderLineChart(pemasukanMap, pengeluaranMap, labels);
        renderRingkasan(pemasukanMap, pengeluaranMap);
        renderDetail(pemasukanMap, pengeluaranMap, labels);
    }

    // ─── Bar Chart ────────────────────────────────────────────────────────────

    private void renderBarChart(Map<String, Long> pemasukanMap,
                                Map<String, Long> pengeluaranMap,
                                String[] labels) {
        List<BarEntry>  entriesPemasukan   = new ArrayList<>();
        List<BarEntry>  entriesPengeluaran = new ArrayList<>();
        Long[] pValues = pemasukanMap.values().toArray(new Long[0]);
        Long[] eValues = pengeluaranMap.values().toArray(new Long[0]);

        for (int i = 0; i < labels.length; i++) {
            entriesPemasukan.add(new BarEntry(i, pValues[i]));
            entriesPengeluaran.add(new BarEntry(i, eValues[i]));
        }

        BarDataSet setPemasukan = new BarDataSet(entriesPemasukan, "Pemasukan");
        setPemasukan.setColor(COLOR_PEMASUKAN);
        setPemasukan.setValueTextSize(9f);
        setPemasukan.setValueFormatter(rupiahFormatter());
        setPemasukan.setValueTextColor(Color.DKGRAY);

        BarDataSet setPengeluaran = new BarDataSet(entriesPengeluaran, "Pengeluaran");
        setPengeluaran.setColor(COLOR_PENGELUARAN);
        setPengeluaran.setValueTextSize(9f);
        setPengeluaran.setValueFormatter(rupiahFormatter());
        setPengeluaran.setValueTextColor(Color.DKGRAY);

        BarData data = new BarData(setPemasukan, setPengeluaran);
        float groupSpace  = 0.3f;
        float barSpace    = 0.05f;
        float barWidth    = 0.3f; // (0.3 + 0.05) * 2 + 0.3 = 1.0
        data.setBarWidth(barWidth);

        barChart.setData(data);
        barChart.groupBars(0f, groupSpace, barSpace);

        // X axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(labels.length);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-30f);

        // Y axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setTextSize(9f);
        leftAxis.setValueFormatter(rupiahFormatterShort());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E2E8F0"));
        barChart.getAxisRight().setEnabled(false);

        // Styling
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setExtraBottomOffset(16f);
        barChart.animateY(600);
        barChart.invalidate();
    }

    // ─── Line Chart ───────────────────────────────────────────────────────────

    private void renderLineChart(Map<String, Long> pemasukanMap,
                                 Map<String, Long> pengeluaranMap,
                                 String[] labels) {
        List<Entry> entriesPemasukan   = new ArrayList<>();
        List<Entry> entriesPengeluaran = new ArrayList<>();
        Long[] pValues = pemasukanMap.values().toArray(new Long[0]);
        Long[] eValues = pengeluaranMap.values().toArray(new Long[0]);

        for (int i = 0; i < labels.length; i++) {
            entriesPemasukan.add(new Entry(i, pValues[i]));
            entriesPengeluaran.add(new Entry(i, eValues[i]));
        }

        LineDataSet setPemasukan = new LineDataSet(entriesPemasukan, "Pemasukan");
        setPemasukan.setColor(COLOR_PEMASUKAN);
        setPemasukan.setCircleColor(COLOR_PEMASUKAN);
        setPemasukan.setLineWidth(2.5f);
        setPemasukan.setCircleRadius(4f);
        setPemasukan.setDrawValues(false);
        setPemasukan.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        setPemasukan.setDrawFilled(true);
        setPemasukan.setFillColor(Color.parseColor("#1A56DB"));
        setPemasukan.setFillAlpha(30);

        LineDataSet setPengeluaran = new LineDataSet(entriesPengeluaran, "Pengeluaran");
        setPengeluaran.setColor(COLOR_PENGELUARAN);
        setPengeluaran.setCircleColor(COLOR_PENGELUARAN);
        setPengeluaran.setLineWidth(2.5f);
        setPengeluaran.setCircleRadius(4f);
        setPengeluaran.setDrawValues(false);
        setPengeluaran.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        setPengeluaran.setDrawFilled(true);
        setPengeluaran.setFillColor(Color.parseColor("#EF4444"));
        setPengeluaran.setFillAlpha(20);

        LineData lineData = new LineData(setPemasukan, setPengeluaran);

        // X axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-30f);

        // Y axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setTextSize(9f);
        leftAxis.setValueFormatter(rupiahFormatterShort());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E2E8F0"));
        lineChart.getAxisRight().setEnabled(false);

        // Styling
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setExtraBottomOffset(16f);
        lineChart.animateY(600);
        lineChart.invalidate();
    }

    // ─── Kartu Ringkasan ──────────────────────────────────────────────────────

    private void renderRingkasan(Map<String, Long> pemasukanMap,
                                 Map<String, Long> pengeluaranMap) {
        long totalP = 0, totalE = 0;
        for (long v : pemasukanMap.values())   totalP += v;
        for (long v : pengeluaranMap.values()) totalE += v;
        long selisih = totalP - totalE;

        tvTotalPemasukan.setText(TransactionAdapter.formatRupiah(totalP));
        tvTotalPengeluaran.setText(TransactionAdapter.formatRupiah(totalE));

        String selisihText = (selisih < 0 ? "- " : "") +
                TransactionAdapter.formatRupiah(Math.abs(selisih));
        tvSelisih.setText(selisihText);
        tvSelisih.setTextColor(selisih >= 0 ? COLOR_POSITIVE : COLOR_NEGATIVE);
    }

    // ─── Kartu Detail Per Periode ─────────────────────────────────────────────

    private void renderDetail(Map<String, Long> pemasukanMap,
                              Map<String, Long> pengeluaranMap,
                              String[] labels) {
        llDetailContainer.removeAllViews();

        Long[] pValues = pemasukanMap.values().toArray(new Long[0]);
        Long[] eValues = pengeluaranMap.values().toArray(new Long[0]);

        // Header row
        llDetailContainer.addView(buildDetailHeader());

        // Divider
        llDetailContainer.addView(buildDivider());

        // Data rows — tampilkan dari terbaru ke terlama (reverse)
        for (int i = labels.length - 1; i >= 0; i--) {
            long p = pValues[i];
            long e = eValues[i];
            long s = p - e;
            llDetailContainer.addView(buildDetailRow(labels[i], p, e, s));
            if (i > 0) llDetailContainer.addView(buildDivider());
        }
    }

    private View buildDetailHeader() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, dp(8));

        row.addView(buildCell("Periode",    1.2f, Color.parseColor("#64748B"), true));
        row.addView(buildCell("Masuk",      1f,   Color.parseColor("#64748B"), true));
        row.addView(buildCell("Keluar",     1f,   Color.parseColor("#64748B"), true));
        row.addView(buildCell("Selisih",    1f,   Color.parseColor("#64748B"), true));
        return row;
    }

    private View buildDetailRow(String label, long pemasukan, long pengeluaran, long selisih) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(6));

        int selisihColor = selisih >= 0 ? COLOR_POSITIVE : COLOR_NEGATIVE;
        String selisihText = (selisih < 0 ? "- " : "+ ") +
                formatShort(Math.abs(selisih));

        row.addView(buildCell(label,                    1.2f, Color.parseColor("#1E293B"), false));
        row.addView(buildCell(formatShort(pemasukan),   1f,   COLOR_PEMASUKAN,             false));
        row.addView(buildCell(formatShort(pengeluaran), 1f,   COLOR_PENGELUARAN,           false));
        row.addView(buildCell(selisihText,              1f,   selisihColor,                false));
        return row;
    }

    private TextView buildCell(String text, float weight, int color, boolean bold) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
        tv.setLayoutParams(lp);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(11f);
        tv.setGravity(Gravity.CENTER);
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        return tv;
    }

    private View buildDivider() {
        View v = new View(this);
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(0, dp(2), 0, dp(2));
        v.setLayoutParams(lp);
        v.setBackgroundColor(Color.parseColor("#E2E8F0"));
        return v;
    }

    // ─── Formatter helpers ────────────────────────────────────────────────────

    /**
     * Formatter label nilai grafik dalam format singkat (juta/ribu).
     */
    private ValueFormatter rupiahFormatterShort() {
        return new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatShort((long) value);
            }
        };
    }

    /**
     * Formatter nilai di atas batang grafik, tampilkan "0" jika nol.
     */
    private ValueFormatter rupiahFormatter() {
        return new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0f) return "";
                return formatShort((long) value);
            }
        };
    }

    /** Format angka menjadi singkatan: 1.5jt, 500rb, dst. */
    private String formatShort(long value) {
        if (value == 0) return "0";
        if (value >= 1_000_000_000L)
            return String.format(Locale.getDefault(), "%.1fM", value / 1_000_000_000f);
        if (value >= 1_000_000L)
            return String.format(Locale.getDefault(), "%.1fjt", value / 1_000_000f);
        if (value >= 1_000L)
            return String.format(Locale.getDefault(), "%.0frb", value / 1_000f);
        return String.valueOf(value);
    }

    /** Konversi dp → px. */
    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
