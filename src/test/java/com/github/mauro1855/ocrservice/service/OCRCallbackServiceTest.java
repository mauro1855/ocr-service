package com.github.mauro1855.ocrservice.service;

import com.github.mauro1855.ocrservice.domain.OCRRequest;
import com.github.mauro1855.ocrservice.repository.OCRRequestRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by mauro1855 on 19/12/2016.
 */
public class OCRCallbackServiceTest {

    @Mock
    private OCRRequestRepository ocrRequestRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    @Spy
    private OCRCallbackService ocrCallbackService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_replyToRequest_DidNotReceive200OK() throws Exception {

        OCRRequest mockRequest = new OCRRequest("test", "/test", HttpMethod.POST, (short) 1, null);
        mockRequest.setOcredFileByteArray("test".getBytes());
        ResponseEntity<String> mockedResponse = new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(mockedResponse);

        ocrCallbackService.replyToRequest(mockRequest);

        assertTrue(mockRequest.isCommunicationAttempted());
        assertFalse(mockRequest.isCommunicated());

    }

    @Test
    public void test_replyToRequest_200OK() throws Exception {

        OCRRequest mockRequest = new OCRRequest("test", "/test", HttpMethod.POST, (short) 1, null);
        mockRequest.setOcredFileByteArray("test".getBytes());
        ResponseEntity<String> mockedResponse = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(mockedResponse);

        ocrCallbackService.replyToRequest(mockRequest);

        assertTrue(mockRequest.isCommunicationAttempted());
        assertTrue(mockRequest.isCommunicated());

    }

}
