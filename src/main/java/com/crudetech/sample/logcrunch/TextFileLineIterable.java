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

    private static class LineIterator implements java.util.Iterator<String> {
        private final BufferedReader reader;
        private String next = null;
        private boolean isPositioned = false;

        private LineIterator(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public boolean hasNext() {
            if (!isPositioned) {
                next = readNextLine();
                isPositioned = true;
            }
            return next != null;
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
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            isPositioned = false;
            String current = next;
            if (!hasNext()) {
                closeReader();
            }
            return current;
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
