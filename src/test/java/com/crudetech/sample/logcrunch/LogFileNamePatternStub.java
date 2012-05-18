package com.crudetech.sample.logcrunch;

import org.joda.time.DateTime;

class LogFileNamePatternStub implements LogFileNamePattern {

    private final boolean doesMatch;
    private final DateTime dateOf;

    LogFileNamePatternStub() {
        this(false, new DateTime(0));
    }

    LogFileNamePatternStub(boolean doesMatch, DateTime dateOf) {
        this.doesMatch = doesMatch;
        this.dateOf = dateOf;
    }

    @Override
    public boolean matches(String fileName) {
        return doesMatch;
    }

    @Override
    public DateTime dateOf(String fileName) {
        return dateOf;
    }
}
