package app.meal.basiclauncher.event;

public class Event {

    private final Type category;
    private final Object data;

    public Event(Type category, Object data) {
        this.category = category;
        this.data = data;
    }

    public Type getCategory() { return category; }

    public Object getData() { return data; }

    public enum Type {
        CLOCK_FORMAT, CLOCK_FONT_SIZE, DOCK_SIZE, FOLLOW_ROTATION
    }
}
