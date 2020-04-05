package com.raad.converter.worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;


public class AsyncDALTaskExecutor {

    public Logger logger = LogManager.getLogger(AsyncDALTaskExecutor.class);

    private LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

    private ThreadPoolExecutor threadPool;

    public void addTask(Runnable task) {
        try {
            logger.debug("Submitting Task of type : " + task.getClass().getCanonicalName());
            this.threadPool.submit(task);
        } catch (RejectedExecutionException ex) {
            logger.error("*********** Exception *********** " +  ex.getMessage());
            logger.error("Failed to submit Task in queue " + ex.getMessage());
        }
    }

    public AsyncDALTaskExecutor(Integer minThreads, Integer maxThreads, Integer threadLifeInMins) {
        logger.info(">============AsyncDALTaskExecutor Start Successful============<");
        this.threadPool = new ThreadPoolExecutor(minThreads, maxThreads,threadLifeInMins, TimeUnit.MINUTES, queue);
        // Add Task back in the pool after waiting for 1 sec
        this.threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
                logger.error("Task Rejected : " + task.getClass().getCanonicalName());
                executor.execute(task);
                executor.shutdown();
            }
        });
        (new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("AsyncDAL Active No Threads: " + threadPool.getActiveCount() + " Core no of Threads: " +
                threadPool.getCorePoolSize() + " Current no of threads: " +
                threadPool.getPoolSize() + " current Queue Size: " + queue.size() + " Max allowed Threads: " +
                        threadPool.getMaximumPoolSize());
            }
        }, 5 * 60 * 1000, 60000);
        logger.info(">============AsyncDALTaskExecutor End Successful============<");
    }
}
