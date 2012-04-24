package com.crudetech.sample.logcrunch.logback;

import org.junit.Test;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

public class CharsetXmlAdapterTest {

    static class Xox {
        @XmlJavaTypeAdapter(CharsetXmlAdapter.class)
        @XmlElement
        private final Charset encoding;
        private Xox(){encoding = null;}
        Xox(Charset encoding) {
            this.encoding = encoding;
        }
    }

    @Test
    public void jaxbEncoding() {
        Xox xox = new Xox(Charset.forName("UTF-8"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JAXB.marshal(xox, out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        Xox unMarshaledXox = JAXB.unmarshal(in, Xox.class);
        assertThat(xox, is(not(sameInstance(unMarshaledXox))));
        assertThat(xox, is(not(nullValue())));
        assertThat(xox.encoding, is(not(nullValue())));
        assertThat(unMarshaledXox.encoding, is(xox.encoding));
    }
}
