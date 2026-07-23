package edu.uph.m24si2.uas_pab.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
     * Menyimpan transaksi pemakaian budget — tercatat di histori tapi tidak mempengaruhi saldo.
     */
    public static void addBudgetTransaction(String userEmail, long amount,
                                             String category, String categoryIcon,
                                             String date, String notes) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(r -> {
                Transaction t = new Transaction(
                        UUID.randomUUID().toString(),
                        userEmail, "PENGELUARAN", amount,
                        category, categoryIcon, date, notes,
                        System.currentTimeMillis()
                );
                t.setBudgetUsage(true); // tandai sebagai budget usage
                r.insertOrUpdate(t);
            });
        } finally {
            realm.close();
        }
    }

    /**
     * Menghapus semua transaksi budget (PENGELUARAN dengan notes mengandung bulan tertentu
     * dan PEMASUKAN "Pengembalian Budget") untuk bulan tertentu.
     * Dipanggil saat reset budget.
     */
    public static void deleteBudgetTransactions(String userEmail, String bulanTahun) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(r -> {
                // Hapus pengeluaran budget bulan ini (dicatat saat set budget)
                RealmResults<Transaction> pengeluaranBudget = r.where(Transaction.class)
                        .equalTo("userEmail", userEmail)
                        .equalTo("type", "PENGELUARAN")
                        .equalTo("category", "Budget")
                        .contains("notes", bulanTahun)
                        .findAll();
                pengeluaranBudget.deleteAllFromRealm();

                // Hapus SEMUA pemakaian budget (isBudgetUsage = true) milik user ini
                RealmResults<Transaction> pemakaianBudget = r.where(Transaction.class)
                        .equalTo("userEmail", userEmail)
                        .equalTo("isBudgetUsage", true)
                        .findAll();
                pemakaianBudget.deleteAllFromRealm();
            });
        } finally {
            realm.close();
        }
    }

    /**
     * Menghapus semua transaksi tabungan berdasarkan targetId di notes.
     * Dipanggil saat target tabungan dihapus agar saldo kembali.
     */
    public static void deleteSavingTransactions(String userEmail, String targetId) {
        // Ambil nama target dulu untuk filter notes
        String namaTarget;
        Realm realmCheck = Realm.getDefaultInstance();
        try {
            io.realm.RealmObject t = realmCheck.where(
                    edu.uph.m24si2.uas_pab.model.SavingTarget.class)
                    .equalTo("id", targetId).findFirst();
            namaTarget = t != null
                    ? ((edu.uph.m24si2.uas_pab.model.SavingTarget) t).getNamaTarget()
                    : targetId;
        } finally {
            realmCheck.close();
        }

        final String finalNama = namaTarget;
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(r -> {
                RealmResults<Transaction> results = r.where(Transaction.class)
                        .equalTo("userEmail", userEmail)
                        .equalTo("category", "Tabungan")
                        .contains("notes", finalNama)
                        .findAll();
                results.deleteAllFromRealm();
            });
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
                    .equalTo("isBudgetUsage", false) // skip budget usage
                    .sum("amount");
            return total != null ? total.longValue() : 0L;
        } finally {
            realm.close();
        }
    }

    // ─── Helper format tanggal ────────────────────────────────────────────────

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    /** Parse string "dd/MM/yyyy" → Date, null jika gagal. */
    private static Date parseDate(String dateStr) {
        try {
            return DATE_FMT.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    // ─── Query grafik harian (7 hari terakhir) ────────────────────────────────

    /**
     * Mengembalikan map {label → amount} untuk 7 hari terakhir (D-6 s/d hari ini).
     * Label berformat "dd/MM".
     *
     * @param userEmail Email pemilik
     * @param type      "PEMASUKAN" atau "PENGELUARAN"
     * @return LinkedHashMap terurut dari hari terlama ke terbaru
     */
    public static Map<String, Long> getDailyData(String userEmail, String type) {
        // Bangun daftar 7 hari terakhir sebagai key terurut
        LinkedHashMap<String, Long> result = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        // Mundur 6 hari → 7 hari total termasuk hari ini
        cal.add(Calendar.DAY_OF_YEAR, -6);
        SimpleDateFormat labelFmt = new SimpleDateFormat("dd/MM", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            result.put(labelFmt.format(cal.getTime()), 0L);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Query semua transaksi user + tipe
        Realm realm = Realm.getDefaultInstance();
        try {
            List<Transaction> all = realm.copyFromRealm(
                    realm.where(Transaction.class)
                            .equalTo("userEmail", userEmail)
                            .equalTo("type", type)
                            .findAll()
            );
            for (Transaction t : all) {
                Date d = parseDate(t.getDate());
                if (d == null) continue;
                String label = labelFmt.format(d);
                if (result.containsKey(label)) {
                    result.put(label, result.get(label) + t.getAmount());
                }
            }
        } finally {
            realm.close();
        }
        return result;
    }

    // ─── Query grafik mingguan (8 minggu terakhir) ────────────────────────────

    /**
     * Mengembalikan map {label → amount} untuk 8 minggu terakhir.
     * Label berformat "W{weekOfYear}" (mis. "W27").
     *
     * @param userEmail Email pemilik
     * @param type      "PEMASUKAN" atau "PENGELUARAN"
     * @return LinkedHashMap terurut dari minggu terlama ke terbaru
     */
    public static Map<String, Long> getWeeklyData(String userEmail, String type) {
        LinkedHashMap<String, Long> result = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -7); // mulai dari 8 minggu lalu

        List<String> weekKeys = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int week  = cal.get(Calendar.WEEK_OF_YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            String label = "Mg" + week + "/" + String.format(Locale.getDefault(), "%02d", month);
            weekKeys.add(label);
            result.put(label, 0L);
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        }

        Realm realm = Realm.getDefaultInstance();
        try {
            List<Transaction> all = realm.copyFromRealm(
                    realm.where(Transaction.class)
                            .equalTo("userEmail", userEmail)
                            .equalTo("type", type)
                            .findAll()
            );
            Calendar txCal = Calendar.getInstance();
            for (Transaction t : all) {
                Date d = parseDate(t.getDate());
                if (d == null) continue;
                txCal.setTime(d);
                int week  = txCal.get(Calendar.WEEK_OF_YEAR);
                int month = txCal.get(Calendar.MONTH) + 1;
                String label = "Mg" + week + "/" + String.format(Locale.getDefault(), "%02d", month);
                if (result.containsKey(label)) {
                    result.put(label, result.get(label) + t.getAmount());
                }
            }
        } finally {
            realm.close();
        }
        return result;
    }

    // ─── Query grafik bulanan (6 bulan terakhir) ──────────────────────────────

    /**
     * Mengembalikan map {label → amount} untuk 6 bulan terakhir.
     * Label berformat "MMM yyyy" (mis. "Jan 2025").
     *
     * @param userEmail Email pemilik
     * @param type      "PEMASUKAN" atau "PENGELUARAN"
     * @return LinkedHashMap terurut dari bulan terlama ke terbaru
     */
    public static Map<String, Long> getMonthlyData(String userEmail, String type) {
        LinkedHashMap<String, Long> result = new LinkedHashMap<>();
        SimpleDateFormat monthFmt = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -5); // mulai dari 6 bulan lalu

        for (int i = 0; i < 6; i++) {
            result.put(monthFmt.format(cal.getTime()), 0L);
            cal.add(Calendar.MONTH, 1);
        }

        Realm realm = Realm.getDefaultInstance();
        try {
            List<Transaction> all = realm.copyFromRealm(
                    realm.where(Transaction.class)
                            .equalTo("userEmail", userEmail)
                            .equalTo("type", type)
                            .findAll()
            );
            for (Transaction t : all) {
                Date d = parseDate(t.getDate());
                if (d == null) continue;
                String label = monthFmt.format(d);
                if (result.containsKey(label)) {
                    result.put(label, result.get(label) + t.getAmount());
                }
            }
        } finally {
            realm.close();
        }
        return result;
    }

    /**
     * Menghitung total per kategori untuk tipe tertentu.
     * Digunakan untuk Pie Chart di GrafikActivity.
     */
    public static Map<String, Long> getCategoryData(String userEmail, String type) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<Transaction> results = realm.where(Transaction.class)
                    .equalTo("userEmail", userEmail)
                    .equalTo("type", type)
                    .findAll();

            Map<String, Long> map = new LinkedHashMap<>();
            for (Transaction t : results) {
                String cat = t.getCategory();
                map.put(cat, map.containsKey(cat) ? map.get(cat) + t.getAmount() : t.getAmount());
            }
            return map;
        } finally {
            realm.close();
        }
    }
}
