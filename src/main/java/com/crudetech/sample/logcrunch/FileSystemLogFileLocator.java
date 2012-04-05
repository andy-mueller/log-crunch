package com.crudetech.sample.logcrunch;

import com.crudetech.sample.filter.BinaryFunction;
import com.crudetech.sample.filter.FilterChain;
import com.crudetech.sample.filter.MappingIterable;
import com.crudetech.sample.filter.Predicate;
import com.crudetech.sample.filter.PredicateBuilder;
import com.crudetech.sample.filter.UnaryFunction;
import org.joda.time.Interval;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.crudetech.sample.filter.Algorithm.accumulate;
import static java.util.Arrays.asList;

public class FileSystemLogFileLocator implements LogFileLocator {
    private final File logFilePath;
    private final LogFileFactory logFileFactory;

    interface LogFileFactory {
        LogFile create(File logFile);
    }

    public FileSystemLogFileLocator(File logFilePath, LogFileFactory logFileFactory) {
        if(!logFilePath.isDirectory()){
            throw new IllegalArgumentException(String.format("\"%s\" is not a directory", logFilePath));
        }
        this.logFilePath = logFilePath;
        this.logFileFactory = logFileFactory;
    }

    @Override
    public Iterable<LogFile> find(LogFileNamePattern fileName, Iterable<Interval> ranges) {
        Iterable<File> allPossibleFiles = allPossibleFilesInDirectory();

        FilterChain<File> fileFilterChain = new FilterChain<File>();
        PredicateBuilder<File> filterBuilder = fileFilterChain.filterBuilder();

        filterBuilder.start(fileNameMatches(fileName));
        //JDK8: filterBuilder.start( (File f)=>fileName.matches(f.getName()); );

        accumulate(filterBuilder.andOpenBrace(), ranges, addRangeWithOr(fileName)).closeBrace();
        // JDK8: accumulate(filterBuilder.andOpenBrace(), ranges, (File f)=>builder.or(range.contains(fileName.dateOf(f.getName());).closeBrace();

        Iterable<File> matchingFiles = fileFilterChain.apply(allPossibleFiles);

        return new MappingIterable<File, LogFile>(matchingFiles, createLogFile());
        //JDK8: new MappingIterable<File, LogFile>(matchingFiles, (File f) => logFileFactory.create(f); );
    }

    private BinaryFunction<PredicateBuilder<File>, PredicateBuilder<File>, Interval> addRangeWithOr(final LogFileNamePattern fileName) {
        return new BinaryFunction<PredicateBuilder<File>, PredicateBuilder<File>, Interval>() {
            @Override
            public PredicateBuilder<File> evaluate(PredicateBuilder<File> builder, Interval range) {
                return builder.or(fileNameInDateRange(fileName, range));
            }
        };
    }

    private Iterable<File> allPossibleFilesInDirectory() {
        @SuppressWarnings("ConstantConditions")
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

            @Override
            public String toString() {
                return range.toString();
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

            @Override
            public String toString() {
                return "==" + fileName;
            }
        };
    }
}
