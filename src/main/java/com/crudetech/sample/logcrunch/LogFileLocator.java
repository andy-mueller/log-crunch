package com.crudetech.sample.logcrunch;

import java.util.Date;

public interface LogFileLocator {
    BufferedReaderLogFile find(String fileName, Date date);
}
