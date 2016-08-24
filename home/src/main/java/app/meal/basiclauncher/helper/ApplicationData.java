package app.meal.basiclauncher.helper;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class ApplicationData implements ItemData {

    private final ComponentName componentName;

    public ApplicationData(ComponentName componentName) {
        this.componentName = componentName;
    }

    public ApplicationData(String packageName, String name) {
        this.componentName = new ComponentName(packageName, name);
    }

    public ApplicationData(String flatString) {
        this.componentName = ComponentName.unflattenFromString(flatString);
    }

    @Override
    public String toString() {
        return componentName.flattenToShortString();
    }

    public String getPackageName() {
        return componentName.getPackageName();
    }

    public String getName() {
        return componentName.getClassName();
    }

    public CharSequence getLabel(PackageManager packageManager) {
        try {
            return packageManager.getActivityInfo(componentName, 0).loadLabel(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            return componentName.getPackageName();
        }
    }

    public Drawable getIcon(PackageManager packageManager) {
        try {
            return packageManager.getActivityInfo(componentName, 0).loadIcon(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            return packageManager.getDefaultActivityIcon();
        }
    }

    public Intent getIntent() {
        return new Intent().setComponent(componentName);
    }
}
