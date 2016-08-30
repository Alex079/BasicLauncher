package app.meal.basiclauncher.helper;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class BasicAppWidgetHost extends android.appwidget.AppWidgetHost {

    public BasicAppWidgetHost(Context context, int hostId) {
        super(context, hostId);
    }

    @Override
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
        return new AppWidgetHostView(context) {

            private boolean isLongClickPerformed = false;
            private Runnable setFlag = null;

            @Override public boolean onInterceptTouchEvent(MotionEvent event) {
                if (isLongClickPerformed) {
                    isLongClickPerformed = false;
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setFlag();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        unsetFlag();
                        break;
                    default:
                }
                return super.onInterceptTouchEvent(event);
            }

            private void setFlag() {
                if (setFlag == null) {
                    setFlag = new Runnable() {
                        @Override public void run() {
                            if (performLongClick()) {
                                isLongClickPerformed = true;
                            }
                        }
                    };
                }
                postDelayed(setFlag, ViewConfiguration.getLongPressTimeout());
            }

            private void unsetFlag() {
                removeCallbacks(setFlag);
                isLongClickPerformed = false;
            }
        };
    }
}
