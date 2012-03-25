package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.FilterChain;
import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.Predicate;
import com.crudetech.sample.filter.PredicateBuilder;
import com.crudetech.sample.filter.UnaryFunction;
import org.joda.time.Interval;

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
    public Iterable<LogFile> find(LogFileNamePattern fileName, List<Interval> ranges) {
        Iterable<File> allPossibleFiles = allPossibleFilesInDirectory();

        FilterChain<File> fileFilterChain = new FilterChain<File>();
        PredicateBuilder<File> filterBuilder = fileFilterChain.filterBuilder();

        filterBuilder.start(fileNameMatches(fileName));

        filterBuilder.andOpenBrace(fileNameInDateRange(fileName, ranges.get(0)));
        for(int i = 1; i < ranges.size(); ++i){
            filterBuilder.or(fileNameInDateRange(fileName, ranges.get(i)));
        }
        filterBuilder.closeBrace();
        //filterBuilder.andOpenBrace().or(x).or(y)....closeBrace()
        //filterBuilder.andInBracesWithOr(x, y, z)

        Iterable<File> matchingFiles = fileFilterChain.apply(allPossibleFiles);

        return new MappingIterable<File, LogFile>(matchingFiles, createLogFile());
    }

    private Iterable<File> allPossibleFilesInDirectory() {
        List<File> allPossibleFiles = asList(logFilePath.listFiles());
        Collections.sort(allPossibleFiles);
        return allPossibleFiles;
    }

    private Predicate<File> fileNameInDateRange(final LogFileNamePattern fileName, final Interval range) {
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
