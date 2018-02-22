package com.github.mauro1855.ocrservice.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by mauro1855 on 19/12/2016.
 */
public class StreamConsumerTest {

    private boolean isClosed = false;

    private InputStream mockedInputStream = new ByteArrayInputStream("Test".getBytes()){
        @Override
        public void close() throws IOException {
            isClosed = true;
            super.close();
        }
    };

    @Test
    public void test_streamIsClosed(){
        StreamConsumer streamConsumer = new StreamConsumer(mockedInputStream);
        streamConsumer.run();
        assertEquals(true, isClosed);


    }


}