package com.github.mauro1855.ocrservice.worker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import com.github.mauro1855.ocrservice.domain.OCRRequest;
import com.github.mauro1855.ocrservice.repository.OCRRequestRepository;
import com.github.mauro1855.ocrservice.service.OCRCallbackService;
import com.github.mauro1855.ocrservice.util.PriorityRunnable;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpMethod;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by mauro1855 on 19/12/2016.
 */
public class OCRRequestWorkerTest {

    @Mock
    private OCRRequestRepository ocrRequestRepository;

    @Mock
    private OCRCallbackService ocrCallbackService;

    @Mock
    private Runtime runtime;

    @Mock
    private Process mockedProcess;

    @Spy
    @InjectMocks
    private OCRRequestWorker ocrRequestWorker;

    private OCRRequest mockRequest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ocrRequestWorker.setOcrCommand("test");
        ocrRequestWorker.setOcrCommand("test");
        ocrRequestWorker.setOcrOutputFilePrefixCommand("test");
        mockRequest = new OCRRequest("test", "/test", HttpMethod.POST, (short) 1, null);
        mockRequest.setId(250L);
        mockRequest.startOCR();
        mockRequest.setFileToOCRByteArray("Test".getBytes());
        when(ocrRequestRepository.getRequest(mockRequest.getId())).thenReturn(mockRequest);
    }

    @Test
    public void test_PriorityRunnable() {
        Runnable runnable = ocrRequestWorker.getRunnable(mockRequest);

        assertTrue(runnable instanceof PriorityRunnable);
        PriorityRunnable priorityRunnable = (PriorityRunnable) runnable;
        assertEquals((int) mockRequest.getPriority(), priorityRunnable.getPriority());
        assertEquals(mockRequest.getRequestCreationDate(), priorityRunnable.getDate());
    }

    @Test
    public void test_externalToolFailedToOCR() throws IOException {
        Process mockProcess = newFailedProcess();
        mockRequest.setRequestOCREndDate(null);
        when(runtime.exec(any(String.class))).thenReturn(mockProcess);

        ocrRequestWorker.getRunnable(mockRequest).run();

        assertNotNull(mockRequest.getRequestOCREndDate());
        assertEquals(-1, (int) mockRequest.getStatusCode());
        verify(runtime, times(3)).exec(any(String.class));
        verify(ocrRequestRepository).updateRequest(mockRequest);
        verify(ocrCallbackService).replyToRequest(mockRequest);
    }

    @Test
    public void test_externalToolTookTooMuchTime() throws IOException {
        Process mockProcess = newTimeOutProcess();

        mockRequest.setRequestOCREndDate(null);
        when(runtime.exec(any(String.class))).thenReturn(mockProcess);

        ocrRequestWorker.getRunnable(mockRequest).run();

        assertNotNull(mockRequest.getRequestOCREndDate());
        assertEquals(-1, (int) mockRequest.getStatusCode());
        verify(runtime, times(3)).exec(any(String.class));
        verify(ocrRequestRepository).updateRequest(mockRequest);
        verify(ocrCallbackService).replyToRequest(mockRequest);
    }

    @Test
    public void test_aliveProcessIsDestroyed() throws Exception {
        mockRequest.setRequestOCREndDate(null);
        when(runtime.exec(any(String.class))).thenReturn(mockedProcess);
        when(mockedProcess.getErrorStream()).thenReturn(new ByteArrayInputStream("Test".getBytes()));
        when(mockedProcess.getInputStream()).thenReturn(new ByteArrayInputStream("Test".getBytes()));
        when(mockedProcess.waitFor(any(Long.class), any(TimeUnit.class))).thenReturn(false);
        when(mockedProcess.isAlive()).thenReturn(true);

        ocrRequestWorker.getRunnable(mockRequest).run();

        verify(mockedProcess, atLeast(1)).destroy();
    }

    @Test
    public void test_OCRSuccessful() throws IOException {

        String targetFileName = ocrRequestWorker.getTempDir() + mockRequest.getId() + "_ocr.pdf";
        File mockFile = new File(targetFileName);
        byte[] emptyByteArray = "1".getBytes();
        FileUtils.writeByteArrayToFile(mockFile, emptyByteArray);

        Process mockProcess = newSuccessProcess();
        mockRequest.setRequestOCREndDate(null);
        when(runtime.exec(any(String.class))).thenReturn(mockProcess);

        ocrRequestWorker.getRunnable(mockRequest).run();

        assertNotNull(mockRequest.getRequestOCREndDate());
        assertEquals(1, (int) mockRequest.getStatusCode());
        verify(runtime, times(1)).exec(any(String.class));
        verify(ocrRequestRepository).updateRequest(mockRequest);
        verify(ocrCallbackService).replyToRequest(mockRequest);
    }

    private Process newFailedProcess(){
        return new Process() {
            @Override
            public OutputStream getOutputStream() {
                return null;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream("Test".getBytes());
            }

            @Override
            public InputStream getErrorStream() {
                return new ByteArrayInputStream("Test".getBytes());
            }

            @Override
            public int waitFor() throws InterruptedException {
                return 0;
            }

            @Override
            public int exitValue() {
                return -1;
            }

            @Override
            public void destroy() {
                return;
            }
        };
    }


    private Process newSuccessProcess(){
        return new Process() {
            @Override
            public OutputStream getOutputStream() {
                return null;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream("Test".getBytes());
            }

            @Override
            public InputStream getErrorStream() {
                return new ByteArrayInputStream("Test".getBytes());
            }

            @Override
            public int waitFor() throws InterruptedException {
                return 0;
            }

            @Override
            public int exitValue() {
                return 0;
            }

            @Override
            public void destroy() {
                return;
            }
        };
    }

    private Process newTimeOutProcess(){
        return new Process() {
            @Override
            public OutputStream getOutputStream() {
                return null;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream("Test".getBytes());
            }

            @Override
            public InputStream getErrorStream() {
                return new ByteArrayInputStream("Test".getBytes());
            }

            @Override
            public int waitFor() throws InterruptedException {
                return 0;
            }

            @Override
            public boolean waitFor(long timeout, TimeUnit unit){
                return false;
            }

            @Override
            public int exitValue() {
                return 0;
            }

            @Override
            public void destroy() {
                return;
            }
        };
    }

}