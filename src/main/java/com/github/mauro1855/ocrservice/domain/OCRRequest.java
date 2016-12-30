package com.github.mauro1855.ocrservice.domain;

import java.util.Arrays;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

/**
 * Created by pereirat on 01/12/2016.
 */
public class OCRRequest implements Serializable {

  private Long id;
  private String requestorReference;
  private String callbackEndpoint;
  private HttpMethod callbackMethod;
  private Short priority;
  private String token;
  private Short statusCode; // 0: Requested; 1: Finished; -1: Error
  private String statusMessage;
  private boolean communicated;
  private boolean communicationAttempted;
  private byte[] fileToOCRByteArray;
  private byte[] ocredFileByteArray;

  private Date requestCreationDate;
  private Date requestCommunicatedDate;
  private Date requestOCRStartDate;
  private Date requestOCREndDate;

  private static final SecureRandom RANDOMIZER = new SecureRandom();

  public OCRRequest(String requestorReference, String callbackEndpoint, HttpMethod callbackMethod, Short priority, byte[] fileToOCRByteArray) {
    this.requestorReference = requestorReference;
    this.callbackEndpoint = callbackEndpoint;
    this.callbackMethod = callbackMethod;
    this.priority = priority;
    this.fileToOCRByteArray = fileToOCRByteArray;
    generateToken();
    this.communicated = false;
    this.communicationAttempted = false;
    this.ocredFileByteArray = null;
    this.requestCreationDate = new Date();
    this.statusCode = 0;
    this.statusMessage = "Request accepted";
  }

  /**
   * Generate random token - generates a random token and sets it
   * token may be useful to clients to validate response with OCR service
   *
   * @return {String} token
   */
  public String generateToken() {
    this.token = new BigInteger(130, RANDOMIZER).toString();
    return token;
  }

  /**
   * Sets the startDate of the OCR
   *
   * @return {void}
   */
  public void startOCR(){
    this.requestOCRStartDate = new Date();
  }

  /**
   * Sets the endDate of the OCR and returns the
   * total duration of the OCR process
   *
   * @return {void}
   */
  public Long endOCR(){
    this.requestOCREndDate = new Date();
    return getOCRDuration();
  }

  /**
   * Calculates the total duration of the OCR
   * based on the start and end dates
   *
   * @return {Long} duration in milliseconds
   */
  public Long getOCRDuration(){
    return requestOCREndDate.getTime() - requestOCRStartDate.getTime();
  }

  /**
   * Sets communication properties indicating the OCRed document
   * was sent back to the client and when
   *
   * @return {void}
   */
  public void communicated(){
    this.communicated = true;
    this.communicationAttempted = true;
    this.requestCommunicatedDate = new Date();
  }

  /**
   * Sets communication properties indicating the OCRed document
   * couldn't be sent back to the user
   *
   * @return {void}
   */
  public void failedToCommunicated(){
    this.communicationAttempted = true;
  }


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRequestorReference() {
    return requestorReference;
  }

  public void setRequestorReference(String requestorReference) {
    this.requestorReference = requestorReference;
  }

  public String getCallbackEndpoint() {
    return callbackEndpoint;
  }

  public void setCallbackEndpoint(String callbackEndpoint) {
    this.callbackEndpoint = callbackEndpoint;
  }

  public HttpMethod getCallbackMethod() {
    return callbackMethod;
  }

  public void setCallbackMethod(HttpMethod callbackMethod) {
    this.callbackMethod = callbackMethod;
  }

  public Short getPriority() {
    return priority;
  }

  public void setPriority(Short priority) {
    this.priority = priority;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public byte[] getFileToOCRByteArray() {
    return fileToOCRByteArray;
  }

  public void setFileToOCRByteArray(byte[] fileToOCRByteArray) {
    this.fileToOCRByteArray = fileToOCRByteArray;
  }

  public Short getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(Short statusCode) {
    this.statusCode = statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = (short) statusCode;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public void setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
  }

  public boolean isCommunicated() {
    return communicated;
  }

  public void setCommunicated(boolean communicated) {
    this.communicated = communicated;
  }

  public boolean isCommunicationAttempted() {
    return communicationAttempted;
  }

  public void setCommunicationAttempted(boolean communicationAttempted) {
    this.communicationAttempted = communicationAttempted;
  }

  public byte[] getOcredFileByteArray() {
    return ocredFileByteArray;
  }

  public void setOcredFileByteArray(byte[] ocredFileByteArray) {
    this.ocredFileByteArray = ocredFileByteArray;
  }

  public Date getRequestCreationDate() {
    return requestCreationDate;
  }

  public void setRequestCreationDate(Date requestCreationDate) {
    this.requestCreationDate = requestCreationDate;
  }

  public Date getRequestCommunicatedDate() {
    return requestCommunicatedDate;
  }

  public void setRequestCommunicatedDate(Date requestCommunicatedDate) {
    this.requestCommunicatedDate = requestCommunicatedDate;
  }

  public Date getRequestOCRStartDate() {
    return requestOCRStartDate;
  }

  public void setRequestOCRStartDate(Date requestOCRStartDate) {
    this.requestOCRStartDate = requestOCRStartDate;
  }

  public Date getRequestOCREndDate() {
    return requestOCREndDate;
  }

  public void setRequestOCREndDate(Date requestOCREndDate) {
    this.requestOCREndDate = requestOCREndDate;
  }

  @Override
  public String toString() {
    return "OCRRequest{" +
        "id=" + id +
        ", requestorReference='" + requestorReference + '\'' +
        ", priority=" + priority +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OCRRequest)) return false;

    OCRRequest that = (OCRRequest) o;

    if (communicated != that.communicated) return false;
    if (communicationAttempted != that.communicationAttempted) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (requestorReference != null ? !requestorReference.equals(that.requestorReference) : that.requestorReference != null)
      return false;
    if (!callbackEndpoint.equals(that.callbackEndpoint)) return false;
    if (callbackMethod != that.callbackMethod) return false;
    if (!priority.equals(that.priority)) return false;
    if (!token.equals(that.token)) return false;
    if (!statusCode.equals(that.statusCode)) return false;
    if (statusMessage != null ? !statusMessage.equals(that.statusMessage) : that.statusMessage != null) return false;
    if (!Arrays.equals(fileToOCRByteArray, that.fileToOCRByteArray)) return false;
    if (!Arrays.equals(ocredFileByteArray, that.ocredFileByteArray)) return false;
    if (requestCommunicatedDate != null ? !requestCommunicatedDate.equals(that.requestCommunicatedDate) : that.requestCommunicatedDate != null)
      return false;
    if (requestOCRStartDate != null ? !requestOCRStartDate.equals(that.requestOCRStartDate) : that.requestOCRStartDate != null)
      return false;
    return requestOCREndDate != null ? requestOCREndDate.equals(that.requestOCREndDate) : that.requestOCREndDate == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (requestorReference != null ? requestorReference.hashCode() : 0);
    result = 31 * result + callbackEndpoint.hashCode();
    result = 31 * result + callbackMethod.hashCode();
    result = 31 * result + priority.hashCode();
    result = 31 * result + token.hashCode();
    result = 31 * result + statusCode.hashCode();
    result = 31 * result + (statusMessage != null ? statusMessage.hashCode() : 0);
    result = 31 * result + (communicated ? 1 : 0);
    result = 31 * result + (communicationAttempted ? 1 : 0);
    result = 31 * result + Arrays.hashCode(fileToOCRByteArray);
    result = 31 * result + Arrays.hashCode(ocredFileByteArray);
    result = 31 * result + (requestCommunicatedDate != null ? requestCommunicatedDate.hashCode() : 0);
    result = 31 * result + (requestOCRStartDate != null ? requestOCRStartDate.hashCode() : 0);
    result = 31 * result + (requestOCREndDate != null ? requestOCREndDate.hashCode() : 0);
    return result;
  }
}
