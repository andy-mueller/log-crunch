package com.crudetech.sample.logcrunch.logback;

import com.crudetech.sample.logcrunch.BufferedReaderLogFile;
import com.crudetech.sample.logcrunch.FileSystemListLogFilesInteractorFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.File;
import java.nio.charset.Charset;

@XmlRootElement(name = "listLogFilesInteractorFactory")
public class LogbackListLogFilesInteractorFactory extends FileSystemListLogFilesInteractorFactory {
    @XmlElement
    private String logLineFormat;
    @XmlElement
    private File searchPath;
    @XmlJavaTypeAdapter(CharsetXmlAdapter.class)
    @XmlElement
    private Charset encoding;

    @SuppressWarnings("unused")
    private LogbackListLogFilesInteractorFactory(){}
    public LogbackListLogFilesInteractorFactory(File searchPath, Charset encoding, String logLineFormat) {
        this.searchPath = searchPath;
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

        LogbackListLogFilesInteractorFactory that = (LogbackListLogFilesInteractorFactory) o;

        return encoding.equals(that.encoding)
           && logLineFormat.equals(that.logLineFormat)
           && searchPath.equals(that.searchPath);

    }

    @Override
    public int hashCode() {
        int result = logLineFormat.hashCode();
        result = 31 * result + searchPath.hashCode();
        result = 31 * result + encoding.hashCode();
        return result;
    }
}
