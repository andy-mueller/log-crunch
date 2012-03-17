package com.crudetech.sample.logcrunch;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

import static com.crudetech.sample.Iterables.copy;


class LogFileMatcher extends TypeSafeMatcher<LogFile> {
    private final LogFile file;

    LogFileMatcher(LogFile file) {
        this.file = file;
    }

    @Override
    protected boolean matchesSafely(LogFile item) {
        List<LogLine> lines = copy(item.getLines());
        return lines.equals(copy(file.getLines()));
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(file);
    }
    static Matcher<LogFile> equalTo(final LogFile file) {
        return new LogFileMatcher(file);
    }
}
