package com.github.mauro1855.ocrservice.service;

import com.github.mauro1855.ocrservice.domain.OCRRequest;
import com.github.mauro1855.ocrservice.repository.OCRRequestRepository;
import com.github.mauro1855.ocrservice.worker.OCRRequestWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by mauro1855 on 01/12/2016.
 */
@Service
public class OCRRequestService
{
  private static Logger logger = LoggerFactory.getLogger(OCRRequestService.class);

  @Autowired
  private OCRRequestWorker ocrRequestWorker;

  @Autowired
  private OCRRequestRepository ocrRequestRepository;

  @Autowired
  private ExecutorService priorityExecutor;

  @Resource(name = "isRunningTests")
  private boolean isRunningTests;

  /**
   * Gets all unprocessed requests and adds them to the
   * priority thread pool
   * <p>
   * This method is run when the program starts
   *
   * @return {void}
   */
  @PostConstruct
  public void restoreRequestsFromLastSession()
  {
    if(isRunningTests) return;

    // Fetches all unprocessed requests from database
    List<OCRRequest> requests = ocrRequestRepository.getAllUnprocessedRequests();

    logger.info("Successfully fetched {} unprocessed requests", requests.size());

    // For every request, submits to the priority thread pool for processing
    for (OCRRequest req : requests) {
      priorityExecutor.submit(ocrRequestWorker.getRunnable(req));
    }
  }

  /**
   * Registers new OCR requests in the DB and submits
   * the OCR task to the priority thread pool
   *
   * @return {Long} new request Id
   */
  public Long registerNewOCRRequest(OCRRequest ocrRequest)
  {
    ocrRequestRepository.createNewRequest(ocrRequest);

    // While the request is in the queue, we remove the file bytes
    // (if we have thousands of files in the queue, it's better if
    // the file bytes are not in memory)
    ocrRequest.setFileToOCRByteArray(null);

    // Submit the request to the queue
    priorityExecutor.submit(ocrRequestWorker.getRunnable(ocrRequest));

    return ocrRequest.getId();
  }
}
