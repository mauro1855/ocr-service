package com.github.mauro1855.ocrservice.controller;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by pereirat on 19/12/2016.
 */
public class UIControllerTest {

    private UIController uiController;

    @Before
    public void setUp(){
        uiController = new UIController();
    }

    @Test
    public void test_home(){
        assertEquals("index", uiController.home());
    }

}