package com.zl.test;

import com.zl.fastlock.FastLock;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
@Threads(500)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class TestFastLock {

    private FastLock fastLock;

    private ReentrantLock reentrantLock;

    private int fastLockCounter;

    private int rawCounter;

    private int reentrantLockCounter;

    @Benchmark
    @Group("fastlock")
    @GroupThreads(4)
    public void fastLockedOp() {
        try {
            fastLock.lock();
            fastLockCounter ++;
        } finally {
            fastLock.unlock();
        }
//        System.out.println("fastlock=" + fastLockCounter);
    }

    @Benchmark
    @Group("reentrantLock")
    @GroupThreads(4)
    public void reentrantLockOp() {
        try {
            reentrantLock.lock();
            reentrantLockCounter ++;
        } finally {
            reentrantLock.unlock();
        }
//        System.out.println("reentrantLock=" + reentrantLockCounter);
    }

    @Benchmark
    @Group("synchronized")
    @GroupThreads(4)
    public void synchronizedOp() {
        synchronized (this) {
            rawCounter ++;
        }
//        System.out.println("synchronized=" + rawCounter);
    }

    @Setup
    public void prepare() {
        fastLock = new FastLock();
        reentrantLock = new ReentrantLock();
        fastLockCounter = 0;
        rawCounter = 0;
        reentrantLockCounter = 0;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TestFastLock.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }


}
