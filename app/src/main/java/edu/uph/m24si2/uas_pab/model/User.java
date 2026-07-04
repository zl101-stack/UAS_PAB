package edu.uph.m24si2.uas_pab.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Model Realm untuk menyimpan data pengguna.
 * Setiap field yang di-annotasi @Required tidak boleh null.
 */
public class User extends RealmObject {

    @PrimaryKey
    private String email;       // Email sebagai primary key (unik per pengguna)

    @Required
    private String nama;

    @Required
    private String phone;

    @Required
    private String password;

    // --- Profil tambahan ---
    private String username;    // Username unik (opsional)
    private String photoUri;    // URI foto profil dari galeri
    private String birthDate;   // Tanggal lahir (format: dd/MM/yyyy)
    private int    birthYear;   // Tahun lahir

    // Constructor kosong wajib ada untuk Realm
    public User() {}

    public User(String nama, String email, String phone, String password) {
        this.nama     = nama;
        this.email    = email;
        this.phone    = phone;
        this.password = password;
    }

    // --- Getter & Setter ---

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhotoUri() { return photoUri; }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public int getBirthYear() { return birthYear; }
    public void setBirthYear(int birthYear) { this.birthYear = birthYear; }
}
