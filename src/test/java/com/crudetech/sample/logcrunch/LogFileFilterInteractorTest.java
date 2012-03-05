package com.crudetech.sample.logcrunch;

import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogFileFilterInteractorTest {
    @Test
    public void ctor(){
        LogFileFilterInteractor interactor = new LogFileFilterInteractor();
    }

    @Test
    public void apply(){
        Date date = null;
        LogFile logFileStub = mock(LogFile.class);
        LogFileLocator locator = mock(LogFileLocator.class);
        when(locator.find("machine101", date)).thenReturn(logFileStub);
        
        LogFileFilterInteractor interactor = new LogFileFilterInteractor();

        Iterable<LogFile> logFiles = interactor.filterFiles("machine101", date);
        
        assertThat(logFiles.iterator().next(), is(logFileStub));
    }

}
