package com.crudetech.sample.logcrunch;

import com.crudetech.sample.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

class FileTestLogFile extends TestLogFile {
    static final File Directory = new File(new TempDir(), "testLogFiles");

    private File file;

    FileTestLogFile(String name) {
        super(name);
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        if (!Directory.exists()) {
            if(!Directory.mkdir()){
                throw new RuntimeException("Could not create directory " + Directory);
            }
        }
        file = new File(Directory, name);
        writeLinesToTestLogFile();
    }

    private void writeLinesToTestLogFile() throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), Encoding));
        try {
            for (LogLine logLine : logLines) {
                logLine.print(writer);
                writer.println();
            }
        } finally {
            writer.close();
        }
    }

    @Override
    protected void after() {
        super.after();
        if (!file.exists()) {
            throw new RuntimeException("The test file does not exist. Cannot delete it!");
        }
        if (!file.delete()) {
            throw new RuntimeException("Could not delete file. File is probably still open!");
        }
        if (file.exists()) {
            throw new RuntimeException("The test file does still not exist");
        }
    }

    File getFile() {
        return file;
    }
}
