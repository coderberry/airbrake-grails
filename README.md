# Airbrake Plugin for Grails

This is the notifier plugin for integrating grails apps with [Airbrake](http://airbrake.io).

When an uncaught exception occurs, Airbrake will POST the relevant data to the Airbrake server specified in your environment.

## Installation & Configuration

Add the following to your `BuildConfig.groovy`

```
compile ":airbrake:0.9.4"
```

Once the plugin is installed, you need to provide your Api Key in `Config.groovy` file:

```groovy
grails.plugins.airbrake.apiKey = 'YOUR_API_KEY'
```

## Usage

Once you have installed and configured the plugin there is nothing else to do. Uncaught exceptions will be logged by log4j and those errors will be reported to Airbrake. However, the plugin also exposes a few other ways to send errors to airbrake.

### Logging Errors with Exceptions

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
grails.plugins.airbrake.paramsFilteredKeys
grails.plugins.airbrake.sessionFilteredKeys
grails.plugins.airbrake.cgiDataFilteredKeys
grails.plugins.airbrake.host
grails.plugins.airbrake.port
grails.plugins.airbrake.secure
grails.plugins.airbrake.async
grails.plugins.airbrake.asyncThreadPoolSize
grails.plugins.airbrake.stackTraceFilterer
grails.plugins.airbrake.excludes
```

### Enabling/Disabling Notifications
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

#### Disabling Notifications by Exception Type
For example, to disable notifications caused by `IllegalArgumentException` and `IllegalStateException`, configure

````groovy
grails.plugins.airbrake.excludes = ['java.lang.IllegalArgumentException', 'java.lang.IllegalStateException']
````

each entry in the list will be converted to a `Pattern` and matched against the exception class name, so the following
would also exclude these two exceptions:

````groovy
grails.plugins.airbrake.excludes = ['java.lang.Illegal.*Exception']
````

#### Runtime Enabling/Disabling
If you wish to enable/disable notifications at runtime you have a couple of options

* Call the service method `airbrakeService.setEnabled(boolean enabled)`
* Invoke the `setEnabled` action of `AirbrakeTestController`. This action expects a single parameter either `enabled=true` or `enabled=false`


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

### Filtering Parameters sent to Airbrake
To prevent certain parameters from being sent to Airbrake you can configure a list of parameter names to filter out. The configuration settings `paramsFilteredKeys`, `sessionFilteredKeys` and `cgiFilteredKeys` filter the params, session and cgi data sent to Airbrake.
For example to prevent any web request parameter named 'password' from being included in the notification to Airbrake you would use the following configuration:
```groovy
grails.plugins.airbrake.paramsFilteredKeys = ['password']
```

Each configuration option also supports regular expression matching on the keys. For example the following configuration would prevent all session data from being sent to Airbrake:
```groovy
grails.plugins.airbrake.sessionFilteredKeys = ['.*']
```


### Custom Airbrake Host, Port and Scheme

If you are running the Airbrake server locally (or a clone thereof, e.g. Errbit), you will need to customise the server URL, port, scheme, etc.
For example to change the server host and port, add the following configuration parameters:

````groovy
grails.plugins.airbrake.host = 'errbit.example.org'
grails.plugins.airbrake.port = 8080
````

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

## Synchronous/Asynchronous notifications

By default, notifications are sent to Airbrake asynchronously using a thread-pool of size 5. To change the size of this thread
pool set the following config parameter:

```groovy
// double the size of the pool
grails.plugins.airbrake.asyncThreadPoolSize = 10
```

To have the notifications sent synchronously, set the async parameter to false:

```groovy
// send notifications synchronously
grails.plugins.airbrake.async = false
```

### Custom Asynchronous Handler
To send the notifications asynchronously using a custom handler, use the async configuration option.
This configuration takes a closure with two parameters the `Notice` to send and the `grailsApplication`. The asynchronous handler should simply call `airbrakeService.sendNotice(notice)` to deliver the notification.

This plugin does not introduce a default choice for processing notices asynchronously. You should choose a method that suits your application.
You could just create a new thread or use a scheduler/queuing plugin such as [Quartz](http://grails.org/plugin/quartz) or [Jesque](http://grails.org/plugin/jesque)

For example if you are using the Quartz plugin you can send notifications asynchronously using the following setting in `Config.groovy`

```groovy
grails.plugins.airbrake.async = { notice, grailsApplication ->
    AirbrakeNotifyJob.triggerNow(notice: notice)
}
```

and the `AirbrakeNotifyJob` is defined in `grails-app\jobs` something like this:

```groovy
class AirbrakeNotifyJob {
    def airbrakeService

    def execute(JobExecutionContext context) {
        Notice notice = context.mergedJobDataMap.notice
        if (notice) {
            airbrakeService.sendNotice(notice)
        }
    }
}
```

### Stack Trace Filtering
By default all stack traces are filtered using an instance of `org.codehaus.groovy.grails.exceptions.DefaultStackTraceFilterer` to remove common Grails and java packages.
To provide custom stack trace filtering simple configure an instance of a class that implements the interface `org.codehaus.groovy.grails.exceptions.StackTraceFilterer` in `Config.groovy`

```groovy
grails.plugins.airbrake.stackTraceFilterer = new MyCustomStackTraceFilterer()
```

## Compatibility

This plugin is compatible with Grails version 3.0 or greater.
A backport to Grails 1.3 is available on the [grails-1.3 branch] (https://github.com/cavneb/airbrake-grails/tree/grails-1.3).
A backport to Grails 2.2 is available on the [grails-2.2 branch] (https://github.com/cavneb/airbrake-grails/tree/grails-2.2).


## Release Notes

* 1.0.1 - 2016/04/11
    * Support for Grails 3.1.4.
* 1.0.0.RC1 - 2015/07/27
    * Support for Grails 3.0. #40
* 0.9.4 - 2013/06/25
    * Bug fix. AirbrakeNotifier.notify no longer throws under any circumstance.
* 0.9.3 - 2013/05/10
    * New feature to enable/disable notification send while the plugin is running #31
    * New configuration option to filter exceptions by name or pattern #34
    * Bug fix. Don't send notifications synchronously by default #26
    * Bug fix. Handle empty backtrace more gracefully for Errbit #33
    * Thanks to @domurtag for all the fixes
* 0.9.2 - 2013/1/19
    * Support for Grails 2.2 and Groovy 2.0 #27
* 0.9.1 - 2012/11/30
    * Notifications sent to Airbrake asynchronously by default using a thread pool of configurable size #20
    * By default stack traces are filtered #19
    * New configuration option to support custom stack trace filtering
    * New configuration options for filtering params, session and cgi data independently
* 0.9.0 - 2012/10/29
    * Support for sending notifications to Airbrake asynchronously
    * Added method to AirbrakeService set notification context information that will be used for any reported errors
    * Simpler api to provide User Data. No need to implement UserDataService instead just set the context. (Breaking Change)
    * All request headers now included when reporting an error.
* 0.8.1 - 2012/10/19
	* Better error message for uncaught exceptions. #18
* 0.8 - 2012/10/16
	* Simpler configuration (Breaking change)
	* Default notification environment to current Grails environment. #9
	* Support for notifying caught exceptions. #15
	* Support for notifying all messages logged at the error level (with or without exceptions)
	* Simpler api for providing user data
	* Full class names in stacktrace. #11
* 0.7.2 - 2012/09/24
	* Added User Agent, Hostname and user data to notifications
* 0.7.1 - 2012/09/21
	* Change supported Grails version to 2.0.0. #3
* 0.7 - 2012/09/21
	* Added support for filtering parameters, session and cgiData. #2

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

# Kudos

The origin version of this plugin was written by Phuong LeCong ([https://github.com/plecong/grails-airbrake](https://github.com/plecong/grails-airbrake)).
Since then it has undergone significant refactoring and updates inspired by the Ruby Airbrake gem ([https://github.com/airbrake/airbrake](https://github.com/airbrake/airbrake))
