package app.meal.basiclauncher.helper;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

public class WidgetListAdapter extends BaseAdapter {

    private List<AppWidgetProviderInfo> data;
    private final LayoutInflater inflater;
    private final PackageManager packageManager;
    private final Comparator<AppWidgetProviderInfo> comparator;
    @LayoutRes private final int layoutRes;
    @IdRes private final int labelRes;
    @IdRes private final int iconRes;
    @IdRes private final int pictureRes;

    public WidgetListAdapter(Context context, @LayoutRes int layout, @IdRes int label, @IdRes int icon, @IdRes int picture) {
        super();
        layoutRes = layout;
        labelRes = label;
        iconRes = icon;
        pictureRes = picture;
        inflater = LayoutInflater.from(context);
        packageManager = context.getPackageManager();
        comparator = new Comparator<AppWidgetProviderInfo>() {
            @Override
            public int compare(AppWidgetProviderInfo leftItem, AppWidgetProviderInfo rightItem) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return leftItem.loadLabel(packageManager).compareTo(rightItem.loadLabel(packageManager));
                } else {
                    return leftItem.label.compareTo(rightItem.label);
                }
            }
        };
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public AppWidgetProviderInfo getItem(int i) {
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
        AppWidgetProviderInfo item = getItem(i);
        Drawable icon = null;
        Drawable picture = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((TextView) view.findViewById(labelRes)).setText(item.loadLabel(packageManager));
            picture = item.loadPreviewImage(viewGroup.getContext(), 0);
            if (picture == null) icon = item.loadIcon(viewGroup.getContext(), 0);
        } else {
            ((TextView) view.findViewById(labelRes)).setText(item.label);
            try {
                if (item.previewImage > 0) {
                    picture = packageManager.getResourcesForApplication(packageManager.getApplicationInfo(item.provider.getPackageName(), 0)).getDrawable(item.previewImage);
                } else {
                    icon = packageManager.getApplicationIcon(item.provider.getPackageName());
                }
            } catch (PackageManager.NameNotFoundException e1) {
                icon = packageManager.getDefaultActivityIcon();
            }
        }
        ((ImageView) view.findViewById(pictureRes)).setImageDrawable(picture);
        ((ImageView) view.findViewById(iconRes)).setImageDrawable(icon);
        return view;
    }

    public void setData(List<AppWidgetProviderInfo> data) {
        if (data == null) {
            this.data = null;
        } else {
            this.data = new ArrayList<>(data);
            Collections.sort(this.data, comparator);
        }
    }
}
