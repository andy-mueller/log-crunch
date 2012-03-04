package com.crudetech.sample.logcrunch;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

class TextFileLineIterable implements Iterable<String> {
    private final BufferedReader reader;

    TextFileLineIterable(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public Iterator<String> iterator() {
        return new LineIterator(reader);
    }

    static class LineIterator implements java.util.Iterator<String> {
        private final BufferedReader reader;
        private String next = null;
        private boolean isPositioned = false;

        LineIterator(BufferedReader reader) {
            this.reader = reader;
            closeReaderOnEnd();
        }

        private void closeReaderOnEnd() {
            if(!hasNext()){
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

            String current = next;

            moved();

            closeReaderOnEnd();
            return current;
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
            try {
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}