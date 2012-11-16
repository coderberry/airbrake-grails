# Airbrake Plugin for Grails

This is the notifier plugin for integrating grails apps with [Airbrake](http://airbrake.io).

When an uncaught exception occurs, Airbrake will POST the relevant data to the Airbrake server specified in your environment.

## Installation & Configuration

Add the following to your `BuildConfig.groovy`

```
compile ":airbrake:0.9.0"
```

Once the plugin is installed, you need to provide your Api Key in `Config.groovy` file:

```groovy
grails.plugins.airbrake.apiKey = 'YOUR_API_KEY'
```

## Usage

Once you have installed and configured the plugin there is nothing else to do. Uncaught exceptions will be logged by log4j and those errors will be reported to Airbrake. However, the plugin also exposes a few other ways to send errors to airbrake.

### Logging Erorrs with Exceptions

Manually logging messages at the error level and including an Exception triggers an error notification to airbrake:

```groovy
class SomeController
	def someAction() {
		try {
			somethingThatThrowsAnException()
		} catch(e) {
			log.error('An error occured', e)
		}
	}
```

(See the section below on configuring the plugin to including errors without exceptions.)

### AirbakeService

The plugin also exposes an `airbrakeService` which can be dependency injected into your Grails classes. The service allows you to send error notifications directly to Airbrake without logging anything to log4j. It has a `notify` method that takes a `Throwable` parameter and an optional Map of arguemnts. The next example shows both in use:

```groovy
class SomeController
	def airbrakeService
	
	def someAction() {
		try {
			somethingThatThrowsAnException()
		} catch(e) {
			airbrakeService.notify(e, [errorMessage: 'An error occurred'])
		}
	}

	def anotherAction() {
		if (somethingWentWrong()) {
			airbrakeService.notify(null, [errorMessage: 'Something went wrong'])
		}
	}
```

## Advanced Configuration
The Api Key is the minimum requirement to report errors to Airbrake. However, the plugin supports several other configuration options. The full list of configuration options is:

```groovy
grails.plugins.airbrake.apiKey
grails.plugins.airbrake.enabled
grails.plugins.airbrake.env
grails.plugins.airbrake.includeEventsWithoutExceptions
grails.plugins.airbrake.filteredKeys
grails.plugins.airbrake.host
grails.plugins.airbrake.port
grails.plugins.airbrake.secure
grails.plugins.airbrake.async
grails.plugins.airbrake.threads
```

### Enabling/Disabling notifications
By default all errors are sent to Airbrake. However, you can disable error notifications (essentially disabling the plugin) by setting `grails.plugins.airbrake.enabled = false`. For example to disable error notificaitons in development and test environments you might have the following in `Config.groovy`:

```groovy
grails.plugins.airbrake.apiKey = 'YOUR_API_KEY'
environments {
	development {
		grails.plugins.airbrake.enabled = false
	}
	test {
		grails.plugins.airbrake.enabled = false
	}
```

### Setting the Environment
By default, the environment name used in Airbrake will match that of the current Grails environment. To change this default, set the env property:

```groovy
grails.plugins.airbrake.env = grails.util.Environment.current.name[0..0] // Airbrake env name changed from default value of Development/Test/Production to D/T/P
```

### Including all events logged at the Error level

By default only uncaught errors or errors logged with an exception are reported to Airbrake. It is often convenient to loosen that restriction so that all messages logged at the `Error` level are reported to Airbrake. This often most useful in `src/java` or `src/groovy` classes that can more easily have a log4j logger than get accees to the dependency injected `airbrakeService`. 

With the following line in `Config.groovy`:

```groovy
grails.plugins.airbrake.includeEventsWithoutExceptions = true
```

then logged errors get reported to Airbrake:

```groovy
@Log4j
class SomeGroovyClass {
	def doSomething() {
		if (somethingWentWrong()) {
			log.error('Something went wrong')
		}
	}
}
```

Note: It might be reasonable to have this setting true by default but for backwards compatibility with previous versions of te plugin the default is false.

### Filtering Parameters

### Custom Airbrake Host, Port and Scheme

### Adding Custom Data to Error Notifications

### Supplying User Data

Airbrake allows certain User data to be supplied with the notice. To set the current users data to be included in all notifications use the `airbrakeService.addNoticeContext` method to set a map of userData.
The supported keys are `id`, `name`, `email` and `username`
```groovy
airbrakeService.addNoticeContext(id: '1234', name: 'Bugs Bunny', email: 'bugs@acme.com', username: 'bugs')
```

In most web apps the simplest way to provide this context is in a Grails filter. For example if you are using `SpringSecurity` add the following `AirbrakeFilter.groovy` in `grails-app/conf`
```groovy
class AirbrakeFilters {
    def airbrakeService
    def springSecurityService

    def filters = {
        all(uri: '/**') {
            before = {
                def user = springSecurityService.currentUser
                if (user) {
                   airbrakeService.addNoticeContext(user: [id: user.id, name: user.name, email: user.email, username: user.username ])
                }
            }
        }
    }
}
```

## Asynchronous Notifications
Notifications are sent to Airbrake Asynchronously using a fixed-size thread pool. By default this pool has a maximum
size of 5, but this can be changed with the config parameter

````groovy
grails.plugins.airbrake.threads = 2
````

## Compatibility

This plugin is compatible with Grails version 2.0 or greater. A backport to Grails 1.3 is available on the [grails-1.3 branch] (https://github.com/cavneb/airbrake-grails/tree/grails-1.3).

## Release Notes

* 0.7 - 2012/09/21
	* Added support for filtering parameters, session and cgiData. #2
* 0.7.1 - 2012/09/21
	* Change supported Grails version to 2.0.0. #3
* 0.7.2 - 2012/09/24
	* Added User Agent, Hostname and user data to notifications
* 0.8 - 2012/10/16
	* Simpler configuration (Breaking change)
	* Default notification environment to current Grails environment. #9
	* Support for notifying caught exceptions. #15
	* Support for notifying all messages logged at the error level (with or without exceptions)
	* Simpler api for providing user data
	* Full class names in stacktrace. #11
* 0.8.1 - 2012/10/19
	* Better error message for uncaught exceptions. #18
* 0.9.0 - 2012/10/29
    * Support for sending notifications to Airbrake asynchronously
    * Added method to AirbrakeService set notification context information that will be used for any reported errors
    * Simpler api to provide User Data. No need to implement UserDataService instead just set the context. (Breaking Change)
    * All request headers now included when reporting an error.
* 0.9.5 - 2012/11/16
    * Notifications sent to Airbrake asynchronously by default using a thread pool of configurable size #20
    * Stack traces are filtered #19
    * Upgraded to Grails 2.1.1
    * XML message is logged at debug level

## Contributing


1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

# Kudos

The origin version of this plugin was written by Phuong LeCong ([https://github.com/plecong/grails-airbrake](https://github.com/plecong/grails-airbrake)).
Since then it has undergone significant refactoring and updates inspired by the Ruby Airbrake gem ([https://github.com/airbrake/airbrake](https://github.com/airbrake/airbrake))
