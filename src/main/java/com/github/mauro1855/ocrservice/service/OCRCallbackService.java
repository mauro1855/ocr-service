package com.github.mauro1855.ocrservice.service;

import com.github.mauro1855.ocrservice.domain.OCRRequest;
import com.github.mauro1855.ocrservice.repository.OCRRequestRepository;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by pereirat on 01/12/2016.
 */
@Service
public class OCRCallbackService {

  private static Logger logger = LoggerFactory.getLogger(OCRCallbackService.class);

  @Value("${client.username}")
  private String clientUsername;
  @Value("${client.password}")
  private String clientPassword;

  @Autowired
  private OCRRequestRepository ocrRequestRepository;

  @Autowired
  private RestTemplate restTemplate;

  /**
   * Starts a new thread that implements a task that, every
   * 30 seconds+, fetches failed communications from the database
   * and tries to send the OCRed file to the client
   *
   * This method is run when the program starts
   *
   * @return {void}
   */
  @Scheduled(fixedDelay = 30000L)
  public void processReplyQueue(){
      List<OCRRequest> requests = ocrRequestRepository.getAllFailedCommunicationRequests();
      logger.debug("Successfully fetched {} communication failed requests", requests.size());
      Date startDate = new Date();

      // tries to reply to the client of each request
      for(OCRRequest req : requests) {
        replyToRequest(req);
      }
  }

  /**
   * Sends reply to client and updates request in the database
   *
   * @param request to be replied to the client
   * @return {void}
   */
  public void replyToRequest(OCRRequest request){
      sendReply(request);
      ocrRequestRepository.updateRequest(request);
  }

  /**
   * Sends reply to client
   *
   * @param request to be replied to the client
   * @return {void}
   */
  private void sendReply(OCRRequest request){

    logger.debug("Attempting to reply to requester of {}", request.getId());

    // prepares HTTP request to client
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    MultiValueMap<String, Object> requestForm = new LinkedMultiValueMap<>();

    // Prepares authentication
    String credentials = clientUsername + ":" + clientPassword;
    byte[] base64Credentials = Base64.encodeBase64(credentials.getBytes());
    String basicAuthString = new String(base64Credentials);
    headers.add("Authorization", "Basic " + basicAuthString);

    try {
      // creates a form to be sent to the client
      requestForm.add("requestId", request.getId());
      requestForm.add("requestorReference", request.getRequestorReference());
      requestForm.add("requestToken", request.getToken());
      requestForm.add("statusCode", request.getStatusCode());
      requestForm.add("statusMessage", request.getStatusMessage());

      if(request.getStatusCode() == 1) {
        File tempFile = File.createTempFile(request.getToken(), ".pdf");
        FileUtils.writeByteArrayToFile(tempFile, request.getOcredFileByteArray());
        requestForm.add("file", new FileSystemResource(tempFile));
      }

      HttpEntity<MultiValueMap<String, Object>> httpRequest = new HttpEntity<>(requestForm, headers);
      // Replies to the client
      ResponseEntity<String> response = restTemplate.exchange(request.getCallbackEndpoint(), request.getCallbackMethod(), httpRequest, String.class);
      if (response.getStatusCode() == HttpStatus.OK) {
        // if client returned 200 OK, then marks the request as communicated
        request.communicated();
        logger.info("Successfully replied to requester of {}", request.getId());
      } else {
        // if client did not return 200 OK, then marks the request as failed
        request.failedToCommunicated();
        logger.debug("Requestor callback returned {}", response.getStatusCode());
        logger.error("Failed to communicate to requester of {}.", request.getId());
      }
    } catch (RestClientException rce) {
      // if connection failed an exception will be thrown
      // mark request as failed
      request.failedToCommunicated();
      logger.debug("Requestor callback returned {}", rce.getMessage());
      logger.error("Failed to communicate to requester of {}.", request.getId());
    } catch (Exception ex) {
      // if some error happens, mark request as failed
      request.failedToCommunicated();
      logger.debug(ex.toString());
      logger.error("Failed to communicate to requester of {}.", request.getId());
    }
  }

  public void setClientUsername(String clientUsername) {
    this.clientUsername = clientUsername;
  }

  public void setClientPassword(String clientPassword) {
    this.clientPassword = clientPassword;
  }
}
