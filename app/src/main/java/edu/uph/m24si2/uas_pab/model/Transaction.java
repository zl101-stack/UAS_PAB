package edu.uph.m24si2.uas_pab.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Model Realm untuk menyimpan data transaksi (Pemasukan & Pengeluaran).
 */
public class Transaction extends RealmObject {

    @PrimaryKey
    private String id;          // UUID sebagai primary key

    @Required
    private String userEmail;   // Relasi ke User (email pemilik transaksi)

    @Required
    private String type;        // "PEMASUKAN" atau "PENGELUARAN"

    private long amount;        // Jumlah dalam Rupiah

    @Required
    private String category;    // Kategori transaksi (mis. "Gaji", "Makan", dll.)

    private String categoryIcon; // Nama ikon kategori

    @Required
    private String date;        // Tanggal transaksi (format: dd/MM/yyyy)

    private String notes;       // Catatan opsional

    private long createdAt;     // Timestamp pembuatan (epoch millis)

    // Constructor kosong wajib untuk Realm
    public Transaction() {}

    public Transaction(String id, String userEmail, String type, long amount,
                       String category, String categoryIcon, String date,
                       String notes, long createdAt) {
        this.id           = id;
        this.userEmail    = userEmail;
        this.type         = type;
        this.amount       = amount;
        this.category     = category;
        this.categoryIcon = categoryIcon;
        this.date         = date;
        this.notes        = notes;
        this.createdAt    = createdAt;
    }

    // --- Getter & Setter ---

    public String getId()                   { return id; }
    public void   setId(String id)          { this.id = id; }

    public String getUserEmail()                        { return userEmail; }
    public void   setUserEmail(String userEmail)        { this.userEmail = userEmail; }

    public String getType()                 { return type; }
    public void   setType(String type)      { this.type = type; }

    public long   getAmount()               { return amount; }
    public void   setAmount(long amount)    { this.amount = amount; }

    public String getCategory()                     { return category; }
    public void   setCategory(String category)      { this.category = category; }

    public String getCategoryIcon()                         { return categoryIcon; }
    public void   setCategoryIcon(String categoryIcon)      { this.categoryIcon = categoryIcon; }

    public String getDate()                 { return date; }
    public void   setDate(String date)      { this.date = date; }

    public String getNotes()                { return notes; }
    public void   setNotes(String notes)    { this.notes = notes; }

    public long   getCreatedAt()                    { return createdAt; }
    public void   setCreatedAt(long createdAt)      { this.createdAt = createdAt; }
}
