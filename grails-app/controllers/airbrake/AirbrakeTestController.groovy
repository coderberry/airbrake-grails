package airbrake

class AirbrakeTestController {

    def throwException = {
        session.myval = "Something in the Session!"
        session.password = "super_secret_password"
        throw new Exception("TestException ${System.currentTimeMillis()}")
    }
}
