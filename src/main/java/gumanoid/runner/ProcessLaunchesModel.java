package gumanoid.runner;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Represents a series of process launches (generally with different arguments)
 *
 * Created by Gumanoid on 19.01.2016.
 */
public class ProcessLaunchesModel {
    private final PublishSubject<ProcessBuilder> starting = PublishSubject.create();
    private final PublishSubject<ProcessModel> started = PublishSubject.create();
    private final PublishSubject<ProcessModel> finished = PublishSubject.create();

    private ProcessModel process;

    public void start(ProcessBuilder builder) {
        try {
            starting.onNext(builder);
            process = new ProcessModel(builder.start());
            started.onNext(process);
            process.start();
            finished.onNext(process);
            process = null;
        } catch (Exception e) {
            started.onError(e);
        }
    }

    public void cancel() {
        if (process != null) {
            process.cancel();
        }
    }

    public Observable<ProcessBuilder> onStarting() {
        return starting.asObservable();
    }

    public Observable<ProcessModel> onStarted() {
        return started.asObservable();
    }

    public Observable<ProcessModel> onFinished() {
        return finished.asObservable();
    }
}
