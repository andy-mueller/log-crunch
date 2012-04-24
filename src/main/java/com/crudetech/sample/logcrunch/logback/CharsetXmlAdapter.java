package com.crudetech.sample.logcrunch.logback;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.nio.charset.Charset;

public class CharsetXmlAdapter extends XmlAdapter<String, Charset> {
    @Override
    public Charset unmarshal(String charsetName) throws Exception {
        return Charset.forName(charsetName)    ;
    }

    @Override
    public String marshal(Charset charset) throws Exception {
        return charset.name();
    }
}