package com.crudetech.sample.logcrunch;

import java.io.File;

class TempDir extends File {
    TempDir() {
        super(System.getProperty("java.io.tmpdir"));
    }
}
