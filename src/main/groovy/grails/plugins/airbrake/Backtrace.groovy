package grails.plugins.airbrake

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Backtrace {
    String className
    String methodName
    String fileName
    Integer lineNumber

    Backtrace() {
    }

    Backtrace(String className, String methodName, String fileName, int lineNumber) {
        this.className = className
        this.methodName = methodName
        this.fileName = fileName
        this.lineNumber = lineNumber
    }

    Backtrace(StackTraceElement stackTraceElement) {
        className = stackTraceElement.className
        methodName = stackTraceElement.methodName
        fileName = stackTraceElement.fileName
        lineNumber = stackTraceElement.lineNumber
    }

    Map toMap() {
        [
            className: className,
            methodName: methodName,
            fileName: fileName,
            lineNumber: lineNumber
        ]
    }
}
