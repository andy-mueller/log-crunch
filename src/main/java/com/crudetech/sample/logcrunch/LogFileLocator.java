package com.crudetech.sample.logcrunch;

import java.util.Date;

public interface LogFileLocator {
    LogFile find(String fileName, Date date);
}
