package com.crudetech.sample.logcrunch;

import java.util.Date;

public class DateTimeRange {
    private final Date start;
    private final Date end;

    public DateTimeRange(Date start, Date end) {
        if (start == null) {
            throw new IllegalArgumentException();
        }
        if (end == null) {
            throw new IllegalArgumentException();
        }
        this.start = (Date) start.clone();
        this.end = (Date) end.clone();
    }

    public boolean contains(Date date) {
        if (date == null) {
            throw new IllegalArgumentException();
        }
        return (!date.before(start) || start.equals(date)) && date.before(end);
    }
}
