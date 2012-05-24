package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.BufferedReaderLogFile;
import com.crudetech.sample.logcrunch.FileSystemListLogFilesInteractorFactory;
import com.crudetech.sample.logcrunch.FileTestLogFile;
import com.crudetech.sample.logcrunch.TestLogFile;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.nio.charset.Charset;


@XmlRootElement(name = "listLogFileInteractorFactory")
class ListLogFilesInteractorFactoryStub extends FileSystemListLogFilesInteractorFactory {

    @Override
    protected File getSearchPath() {
        return FileTestLogFile.Directory;
    }

    @Override
    protected Charset getEncoding() {
        return Charset.forName("UTF-8");
    }

    @Override
    protected BufferedReaderLogFile.LogLineFactory logLineFactory() {
        return TestLogFile.logLineFactory();
    }
}
