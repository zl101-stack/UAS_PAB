package edu.uph.m24si2.uas_pab.db;

import java.util.List;
import java.util.UUID;

import edu.uph.m24si2.uas_pab.model.SavingTarget;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class SavingRepository {

    public static void addTarget(String userEmail, String namaTarget, long targetAmount) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(r -> {
                SavingTarget target = new SavingTarget(
                        UUID.randomUUID().toString(),
                        userEmail, namaTarget, targetAmount, 0L,
                        System.currentTimeMillis());
                r.insertOrUpdate(target);
            });
        } finally {
            realm.close();
        }
    }

    public static void addSaving(String targetId, long amount) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(r -> {
                SavingTarget target = r.where(SavingTarget.class)
                        .equalTo("id", targetId).findFirst();
                if (target != null) {
                    target.setSavedAmount(target.getSavedAmount() + amount);
                }
            });
        } finally {
            realm.close();
        }
    }

    public static void deleteTarget(String targetId) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(r -> {
                SavingTarget target = r.where(SavingTarget.class)
                        .equalTo("id", targetId).findFirst();
                if (target != null) target.deleteFromRealm();
            });
        } finally {
            realm.close();
        }
    }

    public static List<SavingTarget> getAllTargets(String userEmail) {
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<SavingTarget> results = realm.where(SavingTarget.class)
                    .equalTo("userEmail", userEmail)
                    .sort("createdAt", Sort.DESCENDING)
                    .findAll();
            return realm.copyFromRealm(results);
        } finally {
            realm.close();
        }
    }

    public static long getTotalSaved(String userEmail) {
        Realm realm = Realm.getDefaultInstance();
        try {
            Number total = realm.where(SavingTarget.class)
                    .equalTo("userEmail", userEmail)
                    .sum("savedAmount");
            return total != null ? total.longValue() : 0L;
        } finally {
            realm.close();
        }
    }
}
