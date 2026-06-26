package edu.uph.m24si2.uas_pab.db;

import edu.uph.m24si2.uas_pab.model.User;
import io.realm.Realm;

/**
 * Repository untuk operasi database User menggunakan Realm.
 */
public class UserRepository {

    /**
     * Menyimpan pengguna baru ke Realm.
     * Mengembalikan null jika berhasil, atau pesan error jika gagal.
     *
     * @param nama     Nama lengkap pengguna
     * @param email    Email pengguna (primary key, harus unik)
     * @param phone    Nomor HP pengguna
     * @param password Password pengguna
     * @return null jika berhasil, String pesan error jika gagal
     */
    public static String registerUser(String nama, String email, String phone, String password) {
        Realm realm = Realm.getDefaultInstance();
        try {
            // Cek apakah email sudah terdaftar
            User existing = realm.where(User.class)
                    .equalTo("email", email)
                    .findFirst();

            if (existing != null) {
                return "Email sudah terdaftar";
            }

            // Simpan pengguna baru
            realm.executeTransaction(r -> {
                User user = new User(nama, email, phone, password);
                r.insertOrUpdate(user);
            });

            return null; // null = sukses

        } finally {
            realm.close();
        }
    }

    /**
     * Memvalidasi login pengguna berdasarkan email dan password.
     *
     * @param email    Email yang diinput
     * @param password Password yang diinput
     * @return objek User jika login valid, null jika tidak ditemukan
     */
    public static User loginUser(String email, String password) {
        Realm realm = Realm.getDefaultInstance();
        try {
            User user = realm.where(User.class)
                    .equalTo("email", email)
                    .equalTo("password", password)
                    .findFirst();

            // Buat salinan agar bisa digunakan di luar Realm instance
            return user != null ? realm.copyFromRealm(user) : null;

        } finally {
            realm.close();
        }
    }

    /**
     * Mencari pengguna berdasarkan email.
     *
     * @param email Email yang dicari
     * @return salinan objek User, atau null jika tidak ditemukan
     */
    public static User findByEmail(String email) {
        Realm realm = Realm.getDefaultInstance();
        try {
            User user = realm.where(User.class)
                    .equalTo("email", email)
                    .findFirst();
            return user != null ? realm.copyFromRealm(user) : null;
        } finally {
            realm.close();
        }
    }

    /**
     * Mengambil semua user dari database (untuk keperluan debug).
     *
     * @return List semua User yang terdaftar
     */
    public static java.util.List<User> getAllUsers() {
        Realm realm = Realm.getDefaultInstance();
        try {
            return realm.copyFromRealm(realm.where(User.class).findAll());
        } finally {
            realm.close();
        }
    }
}
