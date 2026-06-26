package edu.uph.m24si2.uas_pab;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.uph.m24si2.uas_pab.db.UserRepository;

public class RegisterActivity extends AppCompatActivity {

    EditText etNama, etEmail, etPhone, etPassword, etKonfirmPassword;
    Button btnDaftar;
    TextView tvMasuk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Menghubungkan Activity dengan activity_register.xml
        setContentView(R.layout.activity_register);

        // Inisialisasi komponen
        etNama           = findViewById(R.id.etNama);
        etEmail          = findViewById(R.id.etEmail);
        etPhone          = findViewById(R.id.etPhone);
        etPassword       = findViewById(R.id.etPassword);
        etKonfirmPassword = findViewById(R.id.etKonfirmPassword);

        btnDaftar = findViewById(R.id.btnDaftar);
        tvMasuk   = findViewById(R.id.tvMasuk);

        // Event tombol Daftar Sekarang
        btnDaftar.setOnClickListener(v -> {

            String nama     = etNama.getText().toString().trim();
            String email    = etEmail.getText().toString().trim();
            String phone    = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String konfirm  = etKonfirmPassword.getText().toString().trim();

            // Validasi Nama
            if (nama.isEmpty()) {
                etNama.setError("Nama lengkap harus diisi");
                etNama.requestFocus();
                return;
            }

            // Validasi Email
            if (email.isEmpty()) {
                etEmail.setError("Email harus diisi");
                etEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Format email tidak valid");
                etEmail.requestFocus();
                return;
            }

            // Validasi Nomor HP
            if (phone.isEmpty()) {
                etPhone.setError("Nomor HP harus diisi");
                etPhone.requestFocus();
                return;
            }

            // Validasi Password
            if (password.isEmpty()) {
                etPassword.setError("Password harus diisi");
                etPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("Password minimal 6 karakter");
                etPassword.requestFocus();
                return;
            }

            // Validasi Konfirmasi Password
            if (konfirm.isEmpty()) {
                etKonfirmPassword.setError("Konfirmasi password harus diisi");
                etKonfirmPassword.requestFocus();
                return;
            }

            if (!password.equals(konfirm)) {
                etKonfirmPassword.setError("Password tidak cocok");
                etKonfirmPassword.requestFocus();
                return;
            }

            // Simpan ke Realm database
            String error = UserRepository.registerUser(nama, email, phone, password);

            if (error != null) {
                // Registrasi gagal (misal email sudah terdaftar)
                Log.e("USER_REGISTER", "Registrasi GAGAL: " + error);
                etEmail.setError(error);
                etEmail.requestFocus();
                return;
            }

            // Registrasi berhasil — verifikasi data tersimpan
            Log.d("USER_REGISTER", "=== REGISTRASI BERHASIL ===");
            Log.d("USER_REGISTER", "Nama  : " + nama);
            Log.d("USER_REGISTER", "Email : " + email);
            Log.d("USER_REGISTER", "Phone : " + phone);

            // Cek total user setelah register
            java.util.List<edu.uph.m24si2.uas_pab.model.User> allUsers = edu.uph.m24si2.uas_pab.db.UserRepository.getAllUsers();
            Log.d("USER_REGISTER", "Total user di DB sekarang: " + allUsers.size());

            // Registrasi berhasil
            Toast.makeText(
                    RegisterActivity.this,
                    "Registrasi Berhasil! Silakan Login.",
                    Toast.LENGTH_LONG
            ).show();

            // Kembali ke halaman Login
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        });

        // Event link "Masuk di sini" → kembali ke Login
        tvMasuk.setOnClickListener(v -> {
            finish(); // Kembali ke halaman sebelumnya (LoginActivity)
        });

    }
}
