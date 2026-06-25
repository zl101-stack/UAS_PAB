package edu.uph.m24si2.uas_pab;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnRegister;
    TextView tvForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Menghubungkan Activity dengan activity_login.xml
        setContentView(R.layout.activity_login);

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

            // Simulasi login berhasil
            Toast.makeText(
                    LoginActivity.this,
                    "Login Berhasil",
                    Toast.LENGTH_SHORT
            ).show();

            /*
            Nanti bisa diarahkan ke Dashboard

            Intent intent = new Intent(
                    LoginActivity.this,
                    DashboardActivity.class
            );
            startActivity(intent);
            finish();
            */

        });


        // Event tombol Daftar
        btnRegister.setOnClickListener(v -> {

            Toast.makeText(
                    LoginActivity.this,
                    "Menuju Halaman Register",
                    Toast.LENGTH_SHORT
            ).show();

            /*
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );
            startActivity(intent);
            */

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
}