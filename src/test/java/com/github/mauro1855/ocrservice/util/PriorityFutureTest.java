package com.github.mauro1855.ocrservice.util;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mauro1855 on 19/12/2016.
 */
public class PriorityFutureTest {


    private Date before;
    private Date equalsBefore;
    private Date after;

    @Before
    public void setUp(){
        before = new Date();
        equalsBefore = new Date(before.getTime());
        after = new Date(before.getTime() + 1000L);
    }

    @Test
    public void test_Comparator_differentPriorities(){

        PriorityFuture<Runnable> o1 = new PriorityFuture<>(null, 1, before);
        PriorityFuture<Runnable> o2 = new PriorityFuture<>(null, 2, before);

        assertEquals(1, PriorityFuture.comparator.compare(o1, o2));
        assertEquals(-1, PriorityFuture.comparator.compare(o2, o1));
    }

    @Test
    public void test_Comparator_samePrioritiesDifferentDates(){

        PriorityFuture<Runnable> o1 = new PriorityFuture<>(null, 1, before);
        PriorityFuture<Runnable> o2 = new PriorityFuture<>(null, 1, after);

        assertEquals(-1, PriorityFuture.comparator.compare(o1, o2));
        assertEquals(1, PriorityFuture.comparator.compare(o2, o1));
    }

    @Test
    public void test_Comparator_samePrioritiesSameDates(){

        PriorityFuture<Runnable> o1 = new PriorityFuture<>(null, 1, before);
        PriorityFuture<Runnable> o2 = new PriorityFuture<>(null, 1, equalsBefore);

        assertEquals(-1, PriorityFuture.comparator.compare(o1, o2));
        assertEquals(-1, PriorityFuture.comparator.compare(o2, o1));
    }

}