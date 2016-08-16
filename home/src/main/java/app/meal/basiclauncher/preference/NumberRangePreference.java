package app.meal.basiclauncher.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import app.meal.basiclauncher.R;

public class NumberRangePreference extends Preference {

    public NumberRangePreference(Context context) {
        super(context);
    }

    public NumberRangePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public NumberRangePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NumberRangePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.NumberRangePreference);
        minValue = a.getInt(R.styleable.NumberRangePreference_minValue, getContext().getResources().getInteger(R.integer.number_range_default_min_value));
        maxValue = a.getInt(R.styleable.NumberRangePreference_maxValue, getContext().getResources().getInteger(R.integer.number_range_default_max_value));
        longStep = a.getInt(R.styleable.NumberRangePreference_longStep, getContext().getResources().getInteger(R.integer.number_range_default_long_step));
        a.recycle();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, getContext().getResources().getInteger(R.integer.number_range_default_value));
    }

    private int value;
    private int minValue;
    private int maxValue;
    private int longStep;

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(value) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        if (this.value != value && callChangeListener(value)) {
            this.value = value;
            persistInt(value);
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View currentValue = view.findViewById(R.id.current_value);
        if (currentValue != null && currentValue instanceof TextView) {
            ((TextView) currentValue).setText(String.valueOf(value));
        }
        View buttonUp = view.findViewById(R.id.button_up);
        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (value < maxValue) {
                    setValue(value+1);
                }
            }
        });
        buttonUp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                if (value < maxValue-longStep) {
                    setValue(value+longStep);
                } else if (value < maxValue) {
                    setValue(maxValue);
                }
                return true;
            }
        });
        View buttonDown = view.findViewById(R.id.button_down);
        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (value > minValue) {
                    setValue(value-1);
                }
            }
        });
        buttonDown.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                if (value > minValue+longStep) {
                    setValue(value-longStep);
                } else if (value > minValue) {
                    setValue(minValue);
                }
                return true;
            }
        });
    }
}
