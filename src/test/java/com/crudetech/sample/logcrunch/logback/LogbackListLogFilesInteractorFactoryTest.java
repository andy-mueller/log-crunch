package com.crudetech.sample.logcrunch.logback;

import com.crudetech.sample.TempDir;
import com.crudetech.sample.logcrunch.InteractorFactory;
import com.crudetech.sample.logcrunch.ListLogFilesInteractor;
import org.junit.Test;

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

public class LogbackListLogFilesInteractorFactoryTest {
    @Test
    public void factoryCratesWiredInteractor() {
        LogbackListLogFilesInteractorFactory factory =
                new LogbackListLogFilesInteractorFactory(new TempDir(), Charset.forName("UTF-8"), "yyyyMMdd");

        assertThat(factory.createInteractor(), is(notNullValue()));
    }


    @Test
    public void jaxbSerialization() throws Exception {
        InteractorFactory<ListLogFilesInteractor> factoryLogFile =
                new LogbackListLogFilesInteractorFactory(new TempDir(), Charset.forName("UTF-8"), "yyyyMMdd");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JAXB.marshal(factoryLogFile, out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        InteractorFactory<ListLogFilesInteractor> unmarshalledFactoryLogFile = JAXB.unmarshal(in, LogbackListLogFilesInteractorFactory.class);

        assertThat(unmarshalledFactoryLogFile, is(not(sameInstance(factoryLogFile))));
        assertThat(unmarshalledFactoryLogFile, is(notNullValue()));
        assertThat(unmarshalledFactoryLogFile, is(factoryLogFile));
    }
}

