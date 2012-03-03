package com.crudetech.sample.logcrunch;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BufferedReaderIterableTest {
    @Test
    public void emptyReader_IteratorIsEmpty() throws Exception {
        BufferedReader reader = readerFromString("");
        BufferedReaderIterable bi = new BufferedReaderIterable(reader);

        assertThat(bi.iterator().hasNext(), is(false));
    }

    private BufferedReader readerFromString(String s) {
        try {
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes("UTF-8"))));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void emptyReader_IteratorNextThrows() throws Exception {
        BufferedReader reader = readerFromString("");
        BufferedReaderIterable bi = new BufferedReaderIterable(reader);

        expectedException.expect(NoSuchElementException.class);
        bi.iterator().next();
    }

    @Test
    public void oneLine_IteratorIsNotEmpty() throws Exception {
        BufferedReader reader = readerFromString("text");
        Iterator<String> iterator = new BufferedReaderIterable(reader).iterator();

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.hasNext(), is(true));
    }

    @Test
    public void oneLine_IteratorReturnsThatLine() throws Exception {
        BufferedReader reader = readerFromString("text");
        Iterator<String> iterator = new BufferedReaderIterable(reader).iterator();

        assertThat(iterator.next(), is("text"));
    }

    @Test
    public void oneLine_IteratorEndsAfterLine() throws Exception {
        BufferedReader reader = readerFromString("text");
        Iterator<String> iterator = new BufferedReaderIterable(reader).iterator();
        iterator.next();

        assertThat(iterator.hasNext(), is(false));
        expectedException.expect(NoSuchElementException.class);
        iterator.next();
    }
    @Test
    public void twoLines_IteratorEndsAfterLine2() throws Exception {
        BufferedReader reader = readerFromString("line1\nline2");
        Iterator<String> iterator = new BufferedReaderIterable(reader).iterator();

        assertThat(iterator.next(), is("line1"));
        assertThat(iterator.next(), is("line2"));

        expectedException.expect(NoSuchElementException.class);
        iterator.next();
    }

    @Test
    public void twoLines_IteratorActs() throws Exception {
        BufferedReader reader = readerFromString("line1\nline2");
        Iterator<String> iterator = new BufferedReaderIterable(reader).iterator();

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is("line1"));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is("line2"));
        assertThat(iterator.hasNext(), is(false));
        assertThat(iterator.hasNext(), is(false));

        expectedException.expect(NoSuchElementException.class);
        iterator.next();
    }

    @Test
    public void multipleLines_IteratorDoesIterate() throws Exception {
        BufferedReader reader = readerFromString("line1\nline2\nline3");
        ArrayList<String> actual = new ArrayList<String>();

        for (String s : new BufferedReaderIterable(reader)) {
            actual.add(s);
        }

        assertThat(actual, is(asList("line1", "line2", "line3")));
    }


    @Test
    public void closesReaderAfterIteratorFinishes() throws Exception {
        BufferedReader reader = spy(readerFromString("line1\nline2"));
        Iterator<String> iterator = new BufferedReaderIterable(reader).iterator();

        iterator.next();
        verify(reader, never()).close();

        iterator.next();
        verify(reader, times(1)).close();
    }
}
