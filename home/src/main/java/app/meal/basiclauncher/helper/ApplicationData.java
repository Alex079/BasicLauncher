package app.meal.basiclauncher.helper;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class ApplicationData implements ItemData {

    private final String packageName;
    private final String name;

    public ApplicationData(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
    }

    @Override
    public String toString() {
        return packageName+"|"+name;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public CharSequence getLabel(PackageManager packageManager) {
        ResolveInfo info = packageManager.resolveActivity(new Intent().setClassName(packageName, name), 0);
        return info == null
                ? packageName
                : info.activityInfo.loadLabel(packageManager);
    }

    public Drawable getIcon(PackageManager packageManager) {
        ResolveInfo info = packageManager.resolveActivity(new Intent().setClassName(packageName, name), 0);
        return info == null
                ? packageManager.getDefaultActivityIcon()
                : info.activityInfo.loadIcon(packageManager);
    }

    public Intent getIntent() {
        return new Intent().setClassName(packageName, name);
    }
}
