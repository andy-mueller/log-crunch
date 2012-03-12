package com.crudetech.sample.logcrunch;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

class TextFileLineIterable implements Iterable<String> {
    interface BufferedReaderProvider {
        BufferedReader newReader();
        void closeReader(BufferedReader reader);
    }

    private final BufferedReaderProvider readerProvider;

    TextFileLineIterable(BufferedReaderProvider readerProvider) {
        this.readerProvider = readerProvider;
    }

    @Override
    public Iterator<String> iterator() {
        return new LineIterator();
    }

    private class LineIterator implements java.util.Iterator<String> {
        private final BufferedReader reader;
        private String next = null;
        private boolean isPositioned = false;

        LineIterator() {
            this.reader = readerProvider.newReader();
            closeReaderOnEnd();
        }

        private void closeReaderOnEnd() {
            if (!hasNext()) {
                closeReader();
            }
        }

        @Override
        public boolean hasNext() {
            positionOnNextElement();
            return next != null;
        }

        private void positionOnNextElement() {
            if (!isPositioned) {
                next = readNextLine();
                isPositioned = true;
            }
        }

        private String readNextLine() {
            try {
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String next() {
            verifyHasNextElement();
            try {
                return next;
            } finally {
                moved();
                closeReaderOnEnd();
            }
        }

        private void moved() {
            isPositioned = false;
        }

        private void verifyHasNextElement() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
        }

        private void closeReader() {
            readerProvider.closeReader(reader);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
