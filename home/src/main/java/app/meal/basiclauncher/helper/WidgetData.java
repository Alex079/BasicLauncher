package app.meal.basiclauncher.helper;

import android.appwidget.AppWidgetProviderInfo;

public class WidgetData extends ItemData {

    private final int id;

    private final AppWidgetProviderInfo info;

    public WidgetData(int id, AppWidgetProviderInfo info) {
        this.id = id;
        this.info = info;
    }

    public int getId() {
        return id;
    }

    public AppWidgetProviderInfo getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return getMarker() + "|" + String.valueOf(id);
    }

    @Override
    protected Type getMarker() {
        return Type.WIDGET;
    }
}
