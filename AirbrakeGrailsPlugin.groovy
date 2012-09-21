class AirbrakeGrailsPlugin {
    // the plugin version
    def version = "0.7"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.1 > *"
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "test/**"
    ]

    def title = "Airbrake Plugin"
    def author = "Eric Berry"
    def authorEmail = "cavneb@gmail.com"
    def description = "Airbrake Client for Grails"
    def documentation = "http://grails.org/plugin/airbrake"

    def license = "APACHE"
    def developers = [ [ name: "Phuong LeCong", email: "phuong@reteltechnologies.com" ]]

    def issueManagement = [ system: "GitHub", url: "https://github.com/cavneb/airbrake-grails/issues" ]
    def scm = [ url: "https://github.com/cavneb/airbrake-grails" ]
}
