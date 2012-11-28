package grails.plugins.airbrake

import grails.util.Environment
import org.codehaus.groovy.grails.exceptions.DefaultStackTraceFilterer
import org.codehaus.groovy.grails.exceptions.StackTraceFilterer

class Configuration {
    String notifierName = AirbrakeNotifier.NOTIFIER_NAME
    String notifierUrl = AirbrakeNotifier.NOTIFIER_URL
    String notifierVersion = AirbrakeNotifier.NOTIFIER_VERSION
    String env = Environment.current.name
    String apiKey
    List<String> filteredKeys = []
    boolean secure = false
    boolean enabled = true
    String host = AirbrakeNotifier.AIRBRAKE_HOST
    Integer port
    boolean includeEventsWithoutExceptions = false
    def asyncThreadPoolSize = 5
    Closure async
    StackTraceFilterer stackTraceFilterer

    Configuration(Map options = [:]) {
        // reimplement the map constructor so we can set the port afterwards
        options.each { k,v -> if (this.hasProperty(k)) { this."$k" = v} }
        port = port ?: (secure ? 443 : 80)
        if (!stackTraceFilterer) {
            stackTraceFilterer = new DefaultStackTraceFilterer()
        }
    }

    Map merge(Map options) {
        def merge = properties
        merge.remove 'async'
        merge.remove 'class'
        merge.remove 'metaClass'
        merge << options
        merge
    }

    String getScheme() {
        secure ? 'https' : 'http'
    }
}
