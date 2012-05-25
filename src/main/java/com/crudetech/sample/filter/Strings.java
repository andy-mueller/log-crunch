package com.crudetech.sample.filter;

import java.util.regex.Pattern;

public class Strings {
    public static final String LineSeparator = "\n";

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

    public static BinaryFunction<StringBuilder, ? super StringBuilder, CharSequence> concat(final CharSequence linebreak) {
        return new BinaryFunction<StringBuilder, StringBuilder, CharSequence>() {
            @Override
            public StringBuilder evaluate(StringBuilder sb, CharSequence s) {
                return sb.append(s).append(linebreak);
            }
        };
    }

}
