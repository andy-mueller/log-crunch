package com.crudetech.sample.logcrunch.logback;

import com.crudetech.sample.TempDir;
import com.crudetech.sample.logcrunch.LogFileFilterInteractorFactory;
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

public class LogbackLogFileFilterInteractorFactoryTest {
    @Test
    public void factoryCratesWiredInteractor() {
        LogbackLogFileFilterInteractorFactory factory =
                new LogbackLogFileFilterInteractorFactory(new TempDir(), Charset.forName("UTF-8"), "yyyyMMdd");

        assertThat(factory.createInteractor(), is(notNullValue()));
    }


    @Test
    public void jaxbSerialization() throws Exception {
        LogFileFilterInteractorFactory factory =
                new LogbackLogFileFilterInteractorFactory(new TempDir(), Charset.forName("UTF-8"), "yyyyMMdd");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JAXB.marshal(factory, out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        LogFileFilterInteractorFactory unmarshalledFactory = JAXB.unmarshal(in, LogbackLogFileFilterInteractorFactory.class);

        assertThat(unmarshalledFactory, is(not(sameInstance(factory))));
        assertThat(unmarshalledFactory, is(notNullValue()));
        assertThat(unmarshalledFactory, is(factory));


    }
}

