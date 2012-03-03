package com.crudetech.sample.logcrunch;


import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LogFileTest {
    @Test
    public void lifeCycleEndClosesStream() throws IOException {
        InputStream fileIn = mock(InputStream.class);

        LogFile logFile = new LogFile(fileIn);
        logFile.close();

        verify(fileIn).close();
    }
}
