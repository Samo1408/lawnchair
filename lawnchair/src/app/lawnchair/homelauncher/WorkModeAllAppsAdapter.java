package app.lawnchair.homelauncher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.R;
import com.android.launcher3.model.data.AppInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkModeAllAppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER_HEADER = 0;
    private static final int TYPE_APP_ICON = 1;
    private final Context context;
    private final LayoutInflater inflater;
    private final List<RowItem> items = new ArrayList<>();
    private OnWorkAppClickListener clickListener;

    public interface OnWorkAppClickListener { void onAppClick(AppInfo app); void onAppLongClick(AppInfo app, View anchor); }

    static class RowItem { int type; String headerTitle; AppInfo appInfo; }
    static class HeaderViewHolder extends RecyclerView.ViewHolder { TextView title; HeaderViewHolder(View v) { super(v); title = v.findViewById(R.id.user_group_title); } }
    static class AppViewHolder extends RecyclerView.ViewHolder { BubbleTextView icon; AppViewHolder(BubbleTextView v) { super(v); icon = v; } }

    public WorkModeAllAppsAdapter(Context context) { this.context = context; this.inflater = LayoutInflater.from(context); }
    public void setOnWorkAppClickListener(OnWorkAppClickListener l) { this.clickListener = l; }

    public void loadWorkApps(Map<String, List<AppInfo>> groupedApps) {
        items.clear();
        for (Map.Entry<String, List<AppInfo>> e : groupedApps.entrySet()) {
            if ("Personal".equals(e.getKey())) continue;
            RowItem h = new RowItem(); h.type = TYPE_USER_HEADER; h.headerTitle = e.getKey();
            items.add(h);
            for (AppInfo app : e.getValue()) {
                RowItem a = new RowItem(); a.type = TYPE_APP_ICON; a.appInfo = app;
                items.add(a);
            }
        }
        notifyDataSetChanged();
    }

    @Override public int getItemViewType(int pos) { return items.get(pos).type; }
    @Override public int getItemCount() { return items.size(); }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER_HEADER) return new HeaderViewHolder(inflater.inflate(R.layout.user_group_header, parent, false));
        return new AppViewHolder((BubbleTextView) inflater.inflate(R.layout.all_apps_icon, parent, false));
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder h, int pos) {
        RowItem item = items.get(pos);
        if (h instanceof HeaderViewHolder) ((HeaderViewHolder) h).title.setText(item.headerTitle);
        else if (h instanceof AppViewHolder && item.appInfo != null) {
            BubbleTextView icon = ((AppViewHolder) h).icon;
            icon.applyFromApplicationInfo(item.appInfo);
            icon.setOnClickListener(v -> { if(clickListener!=null) clickListener.onAppClick(item.appInfo); });
            icon.setOnLongClickListener(v -> { if(clickListener!=null) clickListener.onAppLongClick(item.appInfo,v); return true; });
        }
    }
}
