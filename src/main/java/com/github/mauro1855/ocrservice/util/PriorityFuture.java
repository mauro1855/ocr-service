package com.github.mauro1855.ocrservice.util;

import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by mauro1855 on 01/12/2016.
 */
public class PriorityFuture<T> implements RunnableFuture<T> {

  private RunnableFuture<T> src;
  private int priority;
  private Date date;

  public PriorityFuture(RunnableFuture<T> other, int priority, Date date) {
    this.src = other;
    this.priority = priority;
    this.date = date;
  }

  public int getPriority() {
    return priority;
  }

  public Date getDate() {
    return date;
  }

  public boolean cancel(boolean mayInterruptIfRunning) {
    return src.cancel(mayInterruptIfRunning);
  }

  public boolean isCancelled() {
    return src.isCancelled();
  }

  public boolean isDone() {
    return src.isDone();
  }

  public T get() throws InterruptedException, ExecutionException {
    return src.get();
  }

  public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return src.get();
  }

  public void run() {
    src.run();
  }

  // Comparator of priorities and start dates of Runnable tasks
  public static Comparator<Runnable> comparator = (o1, o2) -> {
    if (o1 == null && o2 == null)
      return 0;
    else if (o1 == null)
      return -1;
    else if (o2 == null)
      return 1;
    else {
      int p1 = ((PriorityFuture<?>) o1).getPriority();
      int p2 = ((PriorityFuture<?>) o2).getPriority();

      if(p1 != p2){
        return p1 < p2 ? 1 : -1;
      }else{
        return ((PriorityFuture<?>) o1).getDate().after(((PriorityFuture<?>) o2).getDate()) ? 1 : -1;
      }
    }
  };
}
