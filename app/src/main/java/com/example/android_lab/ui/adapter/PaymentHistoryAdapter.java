package com.example.android_lab.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_lab.R;
import com.example.android_lab.models.PaymentHistoryItem;
import com.example.android_lab.utils.StatusMapper;

import java.util.List;
import android.widget.PopupMenu;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.HistoryViewHolder> {
    private final List<PaymentHistoryItem> historyList;
    private OnItemClickListener listener;
    private OnStatusUpdateListener statusUpdateListener;
    private boolean isAdmin = false;

    public interface OnItemClickListener {
        void onItemClick(PaymentHistoryItem item);
    }

    public interface OnStatusUpdateListener {
        void onStatusUpdate(PaymentHistoryItem item, String newStatus);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnStatusUpdateListener(OnStatusUpdateListener listener) {
        this.statusUpdateListener = listener;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public PaymentHistoryAdapter(List<PaymentHistoryItem> historyList) {
        this.historyList = historyList;
    }
    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_history, parent, false);
        return new HistoryViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        PaymentHistoryItem item = historyList.get(position);
        holder.tvOrderId.setText("Mã đơn: " + item.getOrderId());
        holder.tvDate.setText("Ngày: " + item.getDate());
        holder.tvAmount.setText(String.format("%,.0f₫", item.getAmount()));
        holder.tvStatus.setText("Trạng thái: " + StatusMapper.toVietnamese(item.getStatus()));

        // Chỉ admin mới có thể cập nhật trạng thái
        if (isAdmin) {
            holder.itemView.setOnClickListener(v -> showStatusUpdateMenu(v, item));
        } else {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }

    private void showStatusUpdateMenu(View view, PaymentHistoryItem item) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenu().add("Đã xác nhận");
        popup.getMenu().add("Đang xử lý");
        popup.getMenu().add("Đã hoàn thành");
        popup.getMenu().add("Đã hủy");

        popup.setOnMenuItemClickListener(menuItem -> {
            String newStatus = menuItem.getTitle().toString();
            if (statusUpdateListener != null) {
                statusUpdateListener.onStatusUpdate(item, newStatus);
            }
            return true;
        });

        popup.show();
    }
    @Override
    public int getItemCount() {
        return historyList.size();
    }
    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvDate, tvAmount, tvStatus;
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
