package com.crudetech.sample.logcrunch;

import org.joda.time.Interval;

public class ListLogFilesInteractor {
    private final LogFileLocator logFileLocator;

    public interface Query{
        LogFileNamePattern getLogFileNamePattern();

        Iterable<Interval> getSearchIntervals();
    }
    public ListLogFilesInteractor(LogFileLocator logFileLocator) {
        this.logFileLocator = logFileLocator;
    }

    public interface Result {
        void listFile(LogFile logFile);

        void noFilesFound();
    }
    public void listFiles(Query query, Result result){
        boolean noFilesFound = true;
        for (LogFile logFile : logFileLocator.find(query.getLogFileNamePattern(), query.getSearchIntervals())) {
            result.listFile(logFile);
            logFile.close();
            noFilesFound = false;
        }
        if(noFilesFound){
            result.noFilesFound();
        }
    }

}
