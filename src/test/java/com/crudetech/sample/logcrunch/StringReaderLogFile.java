package com.crudetech.sample.logcrunch;

import java.io.Reader;
import java.io.StringReader;

public class StringReaderLogFile extends BufferedReaderLogFile{
    private final String content;

    public StringReaderLogFile(LogLineFactory logLineFactory, String content) {
        super(logLineFactory);
        this.content = content;
    }

    @Override
    protected Reader createNewReader() {
        return new StringReader(content);
    }
}
