package customLock;

import java.util.HashMap;
import java.util.Map;

/**
 * A custom reentrant read/write lock that allows:
 * 1) Multiple readers (when there is no writer). Any thread can acquire multiple read locks
 * (if nobody is writing).
 * 2) One writer (when nobody else is writing or reading).
 * 3) A writer is allowed to acquire a read lock while holding the write lock.
 * 4) A writer is allowed to acquire another write lock while holding the write lock.
 * 5) A reader can NOT acquire a write lock while holding a read lock.
 *
 * Use ReentrantReadWriteLockTest to test this class.
 * The code is modified from the code of Prof. Rollins.
 */
public class ReentrantReadWriteLock {
    // FILL IN CODE:
    // Add instance variables:
    // for each threadId, you may want to store the number of read locks and write locks currently held
    private int readCount =0;
    private int writerCount = 0;
    private Map<Long, Integer> mapReader;
    private Map<Long, Integer> mapWriter;


    /**
     * Constructor for ReentrantReadWriteLock
     */
    public ReentrantReadWriteLock() {
        mapReader = new HashMap<>();
        mapWriter = new HashMap<>();
    }

    /**
     * Return true if the current thread holds a read lock.
     *
     * @return true or false
     */
    public synchronized boolean isReadLockHeldByCurrentThread() {

        long currentThreadId = Thread.currentThread().getId();
        int reader = mapReader.get(currentThreadId);
        if(reader==0)
            return false;
        return true;
    }

    /**
     * Return true if the current thread holds a write lock.
     *
     * @return true or false
     */
    public synchronized boolean isWriteLockHeldByCurrentThread() {

        long currentThreadId = Thread.currentThread().getId();
        int writer = mapWriter.get(currentThreadId);
        if(writer==0)
            return false;
        return true;

    }

    /**
     * Non-blocking method that attempts to acquire the read lock and acquires it, if it can.
     * Returns true if successful.
     * Checks conditions (whether it can acquire the read lock), and if they are true,
     * acquires the lock (updates readers info).
     *
     * Note that if conditions are false (can not acquire the read lock at the moment), this method
     * does NOT wait, just returns false
     * @return
     */
    public synchronized boolean tryAcquiringReadLock() {
        // write down carefully the conditions when you can get a readLock?
        // if it is true, it should acquire the lock which means modify the map.
        // we change maps return a lock.
        // it should acquiring lock if it can.
        // if you are not able to get lock then do not wait, just return false.

        // if I am not a current reader, if the key does not exist??
        long currentThreadId = Thread.currentThread().getId();
        mapReader.putIfAbsent(currentThreadId, 0);

//        boolean hasOtherWriter = false;
//        for (long key : mapWriter.keySet()) {
//            if(key != currentThreadId && mapWriter.get(key) != 0)
//                hasOtherWriter = true;
//        }
//        //not need to loop.
//        if (!hasOtherWriter) {
//            int reader = mapReader.get(currentThreadId);
//            reader++;
//            mapReader.put(currentThreadId, reader);
//            return true;
//        }
        if (mapWriter.containsKey(currentThreadId) || writerCount == 0) {
            int reader = mapReader.get(currentThreadId);
            reader++;
            mapReader.put(currentThreadId, reader);
            readCount++;
            return true;
        }
        return false;
    }



    /**
     * Non-blocking method that attempts to acquire the write lock, and acquires it, if it is available.
     * Returns true if successful.
     * Checks conditions (whether it can acquire the write lock), and if they are true,
     * acquires the lock (updates writers info).
     *
     * Note that if conditions are false (can not acquire the write lock at the moment), this method
     * does NOT wait, just returns false
     *
     * @return
     */
    public synchronized boolean tryAcquiringWriteLock(){
        long currentThreadId = Thread.currentThread().getId();

        // if I am not a current writer, the mapWriter key does not exist.
        mapWriter.putIfAbsent(currentThreadId,0);

//        // no other writer except current one.
//        boolean hasOtherWriter = false;
//        for (long key : mapWriter.keySet()) {
//            if(key != currentThreadId && mapWriter.get(key) != 0)
//                hasOtherWriter = true;
//        }
//        boolean hasReader = false;
//        for (long key : mapReader.keySet()) {
//            if( mapReader.get(key) != 0)
//                hasReader = true;
//        }
//
//        if(!hasReader && !hasOtherWriter) {
//            int writer = mapWriter.get(currentThreadId);
//            writer++;
//            mapWriter.put(currentThreadId,writer);
//            return true;
//        }
        if( (readCount == 0 && writerCount == 0) || (readCount == 0 && mapWriter.containsKey(currentThreadId))) {
            int writer = mapWriter.get(currentThreadId);
            writer++;
            mapWriter.put(currentThreadId,writer);
            writerCount++;
            return true;
        }
        return false;
    }

    /**
     * Blocking method that will return only when the read lock has been
     * acquired.
     * Calls tryAcquiringReadLock, and as long as it returns false, waits.
     * Catches InterruptedException.
     *
     */
    public synchronized void lockRead() {
        // FILL IN CODE
        // can have while loop just keep calling tryAcquiringReadLock() method.
        while(!tryAcquiringReadLock()){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Releases the read lock held by the calling thread. Other threads might
     * still be holding read locks.
     * If no more readers after unlocking, calls notifyAll().
     */
    public synchronized void unlockRead() {
        // FILL IN CODE

        long currentThreadId = Thread.currentThread().getId();
        if(!mapReader.containsKey(currentThreadId))
            return;

        int reader = mapReader.get(currentThreadId);
        reader--;
        readCount--;
        mapReader.put(currentThreadId,reader);


        boolean hasReader = false;
        for(int r: mapReader.values()){
            if(r !=0) {
                hasReader = true;
                break;
            }
        }
        // when count is 1, I can just remove from map, then I can check the size of the map bigger than 0 or not.
        if(!hasReader)
            this.notifyAll(); // it's purpose is telling other waiting threads get in.
        // you need update the map.

    }

    /**
     * Blocking method that will return only when the write lock has been
     * acquired.
     * Calls tryAcquiringWriteLock, and as long as it returns false, waits.
     * Catches InterruptedException.
     */
    public synchronized void lockWrite() {
        // FILL IN CODE
        while(!tryAcquiringWriteLock()){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    // how can I take care of allowing one thread to hold multiple write lock and read lock.???

    /**
     * Releases the write lock held by the calling thread. The calling thread
     * may continue to hold a read lock.
     * If the number of writers becomes 0, calls notifyAll.
     */

    public synchronized void unlockWrite() {
        // FILL IN CODE
        // if have 5 locks, unlock 5 times.
        // keep track of this specific thread has,
        // it might have multiple locks. unlock only release one of time.
        long currentThreadId = Thread.currentThread().getId();
        if(!mapWriter.containsKey(currentThreadId))
            return;

        int writer = mapWriter.get(currentThreadId);
        writer--;
        writerCount--;
        mapWriter.put(currentThreadId,writer);

        boolean hasWriters = false;
        for(int wr: mapWriter.values()){
            if(wr != 0)
                hasWriters = true;
        }
        // another way to do this : set member variable writerCount.
        if(!hasWriters)
            notifyAll(); // notify all waiting threads to get in.
    }
}

