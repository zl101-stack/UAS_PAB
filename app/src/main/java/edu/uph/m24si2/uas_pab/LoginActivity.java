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
import edu.uph.m24si2.uas_pab.model.User;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnRegister;
    TextView tvForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Menghubungkan Activity dengan activity_login.xml
        setContentView(R.layout.activity_login);

        // Log semua user yang terdaftar di database (untuk debug)
        logSemuaUser();

        // Inisialisasi komponen
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        tvForgot = findViewById(R.id.tvForgot);


        // Event tombol Login
        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validasi input
            if (email.isEmpty()) {
                etEmail.setError("Email harus diisi");
                etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Password harus diisi");
                etPassword.requestFocus();
                return;
            }

            // Cek email & password ke Realm database
            User user = UserRepository.loginUser(email, password);

            if (user == null) {
                // Login gagal — email atau password salah
                Toast.makeText(
                        LoginActivity.this,
                        "Email atau password salah",
                        Toast.LENGTH_SHORT
                ).show();
                etPassword.setError("Email atau password salah");
                etPassword.requestFocus();
                return;
            }

            // Login berhasil
            Log.d("USER_LOGIN", "=== LOGIN BERHASIL ===");
            Log.d("USER_LOGIN", "Nama     : " + user.getNama());
            Log.d("USER_LOGIN", "Email    : " + user.getEmail());
            Log.d("USER_LOGIN", "Phone    : " + user.getPhone());
            Log.d("USER_LOGIN", "Password : " + user.getPassword());

            Toast.makeText(
                    LoginActivity.this,
                    "Selamat datang, " + user.getNama() + "!",
                    Toast.LENGTH_SHORT
            ).show();

            // Arahkan ke MainActivity (Dashboard)
            Intent intent = new Intent(LoginActivity.this, dashboard.class);
            intent.putExtra("USER_EMAIL", user.getEmail());
            intent.putExtra("USER_NAMA", user.getNama());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        });


        // Event tombol Daftar → navigasi ke RegisterActivity
        btnRegister.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );
            startActivity(intent);

        });


        // Event Lupa Password
        tvForgot.setOnClickListener(v -> {

            Toast.makeText(
                    LoginActivity.this,
                    "Fitur Lupa Password Belum Tersedia",
                    Toast.LENGTH_SHORT
            ).show();

        });

    }

    // Method untuk log semua user yang terdaftar di database
    private void logSemuaUser() {
        java.util.List<User> users = UserRepository.getAllUsers();
        Log.d("USER_DB", "=== DAFTAR SEMUA USER DI DATABASE ===");
        if (users.isEmpty()) {
            Log.d("USER_DB", "Database kosong, belum ada user terdaftar.");
        } else {
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                Log.d("USER_DB", "User [" + (i+1) + "]");
                Log.d("USER_DB", "  Nama     : " + u.getNama());
                Log.d("USER_DB", "  Email    : " + u.getEmail());
                Log.d("USER_DB", "  Phone    : " + u.getPhone());
                Log.d("USER_DB", "  Password : " + u.getPassword());
            }
        }
        Log.d("USER_DB", "Total user: " + users.size());
    }
}