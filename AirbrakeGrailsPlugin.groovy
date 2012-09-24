class AirbrakeGrailsPlugin {
    // the plugin version
    def version = "0.7.2"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0.0 > *"
    def pluginExcludes = [
            "grails-app/views/**",
            "grails-app/controllers/**",
            "src/groovy/grails/plugins/airbrake/test/**",
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
}
