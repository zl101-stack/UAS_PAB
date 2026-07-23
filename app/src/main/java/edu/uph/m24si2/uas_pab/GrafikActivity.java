package edu.uph.m24si2.uas_pab;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.uph.m24si2.uas_pab.adapter.TransactionAdapter;
import edu.uph.m24si2.uas_pab.db.TransactionRepository;

public class GrafikActivity extends AppCompatActivity {

    private TabLayout   tabPeriode;
    private TabLayout   tabPieType;
    private BarChart    barChart;
    private LineChart   lineChart;
    private PieChart    pieChart;
    private TextView    tvChartTitle;
    private TextView    tvLineChartTitle;
    private TextView    tvTotalPemasukan;
    private TextView    tvTotalPengeluaran;
    private TextView    tvSelisih;
    private LinearLayout llDetailContainer;
    private LinearLayout llPieLegend;

    private String userEmail;
    private int currentTab    = 0; // 0=Harian, 1=Mingguan, 2=Bulanan
    private int currentPieTab = 0; // 0=Pemasukan, 1=Pengeluaran

    private static final int COLOR_PEMASUKAN   = Color.parseColor("#1A56DB");
    private static final int COLOR_PENGELUARAN = Color.parseColor("#EF4444");
    private static final int COLOR_POSITIVE    = Color.parseColor("#22C55E");
    private static final int COLOR_NEGATIVE    = Color.parseColor("#EF4444");

    // Warna untuk pie chart pemasukan (biru)
    private static final int[] COLORS_PEMASUKAN = {
        Color.parseColor("#1A56DB"), Color.parseColor("#3B82F6"),
        Color.parseColor("#60A5FA"), Color.parseColor("#93C5FD"),
        Color.parseColor("#0EA5E9"), Color.parseColor("#0284C7"),
        Color.parseColor("#2563EB"), Color.parseColor("#1D4ED8"),
    };

    // Warna untuk pie chart pengeluaran (merah)
    private static final int[] COLORS_PENGELUARAN = {
        Color.parseColor("#EF4444"), Color.parseColor("#F87171"),
        Color.parseColor("#FCA5A5"), Color.parseColor("#DC2626"),
        Color.parseColor("#B91C1C"), Color.parseColor("#F97316"),
        Color.parseColor("#FB923C"), Color.parseColor("#FBBF24"),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);

        userEmail = getIntent().getStringExtra("USER_EMAIL");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind views
        tabPeriode         = findViewById(R.id.tabPeriode);
        tabPieType         = findViewById(R.id.tabPieType);
        barChart           = findViewById(R.id.barChart);
        lineChart          = findViewById(R.id.lineChart);
        pieChart           = findViewById(R.id.pieChart);
        tvChartTitle       = findViewById(R.id.tvChartTitle);
        tvLineChartTitle   = findViewById(R.id.tvLineChartTitle);
        tvTotalPemasukan   = findViewById(R.id.tvTotalPemasukan);
        tvTotalPengeluaran = findViewById(R.id.tvTotalPengeluaran);
        tvSelisih          = findViewById(R.id.tvSelisih);
        llDetailContainer  = findViewById(R.id.llDetailContainer);
        llPieLegend        = findViewById(R.id.llPieLegend);

        // Tab periode (Harian/Mingguan/Bulanan)
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

        // Tab pie (Pemasukan/Pengeluaran)
        tabPieType.addTab(tabPieType.newTab().setText("Pemasukan"));
        tabPieType.addTab(tabPieType.newTab().setText("Pengeluaran"));
        tabPieType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentPieTab = tab.getPosition();
                loadPieChart();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadCharts();
    }

    private void loadCharts() {
        Map<String, Long> pemasukanMap;
        Map<String, Long> pengeluaranMap;
        String barTitle, lineTitle;

        switch (currentTab) {
            case 1:
                pemasukanMap   = TransactionRepository.getWeeklyData(userEmail, "PEMASUKAN");
                pengeluaranMap = TransactionRepository.getWeeklyData(userEmail, "PENGELUARAN");
                barTitle  = "Pemasukan vs Pengeluaran (8 Minggu Terakhir)";
                lineTitle = "Tren 8 Minggu Terakhir";
                break;
            case 2:
                pemasukanMap   = TransactionRepository.getMonthlyData(userEmail, "PEMASUKAN");
                pengeluaranMap = TransactionRepository.getMonthlyData(userEmail, "PENGELUARAN");
                barTitle  = "Pemasukan vs Pengeluaran (6 Bulan Terakhir)";
                lineTitle = "Tren 6 Bulan Terakhir";
                break;
            default:
                pemasukanMap   = TransactionRepository.getDailyData(userEmail, "PEMASUKAN");
                pengeluaranMap = TransactionRepository.getDailyData(userEmail, "PENGELUARAN");
                barTitle  = "Pemasukan vs Pengeluaran (7 Hari Terakhir)";
                lineTitle = "Tren 7 Hari Terakhir";
                break;
        }

        tvChartTitle.setText(barTitle);
        tvLineChartTitle.setText(lineTitle);

        String[] labels = pemasukanMap.keySet().toArray(new String[0]);
        renderBarChart(pemasukanMap, pengeluaranMap, labels);
        renderLineChart(pemasukanMap, pengeluaranMap, labels);
        renderRingkasan(pemasukanMap, pengeluaranMap);
        renderDetail(pemasukanMap, pengeluaranMap, labels);
        loadPieChart();
    }

    // ─── Pie Chart ────────────────────────────────────────────────────────────

    private void loadPieChart() {
        String type = currentPieTab == 0 ? "PEMASUKAN" : "PENGELUARAN";
        Map<String, Long> categoryMap = TransactionRepository.getCategoryData(userEmail, type);

        if (categoryMap.isEmpty()) {
            pieChart.setNoDataText("Belum ada data " + (currentPieTab == 0 ? "pemasukan" : "pengeluaran"));
            pieChart.setNoDataTextColor(Color.GRAY);
            pieChart.invalidate();
            llPieLegend.removeAllViews();
            return;
        }

        // Hitung total untuk persentase
        long total = 0;
        for (long v : categoryMap.values()) total += v;

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
            float pct = (entry.getValue() * 100f) / total;
            entries.add(new PieEntry(pct, entry.getKey()));
        }

        int[] colors = currentPieTab == 0 ? COLORS_PEMASUKAN : COLORS_PENGELUARAN;

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(8f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setCenterText(currentPieTab == 0 ? "Pemasukan" : "Pengeluaran");
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(currentPieTab == 0 ? COLOR_PEMASUKAN : COLOR_PENGELUARAN);
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.getLegend().setEnabled(false); // pakai legend custom di bawah
        pieChart.animateY(800);
        pieChart.invalidate();

        // Buat legend custom
        llPieLegend.removeAllViews();
        int i = 0;
        for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
            float pct = (entry.getValue() * 100f) / total;
            int color = colors[i % colors.length];
            llPieLegend.addView(buildLegendRow(entry.getKey(), entry.getValue(), pct, color));
            i++;
        }
    }

    private View buildLegendRow(String kategori, long amount, float pct, int color) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(6), 0, dp(6));

        // Kotak warna
        View dot = new View(this);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(14), dp(14));
        dotLp.setMargins(0, 0, dp(10), 0);
        dot.setLayoutParams(dotLp);
        dot.setBackgroundColor(color);
        row.addView(dot);

        // Nama kategori
        TextView tvKat = new TextView(this);
        LinearLayout.LayoutParams katLp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvKat.setLayoutParams(katLp);
        tvKat.setText(kategori);
        tvKat.setTextColor(Color.parseColor("#1E293B"));
        tvKat.setTextSize(13f);
        row.addView(tvKat);

        // Persentase
        TextView tvPct = new TextView(this);
        tvPct.setText(String.format(Locale.getDefault(), "%.1f%%", pct));
        tvPct.setTextColor(color);
        tvPct.setTextSize(13f);
        tvPct.setTypeface(null, Typeface.BOLD);
        tvPct.setPadding(0, 0, dp(12), 0);
        row.addView(tvPct);

        // Nominal
        TextView tvAmt = new TextView(this);
        tvAmt.setText(TransactionAdapter.formatRupiah(amount));
        tvAmt.setTextColor(Color.parseColor("#64748B"));
        tvAmt.setTextSize(12f);
        row.addView(tvAmt);

        return row;
    }

    // ─── Bar Chart ────────────────────────────────────────────────────────────

    private void renderBarChart(Map<String, Long> pemasukanMap,
                                Map<String, Long> pengeluaranMap,
                                String[] labels) {
        List<BarEntry> entriesP = new ArrayList<>();
        List<BarEntry> entriesE = new ArrayList<>();
        Long[] pValues = pemasukanMap.values().toArray(new Long[0]);
        Long[] eValues = pengeluaranMap.values().toArray(new Long[0]);

        for (int i = 0; i < labels.length; i++) {
            entriesP.add(new BarEntry(i, pValues[i]));
            entriesE.add(new BarEntry(i, eValues[i]));
        }

        BarDataSet setP = new BarDataSet(entriesP, "Pemasukan");
        setP.setColor(COLOR_PEMASUKAN);
        setP.setValueTextSize(9f);
        setP.setValueFormatter(rupiahFormatter());
        setP.setValueTextColor(Color.DKGRAY);

        BarDataSet setE = new BarDataSet(entriesE, "Pengeluaran");
        setE.setColor(COLOR_PENGELUARAN);
        setE.setValueTextSize(9f);
        setE.setValueFormatter(rupiahFormatter());
        setE.setValueTextColor(Color.DKGRAY);

        BarData data = new BarData(setP, setE);
        data.setBarWidth(0.3f);
        barChart.setData(data);
        barChart.groupBars(0f, 0.3f, 0.05f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(labels.length);
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-30f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(rupiahFormatterShort());
        leftAxis.setGridColor(Color.parseColor("#E2E8F0"));
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setExtraBottomOffset(16f);
        barChart.animateY(600);
        barChart.invalidate();
    }

    // ─── Line Chart ───────────────────────────────────────────────────────────

    private void renderLineChart(Map<String, Long> pemasukanMap,
                                 Map<String, Long> pengeluaranMap,
                                 String[] labels) {
        List<Entry> entriesP = new ArrayList<>();
        List<Entry> entriesE = new ArrayList<>();
        Long[] pValues = pemasukanMap.values().toArray(new Long[0]);
        Long[] eValues = pengeluaranMap.values().toArray(new Long[0]);

        for (int i = 0; i < labels.length; i++) {
            entriesP.add(new Entry(i, pValues[i]));
            entriesE.add(new Entry(i, eValues[i]));
        }

        LineDataSet setP = new LineDataSet(entriesP, "Pemasukan");
        setP.setColor(COLOR_PEMASUKAN);
        setP.setCircleColor(COLOR_PEMASUKAN);
        setP.setLineWidth(2.5f);
        setP.setCircleRadius(4f);
        setP.setDrawValues(false);
        setP.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        setP.setDrawFilled(true);
        setP.setFillColor(COLOR_PEMASUKAN);
        setP.setFillAlpha(30);

        LineDataSet setE = new LineDataSet(entriesE, "Pengeluaran");
        setE.setColor(COLOR_PENGELUARAN);
        setE.setCircleColor(COLOR_PENGELUARAN);
        setE.setLineWidth(2.5f);
        setE.setCircleRadius(4f);
        setE.setDrawValues(false);
        setE.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        setE.setDrawFilled(true);
        setE.setFillColor(COLOR_PENGELUARAN);
        setE.setFillAlpha(20);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-30f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(rupiahFormatterShort());
        leftAxis.setGridColor(Color.parseColor("#E2E8F0"));
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setData(new LineData(setP, setE));
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setExtraBottomOffset(16f);
        lineChart.animateY(600);
        lineChart.invalidate();
    }

    // ─── Ringkasan ────────────────────────────────────────────────────────────

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

    // ─── Detail Per Periode ───────────────────────────────────────────────────

    private void renderDetail(Map<String, Long> pemasukanMap,
                              Map<String, Long> pengeluaranMap,
                              String[] labels) {
        llDetailContainer.removeAllViews();
        Long[] pValues = pemasukanMap.values().toArray(new Long[0]);
        Long[] eValues = pengeluaranMap.values().toArray(new Long[0]);

        llDetailContainer.addView(buildDetailHeader());
        llDetailContainer.addView(buildDivider());

        for (int i = labels.length - 1; i >= 0; i--) {
            long p = pValues[i], e = eValues[i], s = p - e;
            llDetailContainer.addView(buildDetailRow(labels[i], p, e, s));
            if (i > 0) llDetailContainer.addView(buildDivider());
        }
    }

    private View buildDetailHeader() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, dp(8));
        row.addView(buildCell("Periode",  1.2f, Color.parseColor("#64748B"), true));
        row.addView(buildCell("Masuk",    1f,   Color.parseColor("#64748B"), true));
        row.addView(buildCell("Keluar",   1f,   Color.parseColor("#64748B"), true));
        row.addView(buildCell("Selisih",  1f,   Color.parseColor("#64748B"), true));
        return row;
    }

    private View buildDetailRow(String label, long p, long e, long s) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(6), 0, dp(6));
        int sColor = s >= 0 ? COLOR_POSITIVE : COLOR_NEGATIVE;
        String sText = (s < 0 ? "- " : "+ ") + formatShort(Math.abs(s));
        row.addView(buildCell(label,            1.2f, Color.parseColor("#1E293B"), false));
        row.addView(buildCell(formatShort(p),   1f,   COLOR_PEMASUKAN,             false));
        row.addView(buildCell(formatShort(e),   1f,   COLOR_PENGELUARAN,           false));
        row.addView(buildCell(sText,            1f,   sColor,                      false));
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
        if (bold) tv.setTypeface(null, Typeface.BOLD);
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

    // ─── Formatters ──────────────────────────────────────────────────────────

    private ValueFormatter rupiahFormatterShort() {
        return new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                return formatShort((long) value);
            }
        };
    }

    private ValueFormatter rupiahFormatter() {
        return new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                if (value == 0f) return "";
                return formatShort((long) value);
            }
        };
    }

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

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
