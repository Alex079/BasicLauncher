package app.meal.basiclauncher;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;

import app.meal.basiclauncher.event.Event;
import app.meal.basiclauncher.event.LocalEventsManager;
import app.meal.basiclauncher.view.CellLayout;

public class MainLauncherActivity extends Activity implements LocalEventsManager.Listener {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.main_activity);
        getDrawerLayout().setScrimColor(getResources().getColor(R.color.drawerScrim));
        CellLayout cellLayout = getCellLayout();
        cellLayout.setFollowRotation(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                getString(R.string.icons_follow_key), getResources().getBoolean(R.bool.icons_follow_default)
        ), false);
        cellLayout.setGridSize(PreferenceManager.getDefaultSharedPreferences(this).getInt(
                getString(R.string.dock_size_key), getResources().getInteger(R.integer.dock_size_default)
        ), false);
        cellLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View view) {
                widgetBeingAdded = ((CellLayout) view).allocateAppWidgetId();
                startActivityForResult(
                        new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetBeingAdded),
                        R.integer.app_widget_signal_pick);
                return true;
            }
        });
        if (state == null) {
            cellLayout.initializeState();
            cellLayout.startListeningWidgetEvents();
        }

        LocalEventsManager.getInstance().register(this);

//        orientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
//            private int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            {
//                Log.w("orientation init", String.valueOf(rotation));
//            }
//            @Override public void onOrientationChanged(int newOrientation) {
//                if (newOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
//                    int newRotation = getWindowManager().getDefaultDisplay().getRotation();
//                    if (rotation != newRotation) {
//                        Log.w("orientation change", rotation + " " + newRotation);
//                    }
//                }
//            }
//        };
//        orientationListener.enable();
    }

//    private OrientationEventListener orientationListener;

    private int widgetBeingAdded = -1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            int id = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            switch (requestCode) {
                case R.integer.app_widget_signal_pick:
                    AppWidgetProviderInfo appWidgetInfo = AppWidgetManager.getInstance(this).getAppWidgetInfo(id);
                    if (appWidgetInfo.configure != null) {
                        startActivityForResult(
                                new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                                        .setComponent(appWidgetInfo.configure)
                                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id),
                                R.integer.app_widget_signal_create);
                        break;
                    }
                case R.integer.app_widget_signal_create:
                    getCellLayout().placeWidget(id);
                    break;
                default:
            }
        } else if (widgetBeingAdded >= 0) {
            getCellLayout().deleteAppWidgetId(widgetBeingAdded);
            widgetBeingAdded = -1;
        }
    }

    @Override
    protected void onDestroy() {
        getCellLayout().stopListeningWidgetEvents();
        //orientationListener.disable();
        LocalEventsManager.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            getCellLayout().stopListeningWidgetEvents();
            //orientationListener.disable();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCellLayout().startListeningWidgetEvents();
        //orientationListener.enable();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                closeDrawer();
                return true;
            case KeyEvent.KEYCODE_MENU:
                startActivity(new Intent(this, SettingsActivity.class));
                /*PopupMenu menu = new PopupMenu(this, getCellLayout());
                menu.inflate(R.menu.main_popup_menu);
                menu.show();*/
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void closeDrawer() {
        DrawerLayout drawerLayout = getDrawerLayout();
        closeDrawer(drawerLayout, findViewById(R.id.applicationListDrawer));
        //closeDrawer(drawerLayout, findViewById(R.id.widgetListDrawer));
    }

    private void closeDrawer(DrawerLayout layout, View drawer) {
        if (layout.isDrawerOpen(drawer)) {
            layout.closeDrawer(drawer);
        }
    }

    @Override
    public void onEvent(Event event) {
        switch (event.getCategory())
        {
            case FOLLOW_ROTATION:
                getCellLayout().setFollowRotation((Boolean) event.getData(), true);
                break;
            case DOCK_SIZE:
                getCellLayout().setGridSize((Integer) event.getData(), true);
                break;
        }
    }

    private CellLayout getCellLayout() {
        return (CellLayout) findViewById(R.id.applicationDock);
    }

    private DrawerLayout getDrawerLayout() {
        return (DrawerLayout) findViewById(R.id.rootLayout);
    }
}
