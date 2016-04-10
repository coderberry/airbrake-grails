package grails.plugins.airbrake

class TestController {

    AirbrakeService airbrakeService

    def testAirbrake() {

        airbrakeService.testAirbrake()
        response.flushBuffer()
    }
}
