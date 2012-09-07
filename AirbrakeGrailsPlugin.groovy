class AirbrakeGrailsPlugin {
    def version = "0.3"
    def grailsVersion = "2.1 > *"
    def pluginExcludes = ["grails-app/views/error.gsp"]

    def title = "Airbrake Plugin"
    def author = "Eric Berry"
    def authorEmail = "cavneb@gmail.com"
    def description = "Airbrake Client for Grails"
    def documentation = "http://grails.org/plugin/airbrake"

    def license = "APACHE"
    def developers = [ [ name: "Eric Berry", email: "cavneb@gmail.com" ],
                       [ name: "Phuong LeCong", email: "phuong@reteltechnologies.com" ]]

    def issueManagement = [ system: "GitHub", url: "https://github.com/cavneb/airbrake-grails/issues" ]
    def scm = [ url: "https://github.com/cavneb/airbrake-grails" ]
}
