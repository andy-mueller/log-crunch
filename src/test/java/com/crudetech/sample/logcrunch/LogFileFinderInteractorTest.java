package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.FilterChain;
import com.crudetech.sample.filter.Predicate;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.crudetech.sample.Iterables.copy;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogFileFinderInteractorTest {

    @Test
    public void findFiles() {
        Date date = new Date();
        BufferedReaderLogFile logFileStub = mock(FileLogFile.class);
        LogFileLocator locator = mock(LogFileLocator.class);
        when(locator.find("machine101", date)).thenReturn(logFileStub);

        @SuppressWarnings("unchecked")
        FilterChain<StringLogLine> infoFilter = mock(FilterChain.class);
        LogFileFinderInteractor interactor = new LogFileFinderInteractor(locator, infoFilter);

        Iterable<BufferedReaderLogFile> logFiles = interactor.getLogFiles("machine101", date);

        assertThat(getFirst(logFiles), is(logFileStub));
        assertThat(size(logFiles), is(1));
    }

    private int size(Iterable<?> iterable) {
        int count = 0;
        Iterator<?> it = iterable.iterator();
        while (it.hasNext()){ count ++; it.next();}
        return count;
    }

    static class StringLogFile extends BufferedReaderLogFile {
        private final String content;

        public StringLogFile(LogLineFactory logLineFactory, String content) {
            super(logLineFactory);
            this.content = content;
        }

        @Override
        protected BufferedReader createNewReader() {
            return new BufferedReader(
                    new StringReader(content)
            );
        }
    }

    @Test
    public void foundFilesAreFiltered() {
        Date date = new Date();
        BufferedReaderLogFile.LogLineFactory loglineFactory = new BufferedReaderLogFile.LogLineFactory() {
            @Override
            public StringLogLine newLogLine(String lineContent) {
                return new StringLogLine(lineContent, new SimpleDateFormat("yyyMMdd"));
            }
        };
        String content = TestLogFile.Line1 + "\n" + TestLogFile.Line2;
        BufferedReaderLogFile logFileStub = new StringLogFile(loglineFactory, content);
        LogFileLocator locator = mock(LogFileLocator.class);
        when(locator.find("machine101", date)).thenReturn(logFileStub);

        Predicate<StringLogLine> isInfo = new Predicate<StringLogLine>() {
            @Override
            public boolean evaluate(StringLogLine item) {
                return item.getLogLevel().equals("INFO");
            }
        };

        @SuppressWarnings("unchecked")
        Collection<Predicate<StringLogLine>> predicates = asList(isInfo);
        FilterChain<StringLogLine> infoFilter = new FilterChain<StringLogLine>(predicates);

        LogFileFinderInteractor interactor = new LogFileFinderInteractor(locator, infoFilter);

        Iterable<BufferedReaderLogFile> logFiles = interactor.getLogFiles("machine101", date);

        BufferedReaderLogFile foundFile = getFirst(logFiles);

        @SuppressWarnings("unchecked") // hate this!
        List<StringLogLine> logLines = (List<StringLogLine>) copy(foundFile.getLines());

        List<StringLogLine> expected = asList(loglineFactory.newLogLine(TestLogFile.Line1));
        assertThat(logLines, is(expected));
    }

    private <T> T getFirst(Iterable<T> i) {
        return i.iterator().next();
    }

}
