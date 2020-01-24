package com.zl.fastlock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class FastLock implements Lock {

    private final Sync sync;


    public FastLock() {
        sync = new Sync();
    }


    private static class Sync extends AbstractQueuedSynchronizer {

        /**
         * 锁等级：0:自旋锁 1:重量级锁
         */
        private volatile int level = 0;


        /**
         * 最大自旋次数
         */
        private static final int loopCount = 20;


        /**
         * 自旋抢占锁，超过loopCount次后失败
         * @return
         */
        private final boolean compareAndSetStateLoop() {
            // 判断锁是否升级
            if(level == 0) {
                for (int i = 0; i < loopCount; i++) {
                    if (compareAndSetState(0, 1)) {
                        return true;
                    }
                }
                // 自旋10次失败，升级成重量级锁
                level = 1;
            }
            return false;
        }

        final void lock() {
            // 先判断是否可重入
            final Thread current = Thread.currentThread();
            if (current == getExclusiveOwnerThread()){
                int c = getState();
                int nextc = c + 1;
                setState(nextc);
                return;
            }
            // 当前线程不持有锁，开始自旋获取锁
            if (compareAndSetStateLoop()) {
                setExclusiveOwnerThread(Thread.currentThread());
            } else {
                // 自旋获取锁失败，进入排队等待流程
                acquire(1);
            }
        }

        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }



        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }


        protected final boolean tryAcquire(int arg, long nanosTimeout) throws InterruptedException {
            long start = System.nanoTime();
            // 先判断是否可重入
            final Thread current = Thread.currentThread();
            if (current == getExclusiveOwnerThread()){
                acquire(1);
                return true;
            }
            // 当前线程不持有锁，开始自旋获取锁
            if (compareAndSetStateLoop()) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            } else {
                long cost = System.nanoTime() - start;
                if(cost >= nanosTimeout)  {
                    return false;
                }
                // 减去自旋耗时
                return super.tryAcquireNanos(arg, nanosTimeout - cost);

            }
        }

        protected final void acquireInterrupt(int arg) throws InterruptedException {
            // 先判断是否可重入
            final Thread current = Thread.currentThread();
            if (current == getExclusiveOwnerThread()){
                acquire(1);
            }
            // 当前线程不持有锁，开始自旋获取锁
            if (compareAndSetStateLoop()) {
                setExclusiveOwnerThread(Thread.currentThread());
            } else {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                super.acquireInterruptibly(arg);
            }
        }


        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }


        /**
         * Reconstitutes the instance from a stream (that is, deserializes it).
         */
        private void readObject(java.io.ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState(0); // reset to unlocked state
        }
    }


    @Override
    public void lock() {
        sync.lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterrupt(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquire(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }


}
