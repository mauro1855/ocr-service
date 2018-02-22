package com.github.mauro1855.ocrservice.service;

import java.util.ArrayList;
import java.util.Date;

import com.github.mauro1855.ocrservice.domain.OCRRequest;
import com.github.mauro1855.ocrservice.repository.OCRRequestRepository;
import com.github.mauro1855.ocrservice.util.OCRThreadPoolExecutor;
import com.github.mauro1855.ocrservice.util.PriorityRunnable;
import com.github.mauro1855.ocrservice.worker.OCRRequestWorker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpMethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by mauro1855 on 19/12/2016.
 */
public class OCRRequestServiceTest {

    @Mock
    private OCRRequestWorker ocrRequestWorker;

    @Mock
    private OCRRequestRepository ocrRequestRepository;

    @Mock
    private OCRThreadPoolExecutor priorityExecutor;


    @Spy
    @InjectMocks
    private OCRRequestService ocrRequestService;

    private OCRRequest mockRequest;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockRequest = new OCRRequest("test", "/test", HttpMethod.POST, (short) 1, null);
    }

    @Test
    public void test_restoreRequestsFromLastSession() throws Exception {

        when(ocrRequestRepository.getAllUnprocessedRequests()).thenReturn(new ArrayList<>());

        ocrRequestService.restoreRequestsFromLastSession();

        verify(ocrRequestRepository).getAllUnprocessedRequests();

    }

    @Test
    public void test_registerNewOCRRequest() throws Exception {

        mockRequest.setId(2L);
        mockRequest.setFileToOCRByteArray(new byte[] {});

        when(ocrRequestWorker.getRunnable(mockRequest)).thenReturn(new PriorityRunnable() {
            @Override
            public int getPriority() {
                return 0;
            }

            @Override
            public Date getDate() {
                return new Date();
            }

            @Override
            public void run() {
                //
            }
        });

        long result = ocrRequestService.registerNewOCRRequest(mockRequest);

        verify(ocrRequestRepository).createNewRequest(any(OCRRequest.class));
        assertEquals(2L, result);
        assertNull(mockRequest.getFileToOCRByteArray());

    }

}