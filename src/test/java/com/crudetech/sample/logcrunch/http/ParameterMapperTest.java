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
    @Test
    public void parameterIsMatchedToAnnotationWhenPresent(){
        class TestClass{
            private Integer item = 0;
            @Parameter("item")
            public void setItem(int item){
                this.item = item;
            }
        }

        TestClass instance = new TestClass();
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put("item", new String[]{"42"});
        ParameterMapper mapper = new ParameterMapper(parameters);

        mapper.mapTo(instance);

        assertThat(instance.item, is(42));
    }
}
