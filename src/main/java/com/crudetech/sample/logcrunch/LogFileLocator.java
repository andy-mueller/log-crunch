package com.crudetech.sample.logcrunch;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Andy
 * Date: 05.03.12
 * Time: 08:57
 * To change this template use File | Settings | File Templates.
 */
public interface LogFileLocator {
    LogFile find(String fileName, Date sixthOfJune);
}
