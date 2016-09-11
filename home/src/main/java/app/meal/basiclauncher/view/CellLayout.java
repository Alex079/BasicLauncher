package app.meal.basiclauncher.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import app.meal.basiclauncher.R;
import app.meal.basiclauncher.helper.ApplicationData;
import app.meal.basiclauncher.helper.BasicAppWidgetHost;
import app.meal.basiclauncher.helper.ItemData;
import app.meal.basiclauncher.helper.WidgetData;

public class CellLayout extends RelativeLayout {

    public CellLayout(Context context) {
        super(context);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CellLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private int gridSize = getResources().getInteger(R.integer.dock_size_default);
    private int cellSize = 0;
    private int gridWidth = 0;
    private int gridHeight = 0;
    private boolean followRotation = getResources().getBoolean(R.bool.icons_follow_default);
    private final AppWidgetHost appWidgetHost = isInEditMode() ? null
            : new BasicAppWidgetHost(getContext(), R.integer.app_widget_host_id);

    private View viewBeingDragged;
    private Drawable originalBackground;
    private Point previousPoint;
    private int previousPosition;
    private int currentPosition;

    private final Map<Integer, ItemData> layoutState = new HashMap<>();

    public void setFollowRotation(boolean followRotation, boolean layout) {
        if (this.followRotation != followRotation) {
            this.followRotation = followRotation;
            if (layout) {
                redoLayout();
            }
        }
    }

    public void setGridSize(int newGridSize, final boolean layout) {
        if (gridSize != newGridSize) {
            gridSize = newGridSize;
            post(new Runnable() {
                @Override public void run() {
                    int width = getMeasuredWidth();
                    int height = getMeasuredHeight();
                    cellSize = Math.min(width, height) / gridSize;
                    gridWidth = width / cellSize;
                    gridHeight = height / cellSize;
                    if (layout) {
                        redoLayout();
                    }
                }
            });
        }
    }

    public int allocateAppWidgetId() {
        return appWidgetHost.allocateAppWidgetId();
    }

    public void deleteAppWidgetId(int id) {
        appWidgetHost.deleteAppWidgetId(id);
    }

    public void placeWidget(int id) {
        WidgetData data = new WidgetData(id, AppWidgetManager.getInstance(getContext()).getAppWidgetInfo(id));
        int position = choosePosition();
        layoutState.put(position, data);
        final AppWidgetHostView view = getTargetView(data);
        view.setTag(R.integer.tag_position, position);
        final Point p = getPoint(position);
        view.setVisibility(INVISIBLE);
        addView(view);
        view.post(new Runnable() {
            @Override public void run() {
                resizeView(view);
                repositionView(view, p, false);
                view.setVisibility(VISIBLE);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (cellSize == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(
                            (MeasureSpec.getSize(widthMeasureSpec) / cellSize) * cellSize,
                            MeasureSpec.getMode(widthMeasureSpec)),
                    MeasureSpec.makeMeasureSpec(
                            (MeasureSpec.getSize(heightMeasureSpec) / cellSize) * cellSize,
                            MeasureSpec.getMode(heightMeasureSpec)));
        }
        cellSize = Math.min(getMeasuredWidth(), getMeasuredHeight()) / gridSize;
        gridWidth = getMeasuredWidth() / cellSize;
        gridHeight = getMeasuredHeight() / cellSize;
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                ClipDescription clipDescription;
                CharSequence label;
                String description;
                if ((clipDescription = event.getClipDescription()) == null ||
                    (label = clipDescription.getLabel()) == null ||
                    (!(description = label.toString()).contains(getContext().getPackageName())) )
                {
                    return super.onDragEvent(event);
                }
                if (layoutState.size() > getGridLimit()) {
                    Toast.makeText(getContext(), R.string.home_screen_is_full, Toast.LENGTH_LONG).show();
                    return super.onDragEvent(event);
                }
                onDragStarted(event, description.contains(getContext().getString(R.string.copy_action)));
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                onDragEntered();
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                onDragMoved(event);
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                onDragExited();
                break;
            case DragEvent.ACTION_DROP:
                onDragDropped();
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                onDragEnded();
                break;
            default:
                return super.onDragEvent(event);
        }
        return true;
    }

    private void onDragStarted(DragEvent event, boolean isCopy) {
        setBackgroundColor(getResources().getColor(R.color.dropZone));
        View sourceView = (View) event.getLocalState();
        if (isCopy) {
            viewBeingDragged = getTargetView((ApplicationData) sourceView.getTag(R.integer.tag_app_data));
            previousPosition = choosePosition();
            viewBeingDragged.setTag(R.integer.tag_position, previousPosition);
            currentPosition = -1;
        } else {
            viewBeingDragged = sourceView;
            previousPosition = (Integer) viewBeingDragged.getTag(R.integer.tag_position);
            currentPosition = previousPosition;
        }
        originalBackground = viewBeingDragged.getBackground();
        viewBeingDragged.setBackgroundColor(getResources().getColor(R.color.dragItem));
        previousPoint = getPoint(previousPosition);
    }

    private void onDragEntered() {
        if (viewBeingDragged.getParent() == null) {
            addView(viewBeingDragged);
            resizeView(viewBeingDragged);
        }
    }

    private void onDragMoved(DragEvent event) {
        int x = (int) event.getX() / cellSize;
        int y = (int) event.getY() / cellSize;
        int newPosition = getPosition(x, y);
        if (currentPosition != newPosition) {
            repositionView(viewBeingDragged, x, y, currentPosition >=0);
            if (layoutState.containsKey(newPosition)) {
                repositionView(getViewByPosition(newPosition), previousPoint, true);
            }
            if (layoutState.containsKey(currentPosition)) {
                Point p = getPoint(currentPosition);
                repositionView(getViewByPosition(currentPosition), p, true);
            }
            currentPosition = newPosition;
        }
    }

    private void onDragExited() {
        if (layoutState.containsKey(currentPosition)) {
            repositionView(getViewByPosition(currentPosition), getPoint(currentPosition), true);
        }
        removeView(viewBeingDragged);
        currentPosition = -1;
    }

    private void onDragDropped() {
        if (layoutState.containsKey(currentPosition)) {
            getViewByPosition(currentPosition).setTag(R.integer.tag_position, previousPosition);
            layoutState.put(previousPosition, layoutState.remove(currentPosition));
        }
        viewBeingDragged.setTag(R.integer.tag_position, currentPosition);
        layoutState.put(currentPosition, (ItemData) viewBeingDragged.getTag(R.integer.tag_app_data));
    }

    private void onDragEnded() {
        setBackgroundColor(Color.TRANSPARENT);
        if (viewBeingDragged != null) {
            viewBeingDragged.setBackgroundDrawable(originalBackground);
            ItemData data = (ItemData) viewBeingDragged.getTag(R.integer.tag_app_data);
            if (currentPosition < 0 && data instanceof WidgetData) {
                deleteAppWidgetId(((WidgetData) data).getId());
            }
            viewBeingDragged = null;
        }
    }

    private int choosePosition() {
        int result;
        int limit = getGridLimit();
        for (result = 0; layoutState.containsKey(result) && result <= limit; result++);
        return result > limit ? -1 : result;
    }

    private View getViewByPosition(int number) {
        int n = getChildCount();
        for (int i = 0; i < n; i++) {
            View view = getChildAt(i);
            if (number == (Integer) view.getTag(R.integer.tag_position)) {
                return view;
            }
        }
        return null;
    }

    private int getPosition(int x, int y) {
        if (followRotation) {
            switch (((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_90:
                    return gridHeight * x + (gridHeight - 1 - y);
                case Surface.ROTATION_180:
                    return (gridWidth - 1 - x) + gridWidth * (gridHeight - 1 - y);
                case Surface.ROTATION_270:
                    return gridHeight * (gridWidth - 1 - x) + y;
                case Surface.ROTATION_0:
                default:
            }
        }
        return x + gridWidth * y;
    }

    private Point getPoint(int position) {
        if (followRotation) {
            switch (((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_90:
                    return new Point(position / gridHeight, gridHeight - 1 - position % gridHeight);
                case Surface.ROTATION_180:
                    return new Point(gridWidth - 1 - position % gridWidth, gridHeight - 1 - position / gridWidth);
                case Surface.ROTATION_270:
                    return new Point(gridWidth - 1 - position / gridHeight, position % gridHeight);
                case Surface.ROTATION_0:
                default:
            }
        }
        return new Point(position % gridWidth, position / gridWidth);
    }

    private int getGridLimit() {
        return (getMeasuredWidth()/cellSize) * (getMeasuredHeight()/cellSize) - 1;
    }

    private void redoLayout() {
        int limit = getGridLimit();
        int n = getChildCount();
        for (int i = 0; i < n; i++) {
            View v = getChildAt(i);
            int position = (Integer) v.getTag(R.integer.tag_position);
            if (position > limit) {
                int newPosition = choosePosition();
                if (newPosition < 0) {
                    layoutState.remove(position);
                    Object data = v.getTag(R.integer.tag_app_data);
                    if (data instanceof WidgetData) {
                        deleteAppWidgetId(((WidgetData) data).getId());
                    }
                    removeView(v);
                    return;
                }
                v.setTag(R.integer.tag_position, newPosition);
                layoutState.put(newPosition, layoutState.remove(position));
                position = newPosition;
            }
            resizeView(v);
            repositionView(v, getPoint(position), false);
        }
    }

    private AppWidgetHostView getTargetView(@NonNull WidgetData widgetData) {
        AppWidgetProviderInfo appWidgetInfo = widgetData.getInfo();
        AppWidgetHostView view = appWidgetHost.createView(getContext(), widgetData.getId(), appWidgetInfo);
        view.setAppWidget(widgetData.getId(), appWidgetInfo);
        view.setTag(R.integer.tag_app_data, widgetData);
        int width = appWidgetInfo.minWidth / cellSize + 1;
        int height = appWidgetInfo.minHeight / cellSize + 1;
        view.setTag(R.integer.tag_size, new Point(width, height));
        view.setOnLongClickListener(new OnLongClickListener() {
            @Override public boolean onLongClick(View view) {
                view.startDrag(
                        ClipData.newPlainText(getContext().getString(R.string.move_action)+"."+getContext().getPackageName(), null),
                        new View.DragShadowBuilder(/*view*/),
                        view, 0);
                layoutState.remove(view.getTag(R.integer.tag_position));
                return false;
            }
        });
        if (followRotation && width != height) {
            view.setPivotX(cellSize/2f);
            view.setPivotY(cellSize/2f);
            switch (((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_90:
                    view.setRotation(270);
                    break;
                case Surface.ROTATION_270:
                    view.setRotation(90);
                    break;
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
            }
        }
        //TODO obtain update info, perform update through local event
        return view;
    }

    private View getTargetView(@NonNull ApplicationData applicationData) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_application_dock, this, false);
        PackageManager packageManager = getContext().getPackageManager();
        TextView textView = (TextView) view.findViewById(R.id.applicationListItemLabel);
        textView.setMaxWidth(cellSize);
        textView.setText(applicationData.getLabel(packageManager));
        ((ImageView) view.findViewById(R.id.applicationListItemIcon))
                .setImageDrawable(applicationData.getIcon(packageManager));
        view.setTag(R.integer.tag_app_data, applicationData);
        view.setTag(R.integer.tag_size, new Point(1, 1));
        view.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                ApplicationData data = (ApplicationData) v.getTag(R.integer.tag_app_data);
                try {
                    getContext().startActivity(data.getIntent());
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), data.getPackageName(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        view.setOnLongClickListener(new OnLongClickListener() {
            @Override public boolean onLongClick(View view) {
                view.startDrag(
                        ClipData.newPlainText(getContext().getString(R.string.move_action)+"."+getContext().getPackageName(), null),
                        new View.DragShadowBuilder(/*view*/),
                        view, 0);
                layoutState.remove(view.getTag(R.integer.tag_position));
                return false;
            }
        });
        return view;
    }

    private void resizeView(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        Point size = (Point) view.getTag(R.integer.tag_size);
        layoutParams.width = size.x * cellSize;
        layoutParams.height = size.y * cellSize;
        view.setLayoutParams(layoutParams);
    }

    private void repositionView(View view, Point p, boolean animate) {
        repositionView(view, p.x, p.y, animate);
    }

    private void repositionView(View view, int x, int y, boolean animate) {
        if (followRotation && (int) view.getRotation() == 0) {
            Point size = (Point) view.getTag(R.integer.tag_size);
            switch (((Activity) getContext()).getWindowManager().getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0:
                    break;
                case Surface.ROTATION_90:
                    y -= size.y-1;
                    break;
                case Surface.ROTATION_180:
                    x -= size.x-1;
                    y -= size.y-1;
                    break;
                case Surface.ROTATION_270:
                    x -= size.x-1;
                    break;
            }
        }
        x *= cellSize;
        y *= cellSize;
        if (animate) {
            view.animate()
                    .setDuration(getResources().getInteger(R.integer.dock_animation_duration_default))
                    .translationX(x)
                    .translationY(y)
                    .start();
        } else {
            view.setX(x);
            view.setY(y);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        writePreferences();
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        readPreferences();
    }

    public void initializeState() {
        readPreferences();
    }

    private void writePreferences() {
        Set<String> s = new HashSet<>();
        for (Map.Entry<Integer, ItemData> e : layoutState.entrySet()) {
            ItemData value = e.getValue();
            if (value != null) {
                s.add(e.getKey() + "|" + value);
            }
        }
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putStringSet(getClass().getName(), s).apply();
    }

    private void readPreferences() {
        layoutState.clear();
        Set<String> s = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getStringSet(getClass().getName(), Collections.<String>emptySet());
        for (String line : s) {
            String[] split = SPLITTER.split(line, 2);
            layoutState.put(Integer.parseInt(split[0]), ItemData.fromString(split[1], getContext()));
        }
        post(new Runnable() {
            @Override public void run() {
                for (Map.Entry<Integer, ItemData> e : layoutState.entrySet()) {
                    final View item;
                    final Point p = getPoint(e.getKey());
                    if (e.getValue() instanceof ApplicationData) {
                        item = getTargetView((ApplicationData) e.getValue());
                    } else if (e.getValue() instanceof WidgetData) {
                        item = getTargetView((WidgetData) e.getValue());
                    } else {
                        continue;
                    }
                    item.setTag(R.integer.tag_position, e.getKey());
                    item.setVisibility(INVISIBLE);
                    addView(item);
                    item.post(new Runnable() {
                        @Override public void run() {
                            resizeView(item);
                            repositionView(item, p, false);
                            item.setVisibility(VISIBLE);
                        }
                    });
                }
            }
        });
    }

    private static final Pattern SPLITTER = Pattern.compile("\\|");
}
