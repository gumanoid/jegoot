package gumanoid.runner;

import rx.Observable;
import rx.subjects.BehaviorSubject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Represents state of some launched external process. Allows to
 * start process, cancel process, listen to process output, and
 * get notification when process started and stopped
 *
 * Created by Gumanoid on 17.01.2016.
 */
public class ProcessModel {
    private final BehaviorSubject<String> output = BehaviorSubject.create();
    private final BehaviorSubject<Integer> exitCode = BehaviorSubject.create();

    private final Process process;

    public ProcessModel(Process process) {
        this.process = process;
    }

    public void start() throws Exception {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null && output.hasObservers()) {
                output.onNext(line);
            }
            output.onCompleted();
        } //todo pass IOExceptions to output's observers?

        exitCode.onNext(process.waitFor()); //todo pass waitFor's exceptions to observers?
        exitCode.onCompleted();
    }

    public void cancel() {
        process.destroyForcibly();
    }

    public Observable<String> getOutput() {
        return output.asObservable();
    }

    public Observable<Integer> getExitCode() {
        return exitCode.asObservable();
    }
}
