package grails.plugins.airbrake

import grails.util.Environment
import org.codehaus.groovy.grails.exceptions.DefaultStackTraceFilterer
import org.codehaus.groovy.grails.exceptions.StackTraceFilterer
import javax.annotation.PreDestroy
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import groovy.util.logging.Log4j

import java.util.regex.Pattern

@Log4j
class Configuration {

    def env = Environment.current.name
    String apiKey
    List<String> paramsFilteredKeys = []
    List<String> sessionFilteredKeys = []
    List<String> cgiDataFilteredKeys = []
    boolean secure = false
    boolean enabled = true
    String path = AirbrakeNotifier.AIRBRAKE_PATH
    String host = AirbrakeNotifier.AIRBRAKE_HOST
    Integer port
    boolean includeEventsWithoutExceptions = false
    Closure async
    StackTraceFilterer stackTraceFilterer
    List excludes = []

    private ExecutorService threadPool

    Configuration(Map options = [:]) {
        configureDefaultAsyncClosure(options)

        // reimplement the map constructor so we can set the port afterwards
        options.each { k,v -> if (this.hasProperty(k)) { this."$k" = v} }
        port = port ?: (secure ? 443 : 80)
        if (!stackTraceFilterer) {
            stackTraceFilterer = new DefaultStackTraceFilterer()
        }

        handleLegacyFilteredKeys(options)
    }

    void setExcludes(List exclusions) {

        this.excludes = exclusions.collect {excludePattern ->
            Pattern.compile(excludePattern.toString())
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

        def async = options.async

        if (!(async instanceof Closure)) {

            // if omitted async will be of type ConfigObject
            if (async instanceof ConfigObject || async as Boolean) {

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
                // only send notices synchronously if the async option is explicitly set to false
                options.async = null
            }
        }
    }

    // We used to have a single filteredKeys configuration parameter that was used for params, session and cgiData filtering
    // Here we provide backwards compatibilty for that old setting
    private handleLegacyFilteredKeys(Map options) {
        if (options.filteredKeys) {
            log.warn "Configuration option 'filteredKeys' is deprecated. Use 'paramsFilteredKeys', 'sessionFilteredKeys' or 'cgiDataFilteredKeys' instead."
            paramsFilteredKeys = options.filteredKeys
            sessionFilteredKeys = options.filteredKeys
            cgiDataFilteredKeys = options.filteredKeys
        }
    }
}
