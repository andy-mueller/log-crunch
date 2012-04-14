package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.TempDir;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class LogbackLogFileFilterInteractorFactoryTest {
    @Test
    public void factoryCratesWiredInteractor(){
        LogbackLogFileFilterInteractorFactory factory =
                new LogbackLogFileFilterInteractorFactory(new TempDir(), Charset.forName("UTF-8"), "yyyyMMdd");

        assertThat(factory.createInteractor(), is(notNullValue()));
    }
}
