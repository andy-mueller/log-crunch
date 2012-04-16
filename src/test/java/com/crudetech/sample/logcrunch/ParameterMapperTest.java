package com.crudetech.sample.logcrunch;

import org.joda.time.Interval;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
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
            public void setItem(Integer item){
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

    @Test
    public void primitivesAreConverted(){
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
    private static enum AnEnum {
        E1, E2
    }
    @Test
    public void enumsAreMapped(){

        class TestClass{
            private AnEnum item = AnEnum.E1;
            @Parameter("anEnum")
            public void setItem(AnEnum e){
                this.item = e;
            }
        }

        TestClass instance = new TestClass();
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put("anEnum", new String[]{"E2"});

        ParameterMapper mapper = new ParameterMapper(parameters);

        mapper.mapTo(instance);

        assertThat(instance.item, is(AnEnum.E2));
    }

    @Test
    public void multipleParametersValuesCallMethodForEach(){
        class TestClass{
            private List<Integer> items = new ArrayList<Integer>();
            @Parameter("item")
            public void setItem(int item){
                items.add(item);
            }
        }

        TestClass instance = new TestClass();
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put("item", new String[]{"42", "55"});

        ParameterMapper mapper = new ParameterMapper(parameters);

        mapper.mapTo(instance);

        assertThat(instance.items, is(asList(42, 55)));
    }

    @Test
    public void stringsAreMapped(){

        class TestClass{
            private String item = "";
            @Parameter("aString")
            public void setItem(String e){
                this.item = e;
            }
        }

        TestClass instance = new TestClass();
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put("aString", new String[]{"Some Text"});

        ParameterMapper mapper = new ParameterMapper(parameters);

        mapper.mapTo(instance);

        assertThat(instance.item, is("Some Text"));
    }

    class IntervalData {
        private Interval intervalWithParseMethod;
        @Parameter("aInterval")
        public void setItem(Interval i){
            this.intervalWithParseMethod = i;
        }
    }
    @Test
    public void otherFactoryMethods(){
        IntervalData instance = new IntervalData();
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put("aInterval", new String[]{"2007-05-07T13:55:22,100/2009-07-02"});

        ParameterMapper mapper = new ParameterMapper(parameters);
        mapper.registerParameterFactory(Interval.class, new ParameterMapper.ReflectionParameterFactory("parse", String.class));


        mapper.mapTo(instance);

        assertThat(instance.intervalWithParseMethod, is(Interval.parse("2007-05-07T13:55:22,100/2009-07-02")));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void badParametersThrow(){

        IntervalData instance = new IntervalData();
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put("aInterval", new String[]{"Invalid stuff that cannot be parsed!"});

        ParameterMapper mapper = new ParameterMapper(parameters);
        mapper.registerParameterFactory(Interval.class, new ParameterMapper.ReflectionParameterFactory("parse", String.class));

        expectedException.expect(ParameterMapper.BadFormatException.class);
        mapper.mapTo(instance);
    }

    @Test
    public void allParametersAreRequiredByDefault(){
        class TestClass{
            private int item = 42;
            @Parameter("unused")
            public void setItem(int item){
                this.item = item;
            }
        }
        TestClass instance = new TestClass();
        Map<String, String[]> parameters = new HashMap<String, String[]>();

        ParameterMapper mapper = new ParameterMapper(parameters);

        expectedException.expect(ParameterMapper.NoParameterException.class);
        mapper.mapTo(instance);
    }
    @Test
    public void parametersCanBeNotRequired(){
        class TestClass{
            private int item = 42;
            @Parameter(value= "unused", required = false)
            public void setItem(int item){
                this.item = item;
            }
        }
        TestClass instance = new TestClass();
        Map<String, String[]> parameters = new HashMap<String, String[]>();

        ParameterMapper mapper = new ParameterMapper(parameters);

        mapper.mapTo(instance);

        assertThat(instance.item, is(42));
    }
}
