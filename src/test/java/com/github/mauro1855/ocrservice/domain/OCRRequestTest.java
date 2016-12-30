package com.github.mauro1855.ocrservice.domain;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

import static org.junit.Assert.*;

/**
 * Created by pereirat on 19/12/2016.
 */
public class OCRRequestTest {

    private OCRRequest ocrRequest;


    @Before
    public void setUp() throws Exception {
        ocrRequest = new OCRRequest("test", "/test", HttpMethod.POST, (short) 1, "Test".getBytes());
        ocrRequest.setId(2L);
    }

    @Test
    public void test_startOCR(){
        ocrRequest.setRequestOCRStartDate(null);

        ocrRequest.startOCR();

        assertNotEquals(null, ocrRequest.getRequestOCRStartDate());
    }

    @Test
    public void test_endOCR(){
        ocrRequest.setRequestOCRStartDate(new Date());
        ocrRequest.setRequestOCREndDate(null);

        ocrRequest.endOCR();

        assertNotEquals(null, ocrRequest.getRequestOCREndDate());
    }

    @Test
    public void test_getOCRDuration(){
        ocrRequest.setRequestOCRStartDate(new Date());
        ocrRequest.setRequestOCREndDate(new Date(ocrRequest.getRequestOCRStartDate().getTime() + 1000));

        assertEquals(1000L, (long) ocrRequest.getOCRDuration());
    }

    @Test
    public void test_communicated(){
        ocrRequest.setCommunicated(false);
        ocrRequest.setRequestCommunicatedDate(null);

        ocrRequest.communicated();

        assertTrue(ocrRequest.isCommunicated());
        assertNotEquals(null, ocrRequest.getRequestCommunicatedDate());
    }

    @Test
    public void test_failedToCommunicated(){
        ocrRequest.setCommunicationAttempted(false);

        ocrRequest.failedToCommunicated();

        assertTrue(ocrRequest.isCommunicationAttempted());
    }

}