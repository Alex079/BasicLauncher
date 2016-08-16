package app.meal.basiclauncher.helper;

public class WidgetData implements ItemData {

    private final int id;

    public WidgetData(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
