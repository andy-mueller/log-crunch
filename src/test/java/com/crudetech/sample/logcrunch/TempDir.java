package com.crudetech.sample.logcrunch;

import java.io.File;
import java.io.IOException;

class TempDir extends File {
    TempDir() {
        super(tempDirName());
    }

    private static String tempDirName() {
        File tmp = createTempFile("unused");
        String tmpDir = tmp.getParent();
        delete(tmp);
        return  tmpDir;
    }

    private static void delete(File file) {
        if(!file.delete()){
            throw new RuntimeException();
        }
    }

    private static File createTempFile(String name) {
        try {
            return File.createTempFile(name, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
