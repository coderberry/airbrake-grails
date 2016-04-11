package grails.plugins.airbrake

import ch.qos.logback.classic.Logger
import grails.plugins.Plugin
import org.slf4j.LoggerFactory

class AirbrakeGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def version = '1.0.1'
    def grailsVersion = "3.1.4 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/conf/**",
            "grails-app/views/**",
            "grails-app/controllers/**",
            "grails-app/jobs/**",
            "test/**",
            ""
    ]

    // TODO Fill in these fields
    def title = "Airbreak" // Headline display name of the plugin
    def author = "Eric Berry"
    def authorEmail = "cavneb@gmail.com"
    def description = "Airbrake Client for Grails 3.1.4 > *"
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "https://github.com/cavneb/airbrake-grails"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
    def developers = [[name: "Phuong LeCong", email: "phuong@reteltechnologies.com"], [name: "Jon Palmer", email: "jpalmer@care.com"], [name: 'Donal Murtagh'], [name: "Marc-Emmanuel Ramage", email: "marc@riflmedia.com"]]

    // Location of the plugin's issue tracker.
    def issueManagement = [system: "GitHub", url: "https://github.com/cavneb/airbrake-grails/issues"]
    def scm = [url: AirbrakeNotifier.NOTIFIER_URL]
    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    Closure doWithSpring() {
        { ->
            def configuration = new Configuration(application.config.grails.plugins.airbrake.clone())

            airbrakeNotifier(AirbrakeNotifier, configuration) { bean ->
                bean.autowire = "byName"
            }

            airbrakeAppender(AirbrakeAppender, ref('airbrakeNotifier'), configuration.includeEventsWithoutExceptions)
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        def appender = applicationContext.airbrakeAppender
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        appender.start()
        logger.addAppender(appender)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
