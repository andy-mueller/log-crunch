package com.crudetech.sample.logcrunch;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.crudetech.sample.Iterables.copy;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

public class TextFileLineIterableTest {
    @Test
    public void emptyReader_IteratorIsEmpty() throws Exception {
        Iterator<String> iterator = createIterator("");

        assertThat(iterator.hasNext(), is(false));
    }

    private Iterator<String> createIterator(String content) {
        BufferedReaderProviderStub stub = new BufferedReaderProviderStub(content);
        return new TextFileLineIterable(stub).iterator();
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void emptyReader_IteratorNextThrows() throws Exception {
        Iterator<String> iterator = createIterator("");

        expectedException.expect(NoSuchElementException.class);
        iterator.next();
    }

    @Test
    public void oneLine_IteratorIsNotEmpty() throws Exception {
        Iterator<String> iterator = createIterator("text");

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.hasNext(), is(true));
    }

    @Test
    public void oneLine_IteratorReturnsThatLine() throws Exception {
        Iterator<String> iterator = createIterator("text");

        assertThat(iterator.next(), is("text"));
    }

    @Test
    public void oneLine_IteratorEndsAfterLine() throws Exception {
        Iterator<String> iterator = createIterator("text");
        iterator.next();

        assertThat(iterator.hasNext(), is(false));
        expectedException.expect(NoSuchElementException.class);
        iterator.next();
    }

    @Test
    public void twoLines_IteratorEndsAfterLine2() throws Exception {
        Iterator<String> iterator = createIterator("line1\nline2");

        assertThat(iterator.next(), is("line1"));
        assertThat(iterator.next(), is("line2"));

        expectedException.expect(NoSuchElementException.class);
        iterator.next();
    }

    @Test
    public void twoLines_IteratorActs() throws Exception {
        Iterator<String> iterator = createIterator("line1\nline2");

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
        ArrayList<String> actual = new ArrayList<String>();
        Iterator<String> iterator = createIterator("line1\nline2\nline3");

        while (iterator.hasNext()) {
            actual.add(iterator.next());
        }

        assertThat(actual, is(asList("line1", "line2", "line3")));
    }


    static class BufferedReaderProviderStub implements TextFileLineIterable.BufferedReaderProvider {
        private final String content;
        private final Set<BufferedReader> trackedReaders = new HashSet<BufferedReader>();

        BufferedReaderProviderStub(String content) {
            this.content = content;
        }

        @Override
        public BufferedReader newReader() {
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes("UTF-8"))));
                trackedReaders.add(r);
                return r;
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void closeReader(BufferedReader reader) {
            trackedReaders.remove(reader);
        }
    }

    @Test
    public void closesReaderAfterIteratorFinishes() throws Exception {
        BufferedReaderProviderStub providerStub = new BufferedReaderProviderStub("line1\nline2");
        assertThat(providerStub.trackedReaders.isEmpty(), is(true));

        Iterator<String> iterator = new TextFileLineIterable(providerStub).iterator();


        iterator.next();
        assertThat(providerStub.trackedReaders.size(), is(1));

        iterator.next();
        assertThat(providerStub.trackedReaders.isEmpty(), is(true));
    }

    @Test
    public void emptyReader_closesReaderOnConstruction() throws Exception {
        BufferedReaderProviderStub providerStub = new BufferedReaderProviderStub("");
        new TextFileLineIterable(providerStub).iterator();

        assertThat(providerStub.trackedReaders.isEmpty(), is(true));
    }

    @Test
    public void multipleLines_IterableDoesIterate() throws Exception {
        ArrayList<String> actual = new ArrayList<String>();
        for (String s : new TextFileLineIterable(new BufferedReaderProviderStub("line1\nline2\nline3"))) {
            actual.add(s);
        }

        assertThat(actual, is(asList("line1", "line2", "line3")));
    }

    @Test
    public void allowsMultipleIterations() throws Exception {
        TextFileLineIterable.BufferedReaderProvider providerStub = new BufferedReaderProviderStub("line1\nline2\nline3");
        TextFileLineIterable iterable = new TextFileLineIterable(providerStub);

        Iterator<String> it1= iterable.iterator();
        Iterator<String> it2= iterable.iterator();
        
        assertThat(it1, is(not(sameInstance(it2))));

        List<String> copy1 = copy(it1);
        List<String> copy2 = copy(it2);
        
        assertThat(copy1, is(copy2));
        assertThat(copy1, is(asList("line1", "line2", "line3")));
    }

    @Test
    public void providerIsCalledOnMultipleIterations() throws Exception {
        BufferedReaderProviderStub providerStub = new BufferedReaderProviderStub("text");
        TextFileLineIterable iterable = new TextFileLineIterable(providerStub);

        Iterator<String> it1= iterable.iterator();
        Iterator<String> it2= iterable.iterator();

        assertThat(providerStub.trackedReaders.size(), is(2));

        iterate(it1);
        iterate(it2);

        assertThat(providerStub.trackedReaders.isEmpty(), is(true));
    }

    private void iterate(Iterator<String> iterator) {
        copy(iterator);
    }
}
