package com.crudetech.sample.logcrunch;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DateTimeRangeTest {
    @Test
    public void rangeIsOpenInterval() {
        Date start = new Date(0);
        Date end = new Date(1000);

        DateTimeRange range = new DateTimeRange(start, end);

        assertThat(range.contains((Date) start.clone()), is(true));
        assertThat(range.contains((Date) end.clone()), is(false));
        assertThat(range.contains(new Date(20)), is(false));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void containsThrowsOnNull() {
        Date start = new Date(0);
        Date end = new Date(1000);

        DateTimeRange range = new DateTimeRange(start, end);

        expectedException.expect(IllegalArgumentException.class);
        range.contains(null);
    }

    @Test
    public void nullForStartThrows() {
        Date end = new Date(1000);

        expectedException.expect(IllegalArgumentException.class);
        new DateTimeRange(null, end);
    }

    @Test
    public void nullForEndThrows() {
        Date start = new Date(1000);

        expectedException.expect(IllegalArgumentException.class);
        new DateTimeRange(start, null);
    }
}
