package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.TempDir;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class LogbackLogFileInteractorFactoryTest {
    @Test
    public void factoryCratesWiredInteractor(){
        LogbackLogFileInteractorFactory factory =
                new LogbackLogFileInteractorFactory(new TempDir(), Charset.forName("UTF-8"), "yyyyMMdd");

        assertThat(factory.createInteractor(), is(notNullValue()));
    }
}
