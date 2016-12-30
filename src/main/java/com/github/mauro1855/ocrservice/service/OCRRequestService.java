package com.github.mauro1855.ocrservice.service;

import com.github.mauro1855.ocrservice.domain.OCRRequest;
import com.github.mauro1855.ocrservice.repository.OCRRequestRepository;
import com.github.mauro1855.ocrservice.util.PriorityFuture;
import com.github.mauro1855.ocrservice.util.PriorityRunnable;
import com.github.mauro1855.ocrservice.worker.OCRRequestWorker;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;

/**
 * Created by pereirat on 01/12/2016.
 */
@Service
public class OCRRequestService {

  private static Logger logger = LoggerFactory.getLogger(OCRRequestService.class);

  public static final int NB_THREADS = 5;
  public static final int QUEUE_INITIAL_SIZE = 20;

  @Autowired
  private OCRRequestWorker ocrRequestWorker;

  @Autowired
  private OCRRequestRepository ocrRequestRepository;

  private static ExecutorService priorityExecutor = new ThreadPoolExecutor(NB_THREADS, NB_THREADS, 0L,
      TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>(QUEUE_INITIAL_SIZE, PriorityFuture.comparator)) {

    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
      RunnableFuture<T> newTaskFor = super.newTaskFor(runnable, value);
      return new PriorityFuture<>(newTaskFor, ((PriorityRunnable) runnable).getPriority(), ((PriorityRunnable) runnable).getDate());
    }
  };


  /**
   * Gets all unprocessed requests and adds them to the
   * priority thread pool
   *
   * This method is run when the program starts
   *
   * @return {void}
   */
  @PostConstruct
  public void restoreRequestsFromLastSession(){

    // Fetches all unprocessed requests from database
    List<OCRRequest> requests = ocrRequestRepository.getAllUnprocessedRequests();

    logger.info("Successfully fetched {} unprocessed requests", requests.size());

    // For every request, submits to the priority thread pool for processing
    for(OCRRequest req : requests){
      priorityExecutor.submit(ocrRequestWorker.getRunnable(req));
    }

  }

  /**
   * Registers new OCR requests in the DB and schedules
   * and submits the OCR task to the priority thread pool
   *
   * @return {Long} new request Id
   */
  public Long registerNewOCRRequest(OCRRequest ocrRequest){

    ocrRequestRepository.createNewRequest(ocrRequest);

    priorityExecutor.submit(ocrRequestWorker.getRunnable(ocrRequest));

    return ocrRequest.getId();

  }

}
