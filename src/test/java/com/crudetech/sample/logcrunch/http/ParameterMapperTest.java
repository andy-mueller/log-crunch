package com.crudetech.sample.logcrunch.http;

import org.junit.Test;

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
}
