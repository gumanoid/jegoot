package gumanoid.runner;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gumanoid on 19.01.2016.
 */
public class ProcessLaunchesModel {
    private final BehaviorSubject<ProcessModel> started = BehaviorSubject.create();
    private final BehaviorSubject<ProcessModel> finished = BehaviorSubject.create();

    private ProcessModel process;

    public void start(ProcessBuilder builder) {
        try {
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

    public Observable<ProcessModel> onStarted() {
        return started.asObservable();
    }

    public Observable<ProcessModel> onFinished() {
        return finished.asObservable();
    }
}
