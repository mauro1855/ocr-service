package com.github.mauro1855.ocrservice.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mauro1855 on 21/04/2017.
 */
public class StreamConsumer extends Thread {

    private static Logger logger = LoggerFactory.getLogger(StreamConsumer.class);

    private InputStream inputStream;

    public StreamConsumer(InputStream inputStream){
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try{
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            while(bufferedReader.readLine() != null){}

            logger.debug("Closing process stream");
            inputStream.close();

        } catch (IOException e) {}
    }
}
