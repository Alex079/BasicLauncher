package app.meal.basiclauncher.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.meal.basiclauncher.R;

public class ApplicationListAdapter extends BaseAdapter {

    private List<ApplicationData> data;
    private final LayoutInflater inflater;
    private final PackageManager packageManager;
    private final Comparator<ApplicationData> comparator;
    @LayoutRes private final int layoutRes;
    @IdRes private final int labelRes;
    @IdRes private final int iconRes;

    public ApplicationListAdapter(Context context, @LayoutRes int layout, @IdRes int label, @IdRes int icon) {
        super();
        layoutRes = layout;
        labelRes = label;
        iconRes = icon;
        inflater = LayoutInflater.from(context);
        packageManager = context.getPackageManager();
        comparator = new Comparator<ApplicationData>() {
            @Override
            public int compare(ApplicationData leftItem, ApplicationData rightItem) {
                return leftItem.getLabel(packageManager).toString().compareTo(rightItem.getLabel(packageManager).toString());
            }
        };
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public ApplicationData getItem(int i) {
        return data == null ? null : data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(layoutRes, null);
        }
        ApplicationData applicationData = getItem(i);
        ((TextView) view.findViewById(labelRes)).setText(applicationData.getLabel(packageManager));
        ((ImageView) view.findViewById(iconRes)).setImageDrawable(applicationData.getIcon(packageManager));
        view.setTag(R.integer.tag_app_data, applicationData);
        return view;
    }

    public void setData(List<ApplicationData> data) {
        if (data == null) {
            this.data = null;
        } else {
            this.data = new ArrayList<>(data);
            Collections.sort(this.data, comparator);
        }
    }
}
