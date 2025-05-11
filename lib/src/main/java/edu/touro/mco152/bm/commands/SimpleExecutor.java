package edu.touro.mco152.bm.commands;

import edu.touro.mco152.bm.observer.BenchmarkObserver;
import edu.touro.mco152.bm.observer.BenchmarkSubject;
import edu.touro.mco152.bm.persist.DiskRun;

import java.util.ArrayList;
import java.util.List;

public class SimpleExecutor implements BenchmarkSubject {

    private static final SimpleExecutor instance = new SimpleExecutor();

    public static SimpleExecutor getInstance() {
        return instance;
    }

    private SimpleExecutor() {}


    public void executeCommand(Command command) {
        command.execute();
    }

    private final List<BenchmarkObserver> observers = new ArrayList<>();

    @Override
    public void registerObserver(BenchmarkObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(BenchmarkObserver o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(DiskRun run) {
        for (BenchmarkObserver o : observers) {
            o.update(run);
        }
    }
}
