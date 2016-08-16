package app.meal.basiclauncher.event;

import java.util.HashSet;
import java.util.Set;

public class LocalEventsManager {

    private LocalEventsManager() {}
    private final Set<Listener> listeners = new HashSet<>();

    public static LocalEventsManager getInstance() {
        return Holder.instance;
    }

    static class Holder {
        static final LocalEventsManager instance = new LocalEventsManager();
    }

    public void send(Event event) {
        for (Listener listener : listeners) {
            listener.onEvent(event);
        }
    }

    public void register(Listener listener) {
        listeners.add(listener);
    }

    public void unregister(Listener listener) {
        listeners.remove(listener);
    }

    public interface Listener {
        void onEvent(Event event);
    }
}
