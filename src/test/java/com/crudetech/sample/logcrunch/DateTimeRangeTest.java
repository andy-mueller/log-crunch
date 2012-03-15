package com.crudetech.sample.logcrunch;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DateTimeRangeTest {

    private Date start;
    private DateTimeRange range;

    @Before
    public void setUp() throws Exception {
        start = new Date(100);
        Date end = new Date(1000);
        range = new DateTimeRange(start, end);
    }

    @Test
    public void dateOnStart() {
        assertThat(range.contains((Date) start.clone()), is(true));
    }
    @Test
    public void dateBeforeStart() {
        assertThat(range.contains(new Date(10)), is(false));
    }

    @Test
    public void dateInRange() {
        assertThat(range.contains(new Date(500)), is(true));
    }
    @Test
    public void dateOnEnd() {
        assertThat(range.contains(new Date(1000)), is(false));
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

    @Test
    public void constructionWithOnePointInTime() throws Exception {
        Date now = new Date();
        DateTimeRange range = new DateTimeRange(now);

        assertThat(range.contains((Date) now.clone()), is(true));
    }
}
