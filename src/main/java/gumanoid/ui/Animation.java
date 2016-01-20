package gumanoid.ui;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action2;
import rx.subscriptions.Subscriptions;

/**
 * Created by Gumanoid on 21.01.2016.
 */
public class Animation<TObject, TProperty> {
    private final Action2<TObject, TProperty> setProperty;
    private Subscription subscription = Subscriptions.empty();

    protected Animation(Action2<TObject, TProperty> setProperty) {
        this.setProperty = setProperty;
    }

    public static <TObject, TProperty> Animation<TObject, TProperty> create(Action2<TObject, TProperty> setProperty) {
        return new Animation<>(setProperty);
    }

    public void animate(TObject object, Observable<TProperty> source) {
        subscription.unsubscribe();
        subscription = source.subscribe(value -> setProperty.call(object, value));
    }

    public void stopAnimation() {
        subscription.unsubscribe();
    }
}
