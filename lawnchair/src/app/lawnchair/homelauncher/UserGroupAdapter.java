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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_APP = 1;
    private final Context context;
    private final List<Section> sections = new ArrayList<>();
    private final LayoutInflater inflater;
    private OnAppClickListener onAppClickListener;

    public interface OnAppClickListener { void onAppClick(AppInfo app, View view); void onAppLongClick(AppInfo app, View view); }

    static class Section { String title; List<AppInfo> apps; Section(String t, List<AppInfo> a) { title = t; apps = a; } }
    static class HeaderHolder extends RecyclerView.ViewHolder { TextView titleText; HeaderHolder(View v) { super(v); titleText = v.findViewById(R.id.user_group_title); } }
    static class AppHolder extends RecyclerView.ViewHolder { BubbleTextView icon; AppHolder(BubbleTextView v) { super(v); icon = v; } }

    public UserGroupAdapter(Context context) { this.context = context; this.inflater = LayoutInflater.from(context); }
    public void setOnAppClickListener(OnAppClickListener l) { this.onAppClickListener = l; }

    public void setApps(Map<String, List<AppInfo>> groupedApps) {
        sections.clear();
        for (Map.Entry<String, List<AppInfo>> e : groupedApps.entrySet()) {
            if (!e.getValue().isEmpty()) sections.add(new Section(e.getKey(), e.getValue()));
        }
        notifyDataSetChanged();
    }

    @Override public int getItemViewType(int position) { return getItemAt(position) instanceof Section ? TYPE_HEADER : TYPE_APP; }

    private Object getItemAt(int position) {
        int current = 0;
        for (Section s : sections) {
            if (position == current) return s;
            current++;
            int n = s.apps.size();
            if (position < current + n) return s.apps.get(position - current);
            current += n;
        }
        return null;
    }

    @Override public int getItemCount() { int c=0; for(Section s:sections) c+=1+s.apps.size(); return c; }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) return new HeaderHolder(inflater.inflate(R.layout.user_group_header, parent, false));
        return new AppHolder((BubbleTextView) inflater.inflate(R.layout.all_apps_icon, parent, false));
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder h, int pos) {
        Object item = getItemAt(pos);
        if (h instanceof HeaderHolder && item instanceof Section) {
            Section s = (Section) item;
            ((HeaderHolder) h).titleText.setText(s.title + " (" + s.apps.size() + ")");
        } else if (h instanceof AppHolder && item instanceof AppInfo) {
            AppInfo app = (AppInfo) item;
            BubbleTextView icon = ((AppHolder) h).icon;
            icon.applyFromApplicationInfo(app);
            icon.setOnClickListener(v -> { if(onAppClickListener!=null) onAppClickListener.onAppClick(app,v); });
            icon.setOnLongClickListener(v -> { if(onAppClickListener!=null) onAppClickListener.onAppLongClick(app,v); return true; });
        }
    }
}
