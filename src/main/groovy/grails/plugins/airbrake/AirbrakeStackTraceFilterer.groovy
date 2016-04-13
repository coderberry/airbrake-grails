package grails.plugins.airbrake;

import org.grails.exceptions.reporting.DefaultStackTraceFilterer;


/**
 * Created by marc on 11/04/2016.
 */
class AirbrakeStackTraceFilterer extends DefaultStackTraceFilterer{


    private static final String[] DEFAULT_INTERNAL_PACKAGES = [
        "org.grails.plugin.resource.DevMode",
        "org.grails.",
        "org.codehaus.groovy.grails.",
        "gant.",
        "org.codehaus.groovy.runtime.",
        "org.codehaus.groovy.reflection.",
        "org.codehaus.groovy.ast.",
        "org.codehaus.gant.",
        "groovy.",
        "org.mortbay.",
        "org.apache.catalina.",
        "org.apache.coyote.",
        "org.apache.tomcat.",
        "net.sf.cglib.proxy.",
        "sun.",
        "java.lang.reflect.",
        "org.springframework.",
        "org.springsource.loaded.",
        "com.opensymphony.",
        "org.hibernate.",
        "javax.servlet."
    ]

    private List<String> packagesToFilter = new ArrayList<String>()
    private boolean shouldFilter
    private String cutOffPackage = null


    @Override
    public Throwable filter(Throwable source) {
        if (shouldFilter) {
            StackTraceElement[] trace = source.getStackTrace();
            List<StackTraceElement> newTrace = filterTraceWithCutOff(trace, cutOffPackage)

            if (newTrace.isEmpty()) {
                // filter with no cut-off so at least there is some trace
                newTrace = filterTraceWithCutOff(trace, null)
            }

            // Only trim the trace if there was some application trace on the stack
            // if not we will just skip sanitizing and leave it as is
            if (!newTrace.isEmpty()) {
                // We don't want to lose anything, so log it
                //STACK_LOG.error(FULL_STACK_TRACE_MESSAGE, source);
                StackTraceElement[] clean = new StackTraceElement[newTrace.size()];
                newTrace.toArray(clean)
                source.setStackTrace(clean)
            }
        }
        return source;
    }

    private List<StackTraceElement> filterTraceWithCutOff(StackTraceElement[] trace, String endPackage) {
        List<StackTraceElement> newTrace = new ArrayList<StackTraceElement>()
        boolean foundGroovy = false
        for (StackTraceElement stackTraceElement : trace) {
            String className = stackTraceElement.getClassName()
            String fileName = stackTraceElement.getFileName()
            if (!foundGroovy && fileName != null && fileName.endsWith(".groovy")) {
                foundGroovy = true;
            }
            if (endPackage != null && className.startsWith(endPackage) && foundGroovy) break
            if (isApplicationClass(className)) {
                if (stackTraceElement.getLineNumber() > -1) {
                    newTrace.add(stackTraceElement)
                }
            }
        }
        return newTrace;
    }

}
