package com.crudetech.sample.logcrunch;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogFileNamePatternTest {
    @Test
    public void matchesName() throws Exception {
        LogFileNamePattern namePattern = new LogFileNamePattern("logFile.%d{yyyy-MM-dd}.log");
        
        String fileName = "logFile.2009-08-21.log";
        assertThat(namePattern.matches(fileName), is(true));
    }

    @Test
    public void extractsDate() throws Exception {
        LogFileNamePattern namePattern = new LogFileNamePattern("logFile.%d{yyyy-MM-dd}.log");


        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, 2009);
        cal.set(Calendar.MONTH, Calendar.AUGUST);
        cal.set(Calendar.DATE, 21);
        Date expectedDate = cal.getTime();

        assertThat(namePattern.dateOf("logFile.2009-08-21.log"), is(expectedDate));
    }
}
