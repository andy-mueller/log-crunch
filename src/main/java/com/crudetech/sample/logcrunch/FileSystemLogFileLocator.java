package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.FilterIterable;
import com.crudetech.sample.filter.Predicate;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import static java.util.Arrays.asList;

public class FileSystemLogFileLocator implements LogFileLocator {
    private final File logFilePath;
    private final LogFileFactory factory;
    private static final String filePattern = "{0}-{1}";
    private SimpleDateFormat fileNameDateFormat= new SimpleDateFormat("yyyyMMdd");


    interface LogFileFactory{
        LogFile create(File logFile);
    }

    public FileSystemLogFileLocator(File logFilePath, LogFileFactory factory) {
        this.logFilePath = logFilePath;
        this.factory = factory;
    }

    @Override
    public LogFile find(String fileName, DateTimeRange range) {
        String logFileName = MessageFormat.format(filePattern, fileName, fileNameDateFormat.format(range.getStart()));

        Iterable<File> matches = new FilterIterable<File>(asList(logFilePath.listFiles()), nameContains(logFileName));
        
        for(File matchedFile : matches){
            return factory.create(matchedFile);
        }

        // TODO: return null object instead
        throw new IllegalArgumentException();
    }

    private Predicate<? super File> nameContains(final String name) {
        return new Predicate<File>() {
            @Override
            public Boolean evaluate(File item) {
                try {
                    return item.getCanonicalPath().toLowerCase().contains(name.toLowerCase());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }


}
