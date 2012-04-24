package com.crudetech.sample.logcrunch.logback;

import com.crudetech.sample.logcrunch.BufferedReaderLogFile;
import com.crudetech.sample.logcrunch.LogFileFilterInteractorFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.File;
import java.nio.charset.Charset;

public class LogbackLogFileFilterInteractorFactory extends LogFileFilterInteractorFactory {
    @XmlElement
    private String logLineFormat;
    @XmlElement
    private File searchPath;
    @XmlJavaTypeAdapter(CharsetXmlAdapter.class)
    @XmlElement
    private Charset encoding;


    private LogbackLogFileFilterInteractorFactory() {
    }

    public LogbackLogFileFilterInteractorFactory(File searchPath, Charset encoding, String logLineFormat) {
        this.searchPath = searchPath;
        this.encoding = encoding;
        this.encoding = encoding;
        this.logLineFormat = logLineFormat;
    }

    @Override
    protected File getSearchPath() {
        return searchPath;
    }

    @Override
    protected Charset getEncoding() {
        return encoding;
    }

    @Override
    protected BufferedReaderLogFile.LogLineFactory logLineFactory() {
        return new LogbackLogLineFactory(logLineFormat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogbackLogFileFilterInteractorFactory that = (LogbackLogFileFilterInteractorFactory) o;

        if (!encoding.equals(that.encoding)) return false;
        if (!logLineFormat.equals(that.logLineFormat)) return false;
        if (!searchPath.equals(that.searchPath)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = logLineFormat.hashCode();
        result = 31 * result + searchPath.hashCode();
        result = 31 * result + encoding.hashCode();
        return result;
    }
}
