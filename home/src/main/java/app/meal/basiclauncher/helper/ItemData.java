package app.meal.basiclauncher.helper;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import java.util.regex.Pattern;

public abstract class ItemData {

    protected abstract Type getMarker();

    public static ItemData fromString(String data, Context context) {
        String[] split = SPLITTER.split(data, 2);
        switch (Type.valueOf(split[0]))
        {
            case APP:
                return new ApplicationData(split[1]);
            case WIDGET:
                int id = Integer.parseInt(split[1]);
                return new WidgetData(id, AppWidgetManager.getInstance(context).getAppWidgetInfo(id));
        }
        return null;
    }

    protected enum Type {
        APP, WIDGET
    }

    private static final Pattern SPLITTER = Pattern.compile("\\|");
}
