package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.UnaryFunction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

public class LogFile {
    private final File logFile;
    private final SimpleDateFormat dateFormat;
    private final Charset encoding;

    public LogFile(File logFile, SimpleDateFormat dateFormat, Charset encoding) {
        this.logFile = logFile;
        this.encoding = encoding;
        this.dateFormat = (SimpleDateFormat) dateFormat.clone();
    }

    public Iterable<? extends StringLogLine> getLines() {
        Iterable<String> textLines = new TextFileLineIterable(createNewReaderProvider());
        return new MappingIterable<String, StringLogLine>(textLines, selectLogLine());
    }

    private TextFileLineIterable.BufferedReaderProvider createNewReaderProvider() {
        return new TextFileLineIterable.BufferedReaderProvider() {
            @Override
            public BufferedReader newReader() {
                return createNewReader();
            }
        };
    }

    private UnaryFunction<String, StringLogLine> selectLogLine() {
        return new UnaryFunction<String, StringLogLine>() {
            @Override
            public StringLogLine evaluate(String argument) {
                return new StringLogLine(argument, dateFormat);
            }
        };
    }

    private BufferedReader createNewReader() {
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
