package com.github.mauro1855.ocrservice.controller;

import com.github.mauro1855.ocrservice.domain.OCRRequest;
import com.github.mauro1855.ocrservice.service.OCRRequestService;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by pereirat on 19/12/2016.
 */
public class OCRControllerTest {

    @Mock
    private OCRRequestService ocrRequestService;

    @InjectMocks
    @Spy
    private OCRController ocrController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_registerNewOCRRequest_fileNotPDF() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);

        when(mockFile.getContentType()).thenReturn("image/jpeg");

        ResponseEntity<String> result = ocrController.registerNewOCRRequest("", (short) 1, "/test", HttpMethod.POST, mockFile);
        JSONObject body = new JSONObject(result.getBody());

        assertFalse(body.getBoolean("success"));
        assertEquals("Impossible to process file: it is not a .pdf", body.get("message"));
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, result.getStatusCode());
        verify(ocrRequestService, never()).registerNewOCRRequest(any(OCRRequest.class));

    }

    @Test
    public void test_registerNewOCRRequest_requestFailedToRegister() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);

        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getBytes()).thenReturn("Test".getBytes());
        when(ocrRequestService.registerNewOCRRequest(any(OCRRequest.class))).thenReturn(null);

        ResponseEntity<String> result = ocrController.registerNewOCRRequest("test", (short) 1, "/test", HttpMethod.POST, mockFile);
        JSONObject body = new JSONObject(result.getBody());

        verify(ocrRequestService).registerNewOCRRequest(any(OCRRequest.class));
        assertFalse(body.getBoolean("success"));
        assertEquals("It is not possible to fulfill the request at this time", body.get("message"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
        verify(ocrRequestService).registerNewOCRRequest(any(OCRRequest.class));

    }

    @Test
    public void test_registerNewOCRRequest_requestRegistered() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        ArgumentCaptor<OCRRequest> argumentCaptor = ArgumentCaptor.forClass(OCRRequest.class);

        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getBytes()).thenReturn("Test".getBytes());
        when(ocrRequestService.registerNewOCRRequest(any(OCRRequest.class))).thenReturn(2L);

        ResponseEntity<String> result = ocrController.registerNewOCRRequest("test", (short) 1, "/test", HttpMethod.POST, mockFile);
        JSONObject body = new JSONObject(result.getBody());

        verify(ocrRequestService).registerNewOCRRequest(argumentCaptor.capture());
        assertEquals("test", argumentCaptor.getValue().getRequestorReference());
        assertEquals("/test", argumentCaptor.getValue().getCallbackEndpoint());
        assertEquals((short) 1, (short) argumentCaptor.getValue().getPriority());
        assertEquals(HttpMethod.POST, argumentCaptor.getValue().getCallbackMethod());
        assertTrue(body.getBoolean("success"));
        assertEquals(2L, body.getLong("requestId"));
        assertEquals("The request was accepted", body.get("message"));
        assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
        verify(ocrRequestService).registerNewOCRRequest(any(OCRRequest.class));

    }

    private OCRRequest mockNewOCRRequest(){
        return new OCRRequest("test", "/test", HttpMethod.POST, (short) 1, "Test".getBytes());
    }

}