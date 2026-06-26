package edu.uph.m24si2.uas_pab.db;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Helper class untuk konfigurasi dan inisialisasi Realm database.
 * Panggil RealmConfig.init(context) satu kali di Application atau MainActivity.
 */
public class RealmConfig {

    private static final String DB_NAME    = "keuangan.realm";
    private static final long   DB_VERSION = 1;

    /**
     * Inisialisasi Realm. Harus dipanggil sebelum Realm.getDefaultInstance() digunakan.
     */
    public static void init(Context context) {
        Realm.init(context);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(DB_NAME)
                .schemaVersion(DB_VERSION)
                .allowWritesOnUiThread(true)
                .allowQueriesOnUiThread(true)
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.setDefaultConfiguration(config);
    }
}
