package grails.plugins.airbrake

import spock.lang.*

class BacktraceSpec extends Specification {
    private final static String CLASS_NAME = 'com.acme.RabbitTraps'
    private final static String METHOD_NAME = 'catch'
    private final static String FILE_NAME = 'RabbitTraps.groovy'
    private final static int LINE_NUMBER = 10

    def 'map constructor'() {
        given:
        def backtrace = new Backtrace(className: CLASS_NAME, methodName: METHOD_NAME, fileName: FILE_NAME, lineNumber: LINE_NUMBER)

        expect:
        assertBacktrace(backtrace)
    }

    def 'args constructor'() {
        given:
        def backtrace = new Backtrace(CLASS_NAME, METHOD_NAME, FILE_NAME, LINE_NUMBER)

        expect:
        assertBacktrace(backtrace)
    }

    def 'StackTraceElement constructor'() {
        given:
        def backtrace = new Backtrace(new StackTraceElement(CLASS_NAME, METHOD_NAME, FILE_NAME, LINE_NUMBER))

        expect:
        assertBacktrace(backtrace)
    }

    def 'toMap is inverse of constructor'() {
        given:
        def args = [className: CLASS_NAME, methodName: METHOD_NAME, fileName: FILE_NAME, lineNumber: LINE_NUMBER]
        def backtrace = new Backtrace(args)

        expect:
        backtrace.toMap() == args
    }

    private void assertBacktrace(Backtrace backtrace) {
        assert backtrace.className == CLASS_NAME
        assert backtrace.methodName == METHOD_NAME
        assert backtrace.fileName == FILE_NAME
        assert backtrace.lineNumber == LINE_NUMBER
    }
}
