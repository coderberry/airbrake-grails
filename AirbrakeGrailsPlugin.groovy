import org.apache.log4j.Logger
import grails.plugins.airbrake.AirbrakeNotifier
import grails.plugins.airbrake.AirbrakeAppender
import grails.plugins.airbrake.Configuration

class AirbrakeGrailsPlugin {
    // the plugin version
    def version = '0.8.1'
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0.0 > *"
    def pluginExcludes = [
            "grails-app/conf/**",
            "grails-app/views/**",
            "grails-app/controllers/**",
            "grails-app/services/test/**",
            "test/**",
            "web-app/**"
    ]

    def title = "Airbrake Plugin"
    def author = "Eric Berry"
    def authorEmail = "cavneb@gmail.com"
    def description = "Airbrake Client for Grails"
    def documentation = "http://grails.org/plugin/airbrake"

    def license = "APACHE"
    def developers = [ [ name: "Phuong LeCong", email: "phuong@reteltechnologies.com" ], [ name: "Jon Palmer", email: "jpalmer@care.com" ] ]

    def issueManagement = [ system: "GitHub", url: "https://github.com/cavneb/airbrake-grails/issues" ]
    def scm = [ url: AirbrakeNotifier.NOTIFIER_URL ]

    def doWithSpring = {

        def configuration = new Configuration(application.config.grails.plugins.airbrake.clone())

        airbrakeNotifier(AirbrakeNotifier, configuration)

        airbrakeAppender(AirbrakeAppender, ref('airbrakeNotifier'), configuration.includeEventsWithoutExceptions)
    }

    def doWithApplicationContext = { applicationContext ->
        def appender = applicationContext.airbrakeAppender
        appender.activateOptions()
        Logger.rootLogger.addAppender(appender)
    }
}
