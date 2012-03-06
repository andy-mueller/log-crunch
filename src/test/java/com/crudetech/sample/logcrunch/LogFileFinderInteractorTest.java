package com.crudetech.sample.logcrunch;

import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogFileFinderInteractorTest {

    @Test
    public void findFiles(){
        Date date = new Date();
        LogFile logFileStub = mock(FileLogFile.class);
        LogFileLocator locator = mock(LogFileLocator.class);
        when(locator.find("machine101", date)).thenReturn(logFileStub);
        
        LogFileFinderInteractor interactor = new LogFileFinderInteractor(locator);

        Iterable<LogFile> logFiles = interactor.getLogFiles("machine101", date);
        
        assertThat(logFiles.iterator().next(), is(logFileStub));
    }

//    @Test
//    public void foundFilesAreFiltered(){
//        Date date = new Date();
//        FileLogFile logFileStub = new FileLogFile();
//        LogFileLocator locator = mock(LogFileLocator.class);
//        when(locator.find("machine101", date)).thenReturn(logFileStub);
//
//        LogFileFinderInteractor interactor = new LogFileFinderInteractor(locator);
//
//        Iterable<FileLogFile> logFiles = interactor.getLogFiles("machine101", date);
//
//        assertThat(logFiles.iterator().next(), is(logFileStub));
//    }

}
