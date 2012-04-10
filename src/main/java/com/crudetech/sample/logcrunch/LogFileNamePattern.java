package com.crudetech.sample.logcrunch;

import org.joda.time.DateTime;

public interface LogFileNamePattern {
    boolean matches(String fileName);

    DateTime dateOf(String fileName);
}
