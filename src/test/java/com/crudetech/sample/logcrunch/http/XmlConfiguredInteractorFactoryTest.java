package com.crudetech.sample.logcrunch.http;

import com.crudetech.sample.logcrunch.FilterLogFileInteractor;
import com.crudetech.sample.logcrunch.InteractorFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class XmlConfiguredInteractorFactoryTest {
    @Rule
    public ResourceStream xmlConfig = new ResourceStream("/logfilter-logcrunch-test.xml");

    @Test
    public void constructsFromXml() {
        XmlConfiguredInteractorFactory<FilterLogFileInteractor> xmlConfiguredFactory =
                new XmlConfiguredInteractorFactory<FilterLogFileInteractor>(xmlConfig.resourceStream);

        InteractorFactory<FilterLogFileInteractor> innerFactoryLogFile = xmlConfiguredFactory.getDecorated();


        InteractorFactory<FilterLogFileInteractor> expected = new FilterLogFileInteractorFactoryStub(42);

        assertThat(innerFactoryLogFile, is(expected));
    }

    @Test
    public void jaxbDecoratorForwardsToInnerInteractor() {
        XmlConfiguredInteractorFactory<FilterLogFileInteractor> xmlConfiguredFactory =
                new XmlConfiguredInteractorFactory<FilterLogFileInteractor>(xmlConfig.resourceStream);

        xmlConfiguredFactory.createInteractor();
        FilterLogFileInteractorFactoryStub innerFactory = (FilterLogFileInteractorFactoryStub) xmlConfiguredFactory.getDecorated();
        assertThat(innerFactory.createInteractorCalled, is(1));
    }

    private static class ResourceStream extends ExternalResource {
        private InputStream resourceStream;
        private final String resourceName;

        ResourceStream(String resourceName) {
            this.resourceName = resourceName;
        }

        @Override
        protected void before() throws Throwable {
            resourceStream = getClass().getResourceAsStream(resourceName);
        }

        @Override
        protected void after() {
            try {
                resourceStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
