package edu.uph.m24si2.uas_pab.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class SavingTarget extends RealmObject {

    @PrimaryKey
    private String id;

    @Required
    private String userEmail;

    @Required
    private String namaTarget;

    private long targetAmount;
    private long savedAmount;
    private long createdAt;

    public SavingTarget() {}

    public SavingTarget(String id, String userEmail, String namaTarget,
                        long targetAmount, long savedAmount, long createdAt) {
        this.id           = id;
        this.userEmail    = userEmail;
        this.namaTarget   = namaTarget;
        this.targetAmount = targetAmount;
        this.savedAmount  = savedAmount;
        this.createdAt    = createdAt;
    }

    public String getId()                           { return id; }
    public void   setId(String id)                  { this.id = id; }
    public String getUserEmail()                    { return userEmail; }
    public void   setUserEmail(String userEmail)    { this.userEmail = userEmail; }
    public String getNamaTarget()                   { return namaTarget; }
    public void   setNamaTarget(String namaTarget)  { this.namaTarget = namaTarget; }
    public long   getTargetAmount()                 { return targetAmount; }
    public void   setTargetAmount(long targetAmount){ this.targetAmount = targetAmount; }
    public long   getSavedAmount()                  { return savedAmount; }
    public void   setSavedAmount(long savedAmount)  { this.savedAmount = savedAmount; }
    public long   getCreatedAt()                    { return createdAt; }
    public void   setCreatedAt(long createdAt)      { this.createdAt = createdAt; }

    public int getProgressPercent() {
        if (targetAmount <= 0) return 0;
        int pct = (int) ((savedAmount * 100L) / targetAmount);
        return Math.min(pct, 100);
    }
}
