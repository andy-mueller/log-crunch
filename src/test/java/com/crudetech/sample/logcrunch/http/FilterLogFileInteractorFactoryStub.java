package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.BufferedReaderLogFile;
import com.crudetech.sample.logcrunch.FileSystemFilterLogFileInteractorFactory;
import com.crudetech.sample.logcrunch.FileTestLogFile;
import com.crudetech.sample.logcrunch.FilterLogFileInteractor;
import com.crudetech.sample.logcrunch.TestLogFile;

import javax.xml.bind.annotation.XmlElement;
import java.io.File;
import java.nio.charset.Charset;


class FilterLogFileInteractorFactoryStub extends FileSystemFilterLogFileInteractorFactory {
    @XmlElement
    private final int aMember;
    transient int createInteractorCalled = 0;


    public FilterLogFileInteractorFactoryStub(int i) {
        aMember = i;
    }

    private FilterLogFileInteractorFactoryStub() {
        aMember = 0;
    }

    @Override
    public FilterLogFileInteractor createInteractor() {
        createInteractorCalled++;
        return super.createInteractor();
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterLogFileInteractorFactoryStub that = (FilterLogFileInteractorFactoryStub) o;
        return aMember == that.aMember;
    }

    @Override
    public int hashCode() {
        return aMember;
    }
}
