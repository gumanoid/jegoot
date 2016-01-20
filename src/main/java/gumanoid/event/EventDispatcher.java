package gumanoid.event;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Dispatches events to appropriate handlers based on
 * event runtime type (i. e. implements late binding)
 *
 * Created by Gumanoid on 18.01.2016.
 */
//todo move to separate package
public class EventDispatcher<BaseType> implements Consumer<BaseType> {
    private final Multimap<Class, Consumer> handlers = HashMultimap.create();
    private final Consumer<BaseType> defaultHandler;

    public EventDispatcher(Consumer<BaseType> defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public <TEvent extends BaseType> void addHandler(Class<? extends TEvent> clazz, Consumer<TEvent> handler) {
        handlers.put(clazz, handler);
    }

    @Override
    public void accept(BaseType event) {
        Collection<Consumer> handlersForClass = handlers.get(event.getClass());
        if (handlersForClass.isEmpty()) {
            defaultHandler.accept(event);
        } else {
            //correctness of the next call is enforced by addHandler,
            //so it's safe to drop type information here
            //noinspection unchecked
            handlersForClass.forEach(h -> h.accept(event));
        }
    }
}
