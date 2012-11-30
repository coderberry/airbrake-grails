package grails.plugins.airbrake

import grails.util.Environment
import org.codehaus.groovy.grails.exceptions.DefaultStackTraceFilterer
import org.codehaus.groovy.grails.exceptions.StackTraceFilterer
import javax.annotation.PreDestroy
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import groovy.util.logging.Log4j

@Log4j
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
    Closure async
    StackTraceFilterer stackTraceFilterer

    private ExecutorService threadPool

    Configuration(Map options = [:]) {
        configureDefaultAsyncClosure(options)

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

    @PreDestroy
    void shutdownThreadPool() {
        threadPool?.shutdown()
    }

    private configureDefaultAsyncClosure(Map options) {
        def async = (options.async != null) ? options.async : true
        if (!(async instanceof Closure)) {
            if (async == true ) {
                log.info "configureDefaultAsyncClosure create default async handler with threadPool"
                def threadPoolSize = options.asyncThreadPoolSize ?: 5
                threadPool = Executors.newFixedThreadPool(threadPoolSize)
                options.async = { notice, grailsApplication ->
                    log.debug "submitting notice to threadPool"
                    Runnable sendToAirbrake = {
                        grailsApplication.mainContext.airbrakeNotifier.sendToAirbrake(notice)
                    } as Runnable
                    threadPool.submit(sendToAirbrake)
                }
            } else {
                options.async = null
            }
        }
    }

}
