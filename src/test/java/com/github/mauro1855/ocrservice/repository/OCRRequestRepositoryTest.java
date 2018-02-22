package com.github.mauro1855.ocrservice.repository;

import java.util.Map;

import com.github.mauro1855.ocrservice.domain.OCRRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Created by mauro1855 on 19/12/2016.
 */
public class OCRRequestRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate foundJdbcTemplate;

    @Spy
    @InjectMocks
    private OCRRequestRepository ocrRequestRepository;

    private OCRRequest mockRequest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockRequest = new OCRRequest("test", "/test", HttpMethod.POST, (short) 1, null);
    }

    @Test
    public void test_getRequest() throws Exception {
        ArgumentCaptor<MapSqlParameterSource> sqlParametersCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        ArgumentCaptor<String> sqlQueryCaptor = ArgumentCaptor.forClass(String.class);

        ocrRequestRepository.getRequest(mockRequest.getId());

        verify(foundJdbcTemplate).queryForObject(sqlQueryCaptor.capture(), sqlParametersCaptor.capture(), any(RowMapper.class));

        Map<String, Object> sqlParametersMap = sqlParametersCaptor.getValue().getValues();
        String originalQuery = sqlQueryCaptor.getValue();
        String processedQuery = originalQuery;

        for(String key : sqlParametersMap.keySet()){
            processedQuery = processedQuery.replace(":" + key, "something not Relevant");
        }
        // To confirm all parameters were replaced
        assertFalse(processedQuery.contains(":"));
    }

    @Test
    public void test_createNewRequest() throws Exception {
        ArgumentCaptor<MapSqlParameterSource> sqlParametersCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        ArgumentCaptor<String> sqlQueryCaptor = ArgumentCaptor.forClass(String.class);

        try {
            ocrRequestRepository.createNewRequest(mockRequest);
        }catch(NullPointerException ex){
            // not possible to mock keyHolder, exception expected
        }

        verify(foundJdbcTemplate).update(sqlQueryCaptor.capture(), sqlParametersCaptor.capture(), any(KeyHolder.class), any(String[].class));

        Map<String, Object> sqlParametersMap = sqlParametersCaptor.getValue().getValues();
        String originalQuery = sqlQueryCaptor.getValue();
        String processedQuery = originalQuery;

        for(String key : sqlParametersMap.keySet()){
            processedQuery = processedQuery.replace(":" + key, "something not Relevant");
        }
        // To confirm all parameters were replaced
        assertFalse(processedQuery.contains(":"));
    }

    @Test
    public void test_updateRequest() throws Exception {
        ArgumentCaptor<MapSqlParameterSource> sqlParametersCaptor = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        ArgumentCaptor<String> sqlQueryCaptor = ArgumentCaptor.forClass(String.class);

        ocrRequestRepository.updateRequest(mockRequest);

        verify(foundJdbcTemplate).update(sqlQueryCaptor.capture(), sqlParametersCaptor.capture());

        Map<String, Object> sqlParametersMap = sqlParametersCaptor.getValue().getValues();
        String originalQuery = sqlQueryCaptor.getValue();
        String processedQuery = originalQuery;

        for(String key : sqlParametersMap.keySet()){
            processedQuery = processedQuery.replace(":" + key, "something not Relevant");
        }
        // To confirm all parameters were replaced
        assertFalse(processedQuery.contains(":"));

    }

    @Test
    public void test_getAllUnprocessedRequests() throws Exception {
        ocrRequestRepository.getAllUnprocessedRequests();

        verify(foundJdbcTemplate).query(any(String.class), any(RowMapper.class));
    }

    @Test
    public void test_getAllFailedCommunicationRequests() throws Exception {
        ocrRequestRepository.getAllFailedCommunicationRequests();

        verify(foundJdbcTemplate).query(any(String.class), any(RowMapper.class));
    }

}