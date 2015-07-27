package grails.plugins.airbrake

import grails.plugins.Plugin
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

class AirbrakeGrailsPlugin extends Plugin {
    // the plugin version
    def version = '1.0.0'
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.0 > *"
    def pluginExcludes = [
            "grails-app/conf/**",
            "grails-app/views/**",
            "grails-app/controllers/**",
            "grails-app/jobs/**",
    ]

    def title = "Airbrake Plugin"
    def author = "Eric Berry"
    def authorEmail = "cavneb@gmail.com"
    def description = "Airbrake Client for Grails"
    def documentation = "https://github.com/cavneb/airbrake-grails"

    def license = "APACHE"
    def developers = [
        [ name: "Phuong LeCong", email: "phuong@reteltechnologies.com" ],
        [ name: "Jon Palmer", email: "jpalmer@care.com" ],
        [ name:  'Donal Murtagh']]

    def issueManagement = [ system: "GitHub", url: "https://github.com/cavneb/airbrake-grails/issues" ]
    def scm = [ url: AirbrakeNotifier.NOTIFIER_URL ]

    def doWithSpring = {

        def configuration = new Configuration(application.config.grails.plugins.airbrake.clone())

        airbrakeNotifier(AirbrakeNotifier, configuration) { bean ->
            bean.autowire = "byName"
        }

        airbrakeAppender(AirbrakeAppender, ref('airbrakeNotifier'), configuration.includeEventsWithoutExceptions)
    }

    def doWithApplicationContext = { applicationContext ->
        def appender = applicationContext.airbrakeAppender

        Logger rootLogger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)
        LoggerContext loggerContext = LoggerFactory.getILoggerFactory()
        appender.context = loggerContext
        appender.start()
        rootLogger.addAppender(appender)

    }
}
