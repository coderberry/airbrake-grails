package grails.plugins.airbrake

import org.apache.log4j.spi.*;

class ThrowableSupplementer implements NoticeSupplementer {
	Notice supplement(Notice notice, LoggingEvent event) {
		def throwable = event.throwableInformation?.throwable

		if (throwable) {
			def e = new Error(
				clazz: throwable.class.name,
				message: throwable.message,
				backtrace: throwable.stackTrace
			)
			notice.error = e
		}

		notice
	}
}