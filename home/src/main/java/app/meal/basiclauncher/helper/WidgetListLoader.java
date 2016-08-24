package app.meal.basiclauncher.helper;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.List;

import app.meal.basiclauncher.R;

public class WidgetListLoader extends AsyncTaskLoader<List<AppWidgetProviderInfo>> {

    public WidgetListLoader(Context context) {
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

    private List<AppWidgetProviderInfo> applicationList;

    @Override
    public List<AppWidgetProviderInfo> loadInBackground() {
        return AppWidgetManager.getInstance(getContext()).getInstalledProviders();
    }

    @Override
    public void deliverResult(List<AppWidgetProviderInfo> data) {
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
