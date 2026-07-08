package edu.uph.m24si2.uas_pab.model;

/**
 * Model data untuk satu item notifikasi (hardcoded / dummy).
 * Tidak disimpan ke Realm — hanya dipakai untuk tampilan presentasi.
 */
public class NotifikasiItem {

    /** Tipe notifikasi, dipakai untuk menentukan warna ikon. */
    public enum Tipe {
        PERINGATAN,   // Merah  — pengeluaran berlebih, budget hampir habis
        INFO,         // Biru   — pengingat umum
        SUKSES,       // Hijau  — target tercapai, tabungan berhasil
        TIPS          // Kuning — saran keuangan
    }

    private final String judul;
    private final String pesan;
    private final String waktu;   // Contoh: "2 hari lalu", "Minggu lalu"
    private final Tipe   tipe;
    private boolean      sudahDibaca;
    private boolean      diBintangi;

    public NotifikasiItem(String judul, String pesan, String waktu, Tipe tipe) {
        this.judul       = judul;
        this.pesan       = pesan;
        this.waktu       = waktu;
        this.tipe        = tipe;
        this.sudahDibaca = false;
        this.diBintangi  = false;
    }

    public String  getJudul()       { return judul; }
    public String  getPesan()       { return pesan; }
    public String  getWaktu()       { return waktu; }
    public Tipe    getTipe()        { return tipe; }
    public boolean isSudahDibaca()  { return sudahDibaca; }
    public void    setSudahDibaca(boolean v) { this.sudahDibaca = v; }
    public boolean isDiBintangi()   { return diBintangi; }
    public void    setDiBintangi(boolean v)  { this.diBintangi = v; }
}
