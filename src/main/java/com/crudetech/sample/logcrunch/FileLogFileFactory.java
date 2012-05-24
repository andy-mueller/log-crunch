package com.crudetech.sample.logcrunch;

import java.io.File;
import java.nio.charset.Charset;

public class FileLogFileFactory implements FileSystemLogFileLocator.LogFileFactory {
    private final BufferedReaderLogFile.LogLineFactory logLineFactory;
    private final Charset encoding;

    public FileLogFileFactory(BufferedReaderLogFile.LogLineFactory logLineFactory, Charset encoding) {
        this.logLineFactory = logLineFactory;
        this.encoding = encoding;
    }

    @Override
    public LogFile create(File logFile) {
        return new FileLogFile(logFile, logLineFactory, encoding);
    }
}

