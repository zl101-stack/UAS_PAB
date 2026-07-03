package edu.uph.m24si2.uas_pab;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.uph.m24si2.uas_pab.adapter.TransactionAdapter;
import edu.uph.m24si2.uas_pab.db.TransactionRepository;
import edu.uph.m24si2.uas_pab.model.Transaction;

public class PengeluaranActivity extends AppCompatActivity {

    private static final String TYPE = "PENGELUARAN";

    // Kategori Pengeluaran: [label, iconKey]
    private static final String[][] CATEGORIES = {
            {"Makan & Minum",   "makan"},
            {"Transportasi",    "transport"},
            {"Belanja",         "belanja"},
            {"Tagihan",         "tagihan"},
            {"Kesehatan",       "kesehatan"},
            {"Hiburan",         "hiburan"},
            {"Pendidikan",      "pendidikan"},
            {"Lainnya",         "lainnya_out"},
    };

    private String userEmail;
    private RecyclerView rvTransaksi;
    private View layoutEmpty;
    private TextView tvTotal;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();

    private String editingId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengeluaran);

        userEmail = getIntent().getStringExtra("USER_EMAIL");

        tvTotal     = findViewById(R.id.tvTotalPengeluaran);
        rvTransaksi = findViewById(R.id.rvTransaksi);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        FloatingActionButton fab = findViewById(R.id.fabTambah);
        fab.setOnClickListener(v -> showTransactionDialog(null));

        View btnAdd = findViewById(R.id.btnAdd);
        if (btnAdd != null) btnAdd.setOnClickListener(v -> showTransactionDialog(null));

        rvTransaksi.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this, transactionList,
                new TransactionAdapter.OnItemActionListener() {
                    @Override public void onEdit(Transaction transaction) {
                        showTransactionDialog(transaction);
                    }
                    @Override public void onDelete(Transaction transaction) {
                        showDeleteConfirm(transaction);
                    }
                }, true); // true = PENGELUARAN (merah)
        rvTransaksi.setAdapter(adapter);

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    // ─── Load & Refresh ───────────────────────────────────────────────────────

    private void loadData() {
        transactionList.clear();
        transactionList.addAll(TransactionRepository.getTransactions(userEmail, TYPE));
        adapter.notifyDataSetChanged();
        updateTotal();
        updateEmptyState();
    }

    private void updateTotal() {
        long total = TransactionRepository.getTotalAmount(userEmail, TYPE);
        tvTotal.setText(TransactionAdapter.formatRupiah(total));
    }

    private void updateEmptyState() {
        if (transactionList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvTransaksi.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvTransaksi.setVisibility(View.VISIBLE);
        }
    }

    // ─── Dialog Tambah / Edit ─────────────────────────────────────────────────

    private void showTransactionDialog(Transaction existing) {
        editingId = existing != null ? existing.getId() : null;

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_transaction, null);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        tvDialogTitle.setText(editingId == null ? "Tambah Pengeluaran" : "Edit Pengeluaran");
        tvDialogTitle.setTextColor(getColor(R.color.expense_red));

        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        etAmount.addTextChangedListener(new RupiahTextWatcher(etAmount));

        AutoCompleteTextView spinnerKategori = dialogView.findViewById(R.id.spinnerKategori);
        String[] categoryLabels = new String[CATEGORIES.length];
        for (int i = 0; i < CATEGORIES.length; i++) categoryLabels[i] = CATEGORIES[i][0];
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categoryLabels);
        spinnerKategori.setAdapter(catAdapter);
        spinnerKategori.setThreshold(0);
        spinnerKategori.setOnClickListener(v -> spinnerKategori.showDropDown());

        EditText etDate = dialogView.findViewById(R.id.etDate);
        etDate.setOnClickListener(v -> showDatePicker(etDate));
        etDate.setFocusable(false);

        EditText etNotes = dialogView.findViewById(R.id.etNotes);

        if (existing != null) {
            etAmount.setText(String.valueOf(existing.getAmount()));
            spinnerKategori.setText(existing.getCategory(), false);
            etDate.setText(existing.getDate());
            etNotes.setText(existing.getNotes());
        } else {
            etDate.setText(todayString());
        }

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialog, which) ->
                        saveTransaction(etAmount, spinnerKategori, etDate, etNotes))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void saveTransaction(EditText etAmount, AutoCompleteTextView spinnerKategori,
                                  EditText etDate, EditText etNotes) {
        String rawAmount     = etAmount.getText().toString()
                .replace("Rp ", "").replace(".", "").trim();
        String categoryLabel = spinnerKategori.getText().toString().trim();
        String date          = etDate.getText().toString().trim();
        String notes         = etNotes.getText().toString().trim();

        if (rawAmount.isEmpty()) {
            Toast.makeText(this, "Jumlah harus diisi", Toast.LENGTH_SHORT).show(); return;
        }
        if (categoryLabel.isEmpty()) {
            Toast.makeText(this, "Kategori harus dipilih", Toast.LENGTH_SHORT).show(); return;
        }
        if (date.isEmpty()) {
            Toast.makeText(this, "Tanggal harus dipilih", Toast.LENGTH_SHORT).show(); return;
        }

        long amount;
        try {
            amount = Long.parseLong(rawAmount);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show(); return;
        }

        String categoryIcon = "lainnya_out";
        for (String[] cat : CATEGORIES) {
            if (cat[0].equals(categoryLabel)) { categoryIcon = cat[1]; break; }
        }

        if (editingId == null) {
            TransactionRepository.addTransaction(userEmail, TYPE, amount,
                    categoryLabel, categoryIcon, date, notes);
        } else {
            TransactionRepository.updateTransaction(editingId, amount,
                    categoryLabel, categoryIcon, date, notes);
        }

        loadData();

        View rootView = findViewById(android.R.id.content);
        String msg = editingId == null
                ? "Pengeluaran berhasil disimpan"
                : "Pengeluaran berhasil diperbarui";
        Snackbar.make(rootView, msg, Snackbar.LENGTH_SHORT).show();
        editingId = null;
    }

    // ─── Hapus ────────────────────────────────────────────────────────────────

    private void showDeleteConfirm(Transaction t) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Pengeluaran")
                .setMessage("Yakin ingin menghapus transaksi ini?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    TransactionRepository.deleteTransaction(t.getId());
                    loadData();
                    Snackbar.make(findViewById(android.R.id.content),
                            "Transaksi dihapus", Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ─── Date Picker ─────────────────────────────────────────────────────────

    private void showDatePicker(EditText target) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format("%02d/%02d/%04d", day, month + 1, year);
            target.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String todayString() {
        Calendar c = Calendar.getInstance();
        return String.format("%02d/%02d/%04d",
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.YEAR));
    }

    // ─── Rupiah TextWatcher ───────────────────────────────────────────────────

    private static class RupiahTextWatcher implements TextWatcher {
        private final EditText editText;
        private boolean isFormatting = false;

        RupiahTextWatcher(EditText et) { this.editText = et; }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (isFormatting) return;
            isFormatting = true;
            String digits = s.toString().replace("Rp ", "").replace(".", "").trim();
            if (!digits.isEmpty()) {
                try {
                    long value = Long.parseLong(digits);
                    String formatted = TransactionAdapter.formatRupiah(value);
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                } catch (NumberFormatException ignored) {}
            }
            isFormatting = false;
        }
    }
}
