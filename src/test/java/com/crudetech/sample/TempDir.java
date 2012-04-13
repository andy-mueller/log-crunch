package com.crudetech.sample;

import java.io.File;

public class TempDir extends File {
     public TempDir() {
        super(System.getProperty("java.io.tmpdir"));
    }
}
