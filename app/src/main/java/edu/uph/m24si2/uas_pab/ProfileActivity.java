package edu.uph.m24si2.uas_pab;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;

import edu.uph.m24si2.uas_pab.db.UserRepository;
import edu.uph.m24si2.uas_pab.model.User;

public class ProfileActivity extends AppCompatActivity {

    private String userEmail;

    private ImageView ivProfilePhoto;
    private EditText  etNama, etUsername, etEmail, etPhone, etBirthDate, etBirthYear;
    private MaterialButton btnSimpan, btnLogout, btnGantiFoto;

    private String selectedPhotoUri = null; // URI dari galeri yang dipilih

    // Launcher untuk memilih foto dari galeri
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // Ambil persistable permission agar URI tetap valid setelah restart
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {
                        // Tidak semua URI mendukung persistable permission — abaikan
                    }
                    selectedPhotoUri = uri.toString();
                    ivProfilePhoto.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userEmail = getIntent().getStringExtra("USER_EMAIL");

        // Bind views
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        etNama         = findViewById(R.id.etNama);
        etUsername     = findViewById(R.id.etUsername);
        etEmail        = findViewById(R.id.etEmail);
        etPhone        = findViewById(R.id.etPhone);
        etBirthDate    = findViewById(R.id.etBirthDate);
        etBirthYear    = findViewById(R.id.etBirthYear);
        btnSimpan      = findViewById(R.id.btnSimpan);
        btnLogout      = findViewById(R.id.btnLogout);
        btnGantiFoto   = findViewById(R.id.btnGantiFoto);

        // Tombol kembali
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Tanggal lahir → DatePicker
        etBirthDate.setOnClickListener(v -> showDatePicker());

        // Ganti foto dari galeri
        btnGantiFoto.setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));
        ivProfilePhoto.setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));

        // Simpan profil
        btnSimpan.setOnClickListener(v -> saveProfile());

        // Logout
        btnLogout.setOnClickListener(v -> confirmLogout());

        // Isi data dari database
        loadUserData();
    }

    // ─── Load data ────────────────────────────────────────────────────────────

    private void loadUserData() {
        if (userEmail == null) return;
        User user = UserRepository.findByEmail(userEmail);
        if (user == null) return;

        etNama.setText(user.getNama() != null ? user.getNama() : "");
        etUsername.setText(user.getUsername() != null ? user.getUsername() : "");
        etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        etPhone.setText(user.getPhone() != null ? user.getPhone() : "");
        etBirthDate.setText(user.getBirthDate() != null ? user.getBirthDate() : "");
        etBirthYear.setText(user.getBirthYear() > 0
                ? String.valueOf(user.getBirthYear()) : "");

        // Tampilkan foto profil jika ada URI tersimpan
        if (user.getPhotoUri() != null && !user.getPhotoUri().isEmpty()) {
            try {
                Uri uri = Uri.parse(user.getPhotoUri());
                ivProfilePhoto.setImageURI(uri);
                selectedPhotoUri = user.getPhotoUri();
            } catch (Exception e) {
                // URI tidak valid — biarkan foto default
            }
        }
    }

    // ─── Simpan Profil ────────────────────────────────────────────────────────

    private void saveProfile() {
        String nama      = etNama.getText().toString().trim();
        String username  = etUsername.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String yearStr   = etBirthYear.getText().toString().trim();

        // Validasi wajib
        if (nama.isEmpty()) {
            etNama.setError("Nama tidak boleh kosong");
            etNama.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Nomor HP tidak boleh kosong");
            etPhone.requestFocus();
            return;
        }

        // Parse tahun lahir
        int birthYear = 0;
        if (!yearStr.isEmpty()) {
            try {
                birthYear = Integer.parseInt(yearStr);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                if (birthYear < 1900 || birthYear > currentYear) {
                    etBirthYear.setError("Tahun lahir tidak valid");
                    etBirthYear.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etBirthYear.setError("Format tahun tidak valid");
                etBirthYear.requestFocus();
                return;
            }
        }

        // Simpan ke Realm
        UserRepository.updateProfile(
                userEmail, nama, username, phone,
                birthDate, birthYear, selectedPhotoUri
        );

        // Konfirmasi Snackbar
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, "Profil berhasil disimpan", Snackbar.LENGTH_SHORT).show();
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Keluar")
                .setMessage("Yakin ingin keluar dari akun ini?")
                .setPositiveButton("Keluar", (dialog, which) -> {
                    // Kembali ke LoginActivity dan bersihkan back stack
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ─── Date Picker ─────────────────────────────────────────────────────────

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        // Coba parse tanggal yang sudah ada
        String existing = etBirthDate.getText().toString().trim();
        if (!existing.isEmpty()) {
            try {
                String[] parts = existing.split("/");
                if (parts.length == 3) {
                    cal.set(Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[1]) - 1,
                            Integer.parseInt(parts[0]));
                }
            } catch (Exception ignored) {}
        }

        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format("%02d/%02d/%04d", day, month + 1, year);
            etBirthDate.setText(date);
            // Auto-isi tahun lahir
            etBirthYear.setText(String.valueOf(year));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
}
