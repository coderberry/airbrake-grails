package airbrake

class AirbrakeTestController {

    def throwException() {
        session.myval = "Something in the Session!"
        throw new Exception("TestException ${System.currentTimeMillis()}")
    }
}
