package com.github.mauro1855.ocrservice.controller;

import com.github.mauro1855.ocrservice.domain.OCRRequest;
import com.github.mauro1855.ocrservice.service.OCRRequestService;

import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by mauro1855 on 01/12/2016.
 */
@RestController
@RequestMapping("/ocr")
public class OCRController {

  private static Logger logger = LoggerFactory.getLogger(OCRController.class);

  private static final String PDF_MIMETYPE = "application/pdf";

  @Autowired
  private OCRRequestService ocrRequestService;

  @RequestMapping(value = "/request", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<String> registerNewOCRRequest(@RequestParam(required = false) String requestorReference,
                                              @RequestParam Short priority,
                                              @RequestParam String callbackEndpoint,
                                              @RequestParam HttpMethod callbackMethod,
                                              @RequestParam MultipartFile file) throws IOException {

    JSONObject toReturn = new JSONObject();

    // gets mimetype of file and checks if it is a PDF
    // if it is not, the request to OCR is not made
    String mimetype = file.getContentType();
    logger.debug("MimeType validation: is {}", mimetype);
    if(!mimetype.equalsIgnoreCase(PDF_MIMETYPE)){
      String errorMsg = "Impossible to process file: it is not a .pdf";
      logger.error(errorMsg);
      toReturn.put("success", false);
      toReturn.put("message", errorMsg);

      return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(toReturn.toString(1));
    }

    // creates a new OCR request an registers it
    OCRRequest newRequest = new OCRRequest(requestorReference, callbackEndpoint, callbackMethod, priority, file.getBytes());
    Long requestId = ocrRequestService.registerNewOCRRequest(newRequest);

    logger.info("Registered new request {} with priority {}", requestId, priority);

    if(requestId == null){
      // if the request Id is null, then there was an unexpected problem, throw 503
      toReturn.put("success", false);
      toReturn.put("message", "It is not possible to fulfill the request at this time");
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(toReturn.toString(1));
    }else{
      // else the request was registered in the DB
      toReturn.put("success", true);
      toReturn.put("message", "The request was accepted");
      toReturn.put("requestorReference", requestorReference);
      toReturn.put("requestId", requestId);
      toReturn.put("requestToken", newRequest.getToken());
    }

    // Return 203 Accepted indicating the request will be processed
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(toReturn.toString(1));

  }
}

