package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.FilterIterable;
import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.Predicate;
import com.crudetech.sample.filter.UnaryFunction;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public class FileSystemLogFileLocator implements LogFileLocator {
    private final File logFilePath;
    private final LogFileFactory logFileFactory;

    interface LogFileFactory {
        LogFile create(File logFile);
    }

    public FileSystemLogFileLocator(File logFilePath, LogFileFactory logFileFactory) {
        this.logFilePath = logFilePath;
        this.logFileFactory = logFileFactory;
    }

    @Override
    public Iterable<LogFile> find(LogFileNamePattern fileName, DateTimeRange range) {
        List<File> allPossibleFiles = asList(logFilePath.listFiles());
        Collections.sort(allPossibleFiles);
        Iterable<File> matchingNameFiles = new FilterIterable<File>(allPossibleFiles, fileNameMatches(fileName));
        Iterable<File> matchingDateFiles = new FilterIterable<File>(matchingNameFiles, fileNameInDateRange(fileName, range));

        return new MappingIterable<File, LogFile>(matchingDateFiles, createLogFile());
    }

    private Predicate<File> fileNameInDateRange(final LogFileNamePattern fileName, final DateTimeRange range) {
        return new Predicate<File>() {
            @Override
            public Boolean evaluate(File argument) {
                return range.contains(fileName.dateOf(argument.getName()));
            }
        };
    }

    private UnaryFunction<LogFile, File> createLogFile() {
        return new UnaryFunction<LogFile, File>() {
            @Override
            public LogFile evaluate(File logFile) {
                return logFileFactory.create(logFile);
            }
        };
    }

    private Predicate<File> fileNameMatches(final LogFileNamePattern fileName) {
        return new Predicate<File>() {
            @Override
            public Boolean evaluate(File argument) {
                return fileName.matches(argument.getName());
            }
        };
    }
}
