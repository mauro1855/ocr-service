package com.github.mauro1855.ocrservice.util;

import java.util.Date;

/**
 * Created by mauro1855 on 01/12/2016.
 */
public interface PriorityRunnable extends Runnable {

  int getPriority();
  Date getDate();

}
