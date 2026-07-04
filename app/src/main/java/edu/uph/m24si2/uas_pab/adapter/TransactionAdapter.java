package edu.uph.m24si2.uas_pab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.uph.m24si2.uas_pab.R;
import edu.uph.m24si2.uas_pab.model.Transaction;

/**
 * Adapter RecyclerView untuk menampilkan daftar transaksi (Pemasukan / Pengeluaran).
 * Warna amount disesuaikan: hijau untuk PEMASUKAN, merah untuk PENGELUARAN.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public interface OnItemActionListener {
        void onEdit(Transaction transaction);
        void onDelete(Transaction transaction);
    }

    private final Context context;
    private final List<Transaction> transactions;
    private final OnItemActionListener listener;
    private final boolean isExpense; // true = PENGELUARAN (warna merah)

    public TransactionAdapter(Context context, List<Transaction> transactions,
                               OnItemActionListener listener, boolean isExpense) {
        this.context      = context;
        this.transactions = transactions;
        this.listener     = listener;
        this.isExpense    = isExpense;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactions.get(position);

        // Ikon kategori
        holder.ivCategoryIcon.setImageResource(getCategoryIconRes(t.getCategoryIcon()));

        // Nama kategori & tanggal
        holder.tvCategory.setText(t.getCategory());
        holder.tvDate.setText(t.getDate());

        // Catatan (sembunyikan jika kosong)
        if (t.getNotes() != null && !t.getNotes().isEmpty()) {
            holder.tvNotes.setVisibility(View.VISIBLE);
            holder.tvNotes.setText(t.getNotes());
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        // Format jumlah dalam Rupiah
        String prefix = isExpense ? "- " : "+ ";
        holder.tvAmount.setText(prefix + formatRupiah(t.getAmount()));

        // Warna amount
        int colorRes = isExpense
                ? context.getColor(R.color.expense_red)
                : context.getColor(R.color.income_green);
        holder.tvAmount.setTextColor(colorRes);

        // Warna latar ikon kategori
        int bgColorRes = isExpense
                ? context.getColor(R.color.expense_icon_bg)
                : context.getColor(R.color.income_icon_bg);
        holder.ivCategoryIcon.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(bgColorRes));

        // Aksi tombol Edit & Hapus
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(t));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(t));
    }

    @Override
    public int getItemCount() { return transactions.size(); }

    // Mapping nama ikon → drawable resource
    public static int getCategoryIconRes(String iconName) {
        if (iconName == null) return android.R.drawable.ic_menu_help;
        switch (iconName) {
            // Pemasukan
            case "gaji":         return android.R.drawable.ic_menu_agenda;
            case "bisnis":       return android.R.drawable.ic_menu_manage;
            case "investasi":    return android.R.drawable.ic_menu_sort_by_size;
            case "freelance":    return android.R.drawable.ic_menu_edit;
            case "bonus":        return android.R.drawable.ic_menu_add;
            case "hadiah":       return android.R.drawable.ic_menu_compass;
            case "lainnya_in":   return android.R.drawable.ic_menu_more;
            // Pengeluaran
            case "makan":        return android.R.drawable.ic_menu_myplaces;
            case "transport":    return android.R.drawable.ic_menu_directions;
            case "belanja":      return android.R.drawable.ic_menu_gallery;
            case "tagihan":      return android.R.drawable.ic_menu_info_details;
            case "kesehatan":    return android.R.drawable.ic_menu_help;
            case "hiburan":      return android.R.drawable.ic_media_play;
            case "pendidikan":   return android.R.drawable.ic_menu_search;
            case "lainnya_out":  return android.R.drawable.ic_menu_more;
            default:             return android.R.drawable.ic_menu_help;
        }
    }

    /** Format long ke "Rp 1.000.000" */
    public static String formatRupiah(long amount) {
        String raw = String.valueOf(amount);
        StringBuilder sb = new StringBuilder();
        int len = raw.length();
        int count = 0;
        for (int i = len - 1; i >= 0; i--) {
            sb.insert(0, raw.charAt(i));
            count++;
            if (count % 3 == 0 && i != 0) sb.insert(0, '.');
        }
        return "Rp " + sb;
    }

    // --- ViewHolder ---

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView  ivCategoryIcon;
        TextView   tvCategory, tvDate, tvNotes, tvAmount;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategory     = itemView.findViewById(R.id.tvCategory);
            tvDate         = itemView.findViewById(R.id.tvDate);
            tvNotes        = itemView.findViewById(R.id.tvNotes);
            tvAmount       = itemView.findViewById(R.id.tvAmount);
            btnEdit        = itemView.findViewById(R.id.btnEdit);
            btnDelete      = itemView.findViewById(R.id.btnDelete);
        }
    }
}
