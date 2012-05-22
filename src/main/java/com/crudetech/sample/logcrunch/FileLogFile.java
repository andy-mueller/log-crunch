package com.crudetech.sample.logcrunch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
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
    protected Reader createNewReader() {
        try {
            return new InputStreamReader(
                    new FileInputStream(logFile),
                    encoding);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void print(PrintWriter w) {
        w.print(logFile.getName());
    }
}
