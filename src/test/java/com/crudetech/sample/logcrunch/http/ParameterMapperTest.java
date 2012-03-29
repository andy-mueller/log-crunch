package com.crudetech.sample.logcrunch.http;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ParameterMapperTest {
    @Test
    public void ctor(){
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        ParameterMapper mapper = new ParameterMapper(parameters);
        assertThat(mapper, is(notNullValue()));
    }
}
