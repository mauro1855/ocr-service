package com.github.mauro1855.ocrservice.worker;

import com.github.mauro1855.ocrservice.domain.OCRRequest;
import com.github.mauro1855.ocrservice.repository.OCRRequestRepository;
import com.github.mauro1855.ocrservice.service.OCRCallbackService;
import com.github.mauro1855.ocrservice.util.PriorityRunnable;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by pereirat on 01/12/2016.
 */
@Service
public class OCRRequestWorker {

  private static Logger logger = LoggerFactory.getLogger(OCRRequestWorker.class);

  private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
  private static final String VAR_DOCKER_SHARE_FOLDER = "!!folder!!";

  @Autowired
  private OCRCallbackService ocrCallbackService;

  @Autowired
  private OCRRequestRepository ocrRequestRepository;

  @Autowired
  private Runtime runtime;

  @Value("${ocr.command}")
  private String ocrCommand;
  @Value("${ocr.extra.commands}")
  private String ocrExtraCommands;
  @Value("${ocr.output.file.prefix.command}")
  private String ocrOutputFilePrefixCommand;

  /**
   * Creates a Runnable task that implements the multiple
   * steps for OCRing the request
   *
   * @param request
   * @return {Runnable} task for thread pool
   */
  public Runnable getRunnable(OCRRequest request) {
    return new PriorityRunnable() {
      @Override
      public int getPriority() {
        return request.getPriority();
      }

      @Override
      public Date getDate() {
        return request.getRequestCreationDate();
      }

      @Override
      public void run() {
        logger.info("Started processing request {} with priority {}", request.getId(), request.getPriority());

        // Marks the request as started in memory
        request.startOCR();

        // the following cycle will try to OCR the file
        // a maximum of 3 times until it gets OCRed
        int i = 0;
        do {

          if(request.getStatusCode() == -1){
            logger.debug("Retrying OCR of request {}", request.getId());
          }

          // processes the request
          processOCRRequest(request);
          i++;
        } while(request.getStatusCode() != 1 && i < 3);

        // marks the request as ended in memory
        request.endOCR();
        logger.info("Finished OCRing request {} in {} ms", request.getId(), request.getOCRDuration());

        // updates request in database and sends reply to client
        ocrRequestRepository.updateRequest(request);
        ocrCallbackService.replyToRequest(request);
      }
    };
  }

  /**
   * Processes the OCR Request, including calling the external tool to
   * perform OCR on file
   *
   * @param request
   * @return {void}
   */
  private void processOCRRequest(OCRRequest request){

    // creates temporary files
    String sourceFileName = request.getId().toString()+"_source.pdf";
    String targetFileName = sourceFileName.replace("_source", "_target");

    File sourceFile = new File(TEMP_DIR + sourceFileName);
    File targetFile = new File(TEMP_DIR + targetFileName);

    try {
      FileUtils.writeByteArrayToFile(sourceFile, request.getFileToOCRByteArray());
    } catch (IOException e) {
      logger.error("Could not create temp file: {}", e.getMessage());
      request.setStatusCode(-1);
      return;
    }
    logger.debug("Created temporary source file: {}", sourceFile.getAbsolutePath());

    String processedSourcePath;
    String processedTargetPath;
    String processedCommand;

    // checks if docker is being run
    // if so, modifications to the command need to be made
    if(ocrCommand.contains("docker")) {
      logger.debug("Docker detected in command for " + SystemUtils.OS_NAME);
      processedCommand = ocrCommand.replace(VAR_DOCKER_SHARE_FOLDER, TEMP_DIR);
      processedSourcePath = sourceFileName;
      processedTargetPath = targetFileName;
    }else{
      processedCommand = ocrCommand;
      processedSourcePath = TEMP_DIR + sourceFileName;
      processedTargetPath = TEMP_DIR + targetFileName;
    }

    String commandToExecute = processedCommand + " " + ocrExtraCommands + " " +
        processedSourcePath + " " + ocrOutputFilePrefixCommand + " " + processedTargetPath;

    try {
      // executes the command to call the external tool
      // waits for a maximum of 5 minutes for the OCR
      logger.debug("Calling external tool to OCR...");
      logger.trace("... on command {}", commandToExecute);
      Process process;
      process = runtime.exec(commandToExecute);
      process.waitFor(5, TimeUnit.MINUTES);

      if(process.exitValue() != 0){
        // if process did not exited with success code then set
        // request status to -1, sets a failure message and returns
        InputStream error = process.getErrorStream();
        String errorString =  IOUtils.toString(error);
        logger.error("An error occurred running the tool for request {}: {}", request.getId(), errorString);
        request.setStatusCode(-1);
        request.setStatusMessage("An internal error occured while processing the request" + request.getId());
        return;
      }
      // otherwise it sets the request as successful and the OCRed content in the request
      request.setOcredFileByteArray(FileUtils.readFileToByteArray(targetFile));
      request.setStatusCode(1);
      request.setStatusMessage("Successfully OCRed");

    } catch (Exception ex){
      // if an exception occurred, then marks the request status code as -1
      // and a failure message
      logger.error("An error occurred running the tool for request {}", request.getId());
      request.setStatusCode(-1);
      request.setStatusMessage("An internal error occured while processing the request " + request.getId());
    }
  }

  public String getOcrCommand() {
    return ocrCommand;
  }

  public void setOcrCommand(String ocrCommand) {
    this.ocrCommand = ocrCommand;
  }

  public String getOcrExtraCommands() {
    return ocrExtraCommands;
  }

  public void setOcrExtraCommands(String ocrExtraCommands) {
    this.ocrExtraCommands = ocrExtraCommands;
  }

  public String getOcrOutputFilePrefixCommand() {
    return ocrOutputFilePrefixCommand;
  }

  public void setOcrOutputFilePrefixCommand(String ocrOutputFilePrefixCommand) {
    this.ocrOutputFilePrefixCommand = ocrOutputFilePrefixCommand;
  }
}
