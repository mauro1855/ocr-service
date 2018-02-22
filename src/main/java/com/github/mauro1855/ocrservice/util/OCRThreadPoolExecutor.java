package com.github.mauro1855.ocrservice.util;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by smasue on 12/20/16.
 */
public class OCRThreadPoolExecutor extends ThreadPoolExecutor
{
  public OCRThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int queueInitialSize)
  {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<>(queueInitialSize, PriorityFuture.comparator));
  }

  @Override
  protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value)
  {
    RunnableFuture<T> newTaskFor = super.newTaskFor(runnable, value);
    return new PriorityFuture<>(newTaskFor, ((PriorityRunnable) runnable).getPriority(), ((PriorityRunnable) runnable).getDate());
  }
  
}
