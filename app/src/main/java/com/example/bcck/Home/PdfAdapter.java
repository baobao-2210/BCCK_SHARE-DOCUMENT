package com.example.bcck.Home;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bcck.R;

import java.util.List;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.PdfViewHolder> {

    private List<PdfItem> list;
    private OnPdfClickListener listener;

    public interface OnPdfClickListener {
        void onClick(PdfItem item);
    }

    public PdfAdapter(List<PdfItem> list, OnPdfClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @Override
    public PdfViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pdf, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PdfViewHolder holder, int position) {
        PdfItem item = list.get(position);
        holder.txtTitle.setText(item.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class PdfViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;

        PdfViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
        }
    }
}

