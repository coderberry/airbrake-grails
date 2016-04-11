package grails.plugins.airbrake

class TestController {

    AirbrakeService airbrakeService

    def testAirbrake() {

        try {
            throw new Exception("This is a test error")
        } catch (Exception e) {
            airbrakeService.notify(e, [errorMessage: e.getMessage()])
        }
        response.flushBuffer()
    }
}
