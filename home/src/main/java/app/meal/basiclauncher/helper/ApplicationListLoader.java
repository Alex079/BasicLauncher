package app.meal.basiclauncher.helper;

import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

import app.meal.basiclauncher.R;

public class ApplicationListLoader extends AsyncTaskLoader<List<ApplicationData>> {

    public ApplicationListLoader(Context context) {
        super(context);
        packageFilter.addDataScheme(getContext().getString(R.string.package_scheme_name));
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        externalAvailableFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        externalAvailableFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
    }

    private List<ApplicationData> applicationList;

    @Override
    public List<ApplicationData> loadInBackground() {
        PackageManager packageManager = getContext().getPackageManager();
        List<ResolveInfo> availableActivities = packageManager.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);
        List<ApplicationData> result = new ArrayList<>();
        for (ResolveInfo info : availableActivities) {
            result.add(new ApplicationData(info.activityInfo.packageName, info.activityInfo.name));
        }
        return result;
    }

    @Override
    public void deliverResult(List<ApplicationData> data) {
        if (isReset()) {
            return;
        }
        applicationList = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (applicationList == null || takeContentChanged()) {
            forceLoad();
        } else {
            deliverResult(applicationList);
        }
        registerReceiver();
        super.onStartLoading();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        unregisterReceiver();
        applicationList = null;
        onStopLoading();
        super.onReset();
    }

    private final IntentFilter packageFilter = new IntentFilter();
    private final IntentFilter externalAvailableFilter = new IntentFilter();

    private boolean isRegistered = false;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            onContentChanged();
        }
    };

    private void registerReceiver() {
        getContext().registerReceiver(receiver, packageFilter);
        getContext().registerReceiver(receiver, externalAvailableFilter);
        isRegistered = true;
    }

    private void unregisterReceiver() {
        if (isRegistered) {
            getContext().unregisterReceiver(receiver);
            isRegistered = false;
        }
    }
}
