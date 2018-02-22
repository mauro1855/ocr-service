package com.github.mauro1855.ocrservice.repository;

import com.github.mauro1855.ocrservice.domain.OCRRequest;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Created by mauro1855 on 07/12/2016.
 */
@Repository
public class OCRRequestRepository {

    @Autowired
    private NamedParameterJdbcTemplate ocrServiceJdbcTemplate;

    private static final String GET_UNPROCESSED_REQUESTS = "SELECT * FROM ocr_requests WHERE status_code = 0 AND request_stopped = 'N' ORDER BY priority DESC, creation_date ASC";

    private static final String GET_SINGLE_REQUEST = "SELECT * FROM ocr_requests WHERE id = :requestId";

    private static final String GET_FAILED_COMMUNICATION_REQUESTS = "SELECT * FROM ocr_requests WHERE communicated = 'N' AND communication_attempted = 'Y' AND request_stopped = 'N' ORDER BY priority DESC, creation_date ASC";

    private static final String INSERT_NEW_REQUEST = "insert into ocr_requests (requestor_reference, \n" +
            "  callback_endpoint, \n" +
            "  callback_method, \n" +
            "  priority, \n" +
            "  token, \n" +
            "  status_code, \n" +
            "  status_message, \n" +
            "  communicated, \n" +
            "  communication_attempted,\n" +
            "  creation_date,\n" +
            "  file_to_ocr)  \n" +
            "values (:requestor_reference, \n" +
            ":callback_endpoint, \n" +
            ":callback_method, \n" +
            ":priority, :token, \n" +
            ":status_code, \n" +
            ":status_message, \n" +
            ":communicated, \n" +
            ":communication_attempted, \n" +
            ":creation_date, \n" +
            ":file_to_ocr)";

    private static final String UPDATE_REQUEST = "UPDATE ocr_requests \n" +
            "SET status_code = :statusCode, \n" +
            "status_message = :statusMessage, \n" +
            "communicated = :communicated, \n" +
            "communication_attempted = :communicationAttempted, \n" +
            "communicated_date = :communicationDate,\n" +
            "OCR_start_date = :ocrStartDate,\n" +
            "OCR_end_date = :ocrEndDate,\n" +
            "file_to_ocr = :fileToOCR,\n" +
            "ocred_file = :ocredFile \n" +
            "WHERE id = :id";


    /**
     * Gets an OCRRequest by it's ID
     * @param requestId of the request
     * @return {OCRRequest}
     */
    public OCRRequest getRequest(Long requestId){
        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue( "requestId", requestId);
        return ocrServiceJdbcTemplate.queryForObject(GET_SINGLE_REQUEST, parameters, ocrRequestTypeMapping());
    }

    /**
     * Inserts a new request in the database
     * and gets generated ID
     *
     * @param newRequest
     * @return {void}
     */
    public void createNewRequest(OCRRequest newRequest){

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue( "requestor_reference", newRequest.getRequestorReference())
                .addValue( "callback_endpoint",  newRequest.getCallbackEndpoint())
                .addValue( "callback_method",  newRequest.getCallbackMethod().toString())
                .addValue( "priority",  newRequest.getPriority())
                .addValue( "token",  newRequest.getToken())
                .addValue( "status_code",  newRequest.getStatusCode())
                .addValue( "status_message",  newRequest.getStatusMessage())
                .addValue( "communicated",  newRequest.isCommunicated() ? "Y" : "N")
                .addValue( "communication_attempted",  newRequest.isCommunicationAttempted() ? "Y" : "N")
                .addValue( "creation_date",  newRequest.getRequestCreationDate())
                .addValue( "file_to_ocr",  newRequest.getFileToOCRByteArray());
        final KeyHolder holder = new GeneratedKeyHolder();
        ocrServiceJdbcTemplate.update(INSERT_NEW_REQUEST, parameters, holder, new String[] {"id"} );

        Long newRequestId = holder.getKey().longValue();
        newRequest.setId(newRequestId);
    }

    /**
     * Updates request in the database
     *
     * @param request
     * @return {void}
     */
    public void updateRequest(OCRRequest request){

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue( "id", request.getId())
                .addValue( "statusCode",  request.getStatusCode())
                .addValue( "statusMessage",  request.getStatusMessage())
                .addValue( "communicated",  request.isCommunicated() ? "Y" : "N")
                .addValue( "communicationAttempted",  request.isCommunicationAttempted() ? "Y" : "N")
                .addValue( "communicationDate",  request.getRequestCommunicatedDate())
                .addValue( "ocrStartDate", request.getRequestOCRStartDate())
                .addValue( "ocrEndDate", request.getRequestOCREndDate());

        // if request has been communicated, set fileToOCR and OCRedFile to null
        if(request.isCommunicated())
            parameters.addValue("fileToOCR", null).addValue("ocredFile", null);
        else
            parameters.addValue("fileToOCR", request.getFileToOCRByteArray()).addValue( "ocredFile",  request.getOcredFileByteArray());

        ocrServiceJdbcTemplate.update(UPDATE_REQUEST, parameters);
    }

    /**
     * Gets all unprocessed requests from the database
     *
     * @return {List} list of unprocessed requests
     */
    public List<OCRRequest> getAllUnprocessedRequests(){

        List<OCRRequest> ocrRequests = ocrServiceJdbcTemplate.query(GET_UNPROCESSED_REQUESTS, ocrRequestTypeMapping());
        return ocrRequests;
    }

    /**
     * Gets all requests that failed to be communicated
     * to the client
     *
     * @return {List} list of processed requests with failed communication
     */
    public List<OCRRequest> getAllFailedCommunicationRequests(){

        List<OCRRequest> ocrRequests = ocrServiceJdbcTemplate.query(GET_FAILED_COMMUNICATION_REQUESTS, ocrRequestTypeMapping());
        return ocrRequests;
    }

    private static RowMapper<OCRRequest> ocrRequestTypeMapping()
    {
        return (resultSet, i) ->
        {
            Long id = resultSet.getLong("id");
            String requestorReference = resultSet.getString("requestor_reference");
            String callbackEndpoint = resultSet.getString("callback_endpoint");
            String callbackMethod = resultSet.getString("callback_method");
            Short priority = resultSet.getShort("priority");
            String token = resultSet.getString("token");
            Short statusCode = resultSet.getShort("status_code");
            String statusMessage = resultSet.getString("status_message");
            Boolean communicated = resultSet.getString("communicated").equals("Y");
            Boolean communicationAttempted = resultSet.getString("communication_attempted").equals("Y");
            Date creationDate = resultSet.getTimestamp("creation_date");
            Date communicatedDate = resultSet.getTimestamp("communicated_date");
            Date OCRStartDate = resultSet.getTimestamp("OCR_start_date");
            Date OCREndDate = resultSet.getTimestamp("OCR_end_date");
            byte[] fileToOCR = resultSet.getBytes("file_to_ocr");
            byte[] ocredFile = resultSet.getBytes("ocred_file");

            OCRRequest newRequest =  new OCRRequest(requestorReference,callbackEndpoint,HttpMethod.resolve(callbackMethod), priority,fileToOCR);
            newRequest.setId(id);
            newRequest.setToken(token);
            newRequest.setStatusCode(statusCode);
            newRequest.setStatusMessage(statusMessage);
            newRequest.setCommunicated(communicated);
            newRequest.setCommunicationAttempted(communicationAttempted);
            newRequest.setOcredFileByteArray(ocredFile);
            newRequest.setRequestCreationDate(creationDate);
            newRequest.setRequestCommunicatedDate(communicatedDate);
            newRequest.setRequestOCRStartDate(OCRStartDate);
            newRequest.setRequestOCREndDate(OCREndDate);

            return newRequest;
        };
    }

}
