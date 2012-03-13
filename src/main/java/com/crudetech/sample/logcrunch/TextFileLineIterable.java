package com.crudetech.sample.logcrunch;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

class TextFileLineIterable implements Iterable<String> {
    interface BufferedReaderProvider {
        BufferedReader newReader();
        void closeReader(BufferedReader reader);
        boolean isClosed(BufferedReader reader);
    }

    private final BufferedReaderProvider readerProvider;

    TextFileLineIterable(BufferedReaderProvider readerProvider) {
        this.readerProvider = readerProvider;
    }

    @Override
    public Iterator<String> iterator() {
        return new LineIterator();
    }

    private class LineIterator implements Iterator<String> {
        private final BufferedReader reader;
        private String next = null;
        private boolean isPositioned = false;
        private boolean closedReader = false;

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
            if (isBufferedReaderClosed()) {
                return false;
            }
            positionOnNextElement();
            return next != null;
        }

        private boolean isBufferedReaderClosed() {
            return readerProvider.isClosed(reader);
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
            verifyConcurrentModification();
            verifyHasNextElement();
            try {
                return next;
            } finally {
                moved();
                closeReaderOnEnd();
            }
        }

        private void verifyConcurrentModification() {
            if(isBufferedReaderClosed() && !closedReader){
                throw new ConcurrentModificationException();
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
            closedReader = true;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
