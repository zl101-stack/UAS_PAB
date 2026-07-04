package edu.uph.m24si2.uas_pab.db;

import java.util.List;
import java.util.UUID;

import edu.uph.m24si2.uas_pab.model.Transaction;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Repository untuk operasi CRUD transaksi (Pemasukan & Pengeluaran) menggunakan Realm.
 */
public class TransactionRepository {

    /**
     * Menyimpan transaksi baru ke Realm.
     *
     * @param userEmail   Email pemilik transaksi
     * @param type        "PEMASUKAN" atau "PENGELUARAN"
     * @param amount      Jumlah dalam Rupiah
     * @param category    Nama kategori
     * @param categoryIcon Identifier ikon kategori
     * @param date        Tanggal (dd/MM/yyyy)
     * @param notes       Catatan opsional (boleh null/kosong)
     */
    public static void addTransaction(String userEmail, String type, long amount,
                                      String category, String categoryIcon,
                                      String date, String notes) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(r -> {
                Transaction t = new Transaction(
                        UUID.randomUUID().toString(),
                        userEmail, type, amount,
                        category, categoryIcon,
                        date, notes,
                        System.currentTimeMillis()
                );
                r.insertOrUpdate(t);
            });
        } finally {
            realm.close();
        }
    }

    /**
     * Memperbarui transaksi yang sudah ada.
     *
     * @param id          ID transaksi yang akan diperbarui
     * @param amount      Jumlah baru
     * @param category    Kategori baru
     * @param categoryIcon Ikon kategori baru
     * @param date        Tanggal baru
     * @param notes       Catatan baru
     */
    public static void updateTransaction(String id, long amount,
                                         String category, String categoryIcon,
                                         String date, String notes) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(r -> {
                Transaction t = r.where(Transaction.class)
                        .equalTo("id", id)
                        .findFirst();
                if (t != null) {
                    t.setAmount(amount);
                    t.setCategory(category);
                    t.setCategoryIcon(categoryIcon);
                    t.setDate(date);
                    t.setNotes(notes);
                }
            });
        } finally {
            realm.close();
        }
    }

    /**
     * Menghapus transaksi berdasarkan ID.
     *
     * @param id ID transaksi yang dihapus
     */
    public static void deleteTransaction(String id) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(r -> {
                Transaction t = r.where(Transaction.class)
                        .equalTo("id", id)
                        .findFirst();
                if (t != null) t.deleteFromRealm();
            });
        } finally {
            realm.close();
        }
    }

    /**
     * Mengambil semua transaksi milik user berdasarkan tipe, diurutkan terbaru dulu.
     *
     * @param userEmail Email pemilik
     * @param type      "PEMASUKAN" atau "PENGELUARAN"
     * @return List salinan transaksi
     */
    public static List<Transaction> getTransactions(String userEmail, String type) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Transaction> results = realm.where(Transaction.class)
                    .equalTo("userEmail", userEmail)
                    .equalTo("type", type)
                    .sort("createdAt", Sort.DESCENDING)
                    .findAll();
            return realm.copyFromRealm(results);
        } finally {
            realm.close();
        }
    }

    /**
     * Menghitung total jumlah transaksi berdasarkan user dan tipe.
     *
     * @param userEmail Email pemilik
     * @param type      "PEMASUKAN" atau "PENGELUARAN"
     * @return Total jumlah dalam Rupiah
     */
    public static long getTotalAmount(String userEmail, String type) {
        Realm realm = Realm.getDefaultInstance();
        try {
            Number total = realm.where(Transaction.class)
                    .equalTo("userEmail", userEmail)
                    .equalTo("type", type)
                    .sum("amount");
            return total != null ? total.longValue() : 0L;
        } finally {
            realm.close();
        }
    }
}
