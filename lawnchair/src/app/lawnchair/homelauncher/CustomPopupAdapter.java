package app.lawnchair.homelauncher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.R;

import java.util.ArrayList;
import java.util.List;

public class CustomPopupAdapter extends RecyclerView.Adapter<CustomPopupAdapter.ViewHolder> {

    private final Context context;
    private final List<PopupItem> items = new ArrayList<>();
    private OnPopupItemClickListener clickListener;

    public interface OnPopupItemClickListener { void onItemClick(PopupItem item); }

    public static class PopupItem {
        public int iconResId;
        public String label;
        public Runnable action;
        public PopupItem(int iconResId, String label, Runnable action) {
            this.iconResId = iconResId; this.label = label; this.action = action;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon; TextView label;
        ViewHolder(View v) { super(v); icon = v.findViewById(R.id.popup_item_icon); label = v.findViewById(R.id.popup_item_label); }
    }

    public CustomPopupAdapter(Context context) { this.context = context; }
    public void setOnPopupItemClickListener(OnPopupItemClickListener listener) { this.clickListener = listener; }

    public void setItems(List<PopupItem> newItems) { items.clear(); items.addAll(newItems); notifyDataSetChanged(); }
    public void addItem(int iconResId, String label, Runnable action) {
        items.add(new PopupItem(iconResId, label, action)); notifyItemInserted(items.size() - 1);
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.popup_item, parent, false);
        return new ViewHolder(v);
    }
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        PopupItem item = items.get(position);
        holder.icon.setImageResource(item.iconResId);
        holder.label.setText(item.label);
        holder.itemView.setOnClickListener(v -> { if (item.action != null) item.action.run(); if (clickListener != null) clickListener.onItemClick(item); });
    }
    @Override public int getItemCount() { return items.size(); }
}
