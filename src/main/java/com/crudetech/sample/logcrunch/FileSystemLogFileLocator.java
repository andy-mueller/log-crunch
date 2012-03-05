package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.FilterIterable;
import com.crudetech.sample.filter.Predicate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.Arrays.asList;

public class FileSystemLogFileLocator {
    private final File logFilePath;
    private static final String filePattern = "{0}-{1}";
    private SimpleDateFormat fileNameDateFormat= new SimpleDateFormat("yyyyMMdd");


    //TODO: move to LogFileFactor
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private Charset encoding = Charset.forName("UTF-8");

    public FileSystemLogFileLocator(File logFilePath) {
        this.logFilePath = logFilePath;
    }

    public LogFile find(String fileName, Date sixthOfJune) {
        String logFileName = MessageFormat.format(filePattern, fileName, fileNameDateFormat.format(sixthOfJune));

        Iterable<File> matches = new FilterIterable<File>(asList(logFilePath.listFiles()), nameContains(logFileName));
        
        for(File matchedFile : matches){
            return new LogFile(matchedFile, dateFormat, encoding);
        }

        // TODO: return null object instead
        throw new IllegalArgumentException();
    }

    private Predicate<? super File> nameContains(final String name) {
        return new Predicate<File>() {
            @Override
            public boolean evaluate(File item) {
                try {
                    return item.getCanonicalPath().toLowerCase().contains(name.toLowerCase());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }


}
