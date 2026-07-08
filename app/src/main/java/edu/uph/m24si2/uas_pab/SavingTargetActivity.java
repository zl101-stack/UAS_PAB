package edu.uph.m24si2.uas_pab;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import edu.uph.m24si2.uas_pab.adapter.TransactionAdapter;
import edu.uph.m24si2.uas_pab.db.SavingRepository;
import edu.uph.m24si2.uas_pab.model.SavingTarget;

public class SavingTargetActivity extends AppCompatActivity {

    private String userEmail;
    private LinearLayout containerTargets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_target);

        userEmail        = getIntent().getStringExtra("USER_EMAIL");
        containerTargets = findViewById(R.id.containerTargets);

        findViewById(R.id.btnTambahTarget).setOnClickListener(v -> showDialogTambahTarget());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTargets();
    }

    private void loadTargets() {
        containerTargets.removeAllViews();
        List<SavingTarget> targets = SavingRepository.getAllTargets(userEmail);

        if (targets.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Belum ada target tabungan.\nTambahkan target baru!");
            tvEmpty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvEmpty.setPadding(0, 64, 0, 0);
            tvEmpty.setTextSize(14);
            tvEmpty.setTextColor(0xFF888888);
            containerTargets.addView(tvEmpty);
            return;
        }

        for (SavingTarget target : targets) {
            addTargetCard(target);
        }
    }

    private void addTargetCard(SavingTarget target) {
        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_saving_target, containerTargets, false);

        TextView    tvNama      = card.findViewById(R.id.tvNamaTarget);
        TextView    tvSaved     = card.findViewById(R.id.tvSaved);
        TextView    tvTarget    = card.findViewById(R.id.tvTarget);
        TextView    tvPercent   = card.findViewById(R.id.tvPercent);
        ProgressBar progressBar = card.findViewById(R.id.progressBar);

        tvNama.setText(target.getNamaTarget());
        tvSaved.setText("Terkumpul: " + TransactionAdapter.formatRupiah(target.getSavedAmount()));
        tvTarget.setText("Target: " + TransactionAdapter.formatRupiah(target.getTargetAmount()));
        tvPercent.setText(target.getProgressPercent() + "%");
        progressBar.setProgress(target.getProgressPercent());

        card.findViewById(R.id.btnTambahDana).setOnClickListener(v ->
                showDialogTambahDana(target.getId()));

        card.findViewById(R.id.btnHapus).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Hapus Target")
                        .setMessage("Hapus target \"" + target.getNamaTarget() + "\"?")
                        .setPositiveButton("Hapus", (d, w) -> {
                            SavingRepository.deleteTarget(target.getId());
                            loadTargets();
                        })
                        .setNegativeButton("Batal", null)
                        .show());

        containerTargets.addView(card);
    }

    private void showDialogTambahTarget() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_tambah_target, null);

        EditText etNama   = dialogView.findViewById(R.id.etNamaTarget);
        EditText etJumlah = dialogView.findViewById(R.id.etJumlahTarget);

        new AlertDialog.Builder(this)
                .setTitle("Tambah Target Tabungan")
                .setView(dialogView)
                .setPositiveButton("Simpan", (d, w) -> {
                    String nama   = etNama.getText().toString().trim();
                    String jumlah = etJumlah.getText().toString().trim();
                    if (nama.isEmpty()) {
                        Toast.makeText(this, "Nama target harus diisi", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (jumlah.isEmpty()) {
                        Toast.makeText(this, "Jumlah target harus diisi", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long amount = Long.parseLong(jumlah);
                    if (amount <= 0) {
                        Toast.makeText(this, "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SavingRepository.addTarget(userEmail, nama, amount);
                    Toast.makeText(this, "Target berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                    loadTargets();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showDialogTambahDana(String targetId) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_tambah_dana, null);

        EditText etJumlah = dialogView.findViewById(R.id.etJumlahDana);

        new AlertDialog.Builder(this)
                .setTitle("Tambah Dana Tabungan")
                .setView(dialogView)
                .setPositiveButton("Simpan", (d, w) -> {
                    String jumlah = etJumlah.getText().toString().trim();
                    if (jumlah.isEmpty()) {
                        Toast.makeText(this, "Jumlah harus diisi", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long amount = Long.parseLong(jumlah);
                    if (amount <= 0) {
                        Toast.makeText(this, "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SavingRepository.addSaving(targetId, amount);
                    Toast.makeText(this, "Dana berhasil ditambahkan!", Toast.LENGTH_SHORT).show();
                    loadTargets();
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}
