package com.crudetech.sample.logcrunch;


import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TrackingBufferedReaderProviderTest {

    private TrackingBufferedReaderProvider provider;

    @Before
    public void setUp() throws Exception {
        provider = new TrackingBufferedReaderProvider() {
            @Override
            Reader createNewReader() {
                return new InputStreamReader(new ByteArrayInputStream("line1\nline2".getBytes()));
            }
        };
    }

    @Test
    public void createdProviderIsTracked(){
        BufferedReader r = provider.newReader();
        assertThat(provider.isClosed(r), is(false));
    }
    @Test
    public void closingRemovesFromTracking(){
        BufferedReader r = provider.newReader();
        provider.closeReader(r);
        assertThat(provider.isClosed(r), is(true));
    }
    @Test
    public void closingAllRemovesFromTracking(){
        BufferedReader r = provider.newReader();
        provider.closeAllReaders();
        assertThat(provider.isClosed(r), is(true));
    }
    
    @Test
    public void closingReaderRemovesFromTracking() throws IOException {
        BufferedReader r = provider.newReader();
        r.close();
        assertThat(provider.isClosed(r), is(true));
    }
}
