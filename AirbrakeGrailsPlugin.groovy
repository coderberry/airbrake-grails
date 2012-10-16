import org.apache.log4j.Logger
import grails.util.Environment
import grails.plugins.airbrake.AirbrakeNotifier
import grails.plugins.airbrake.AirbrakeAppender
import grails.plugins.airbrake.UserDataSupplementer
import grails.plugins.airbrake.UserDataService

class AirbrakeGrailsPlugin {
    // the plugin version
    def version = "0.8"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0.0 > *"
    def pluginExcludes = [
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
    def scm = [ url: "https://github.com/cavneb/airbrake-grails" ]

    def appender

    def doWithSpring = {

        def options = application.config.grails.plugins.airbrake

        ConfigObject defaultOptions = new ConfigObject()
        defaultOptions.putAll(
            env: Environment.current.name,
            apiKey: null,
            filteredKeys: [],
            secure: false,
            enabled: true,
            host: null,
            port: null,
            supplementers: [],
            includeEventsWithoutExceptions: false)
        def mergedOptions = defaultOptions.merge(options)


        airbrakeNotifier(AirbrakeNotifier,
                mergedOptions.apiKey,
                mergedOptions.env,
                mergedOptions.filteredKeys,
                mergedOptions.host,
                mergedOptions.port,
                mergedOptions.secure,
                mergedOptions.supplementers,
                mergedOptions.enabled)

        airbrakeAppender(AirbrakeAppender, ref('airbrakeNotifier'), mergedOptions.includeEventsWithoutExceptions)
    }

    def doWithApplicationContext = { applicationContext ->
        def userDataService = application.config.grails.plugins.airbrake.userDataService
        if (userDataService) {
            applicationContext.airbrakeNotifier.addUserDataService(applicationContext.getBean(userDataService))
        }

        def appender = applicationContext.airbrakeAppender
        appender.activateOptions()
        Logger.rootLogger.addAppender(appender)
    }
}
