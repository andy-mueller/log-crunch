package com.crudetech.sample.logcrunch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class FileLogFile extends BufferedReaderLogFile {
    private final File logFile;
    private final Charset encoding;

    public FileLogFile(File logFile, LogLineFactory logLineFactory, Charset encoding) {
        super(logLineFactory);
        this.logFile = logFile;
        this.encoding = encoding;
    }

    @Override
    protected BufferedReader createNewReader() {
       try {
            return new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(logFile),
                            encoding)
            );
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
