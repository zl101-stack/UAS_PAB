package edu.uph.m24si2.uas_pab;

import android.app.Application;
import android.util.Log;

import edu.uph.m24si2.uas_pab.db.RealmConfig;
import edu.uph.m24si2.uas_pab.db.UserRepository;
import edu.uph.m24si2.uas_pab.model.User;

/**
 * Application class — dipanggil pertama kali saat aplikasi berjalan.
 * Digunakan untuk inisialisasi Realm agar siap digunakan di seluruh app.
 */
public class KeuanganApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("REALM_DEBUG", ">>> KeuanganApp.onCreate() DIPANGGIL <<<");
        // Inisialisasi Realm database
        try {
            RealmConfig.init(this);
            Log.e("REALM_DEBUG", ">>> RealmConfig.init() SELESAI <<<");
        } catch (Throwable t) {
            Log.e("REALM_DEBUG", ">>> RealmConfig.init() CRASH: " + t.getMessage(), t);
            return;
        }

        // === DEBUG: Cek isi database saat app pertama kali jalan ===
        try {
            java.util.List<User> users = UserRepository.getAllUsers();
            Log.d("REALM_DEBUG", "App start - Total user di DB: " + users.size());
            for (User u : users) {
                Log.d("REALM_DEBUG", "  -> " + u.getEmail() + " / " + u.getNama());
            }
        } catch (Exception e) {
            Log.e("REALM_DEBUG", "ERROR saat akses Realm: " + e.getMessage(), e);
        }
    }
}
