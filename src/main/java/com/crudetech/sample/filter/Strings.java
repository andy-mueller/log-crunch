package com.crudetech.sample.filter;

import java.util.regex.Pattern;

public class Strings {
    public static BinaryFunction<String, ? super String, ? super String> concat() {
        return new BinaryFunction<String, String, String>() {
            @Override
            public String evaluate(String argument1, String argument2) {
                return argument1 + argument2;
            }
        };
    }

    public static UnaryFunction<String, String> regexQuote() {
        return new UnaryFunction<String, String>() {
            @Override
            public String evaluate(String argument) {
                return Pattern.quote(argument) + ".*";
            }
        };
    }
}
