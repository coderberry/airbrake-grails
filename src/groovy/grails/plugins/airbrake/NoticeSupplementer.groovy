package grails.plugins.airbrake

import org.apache.log4j.spi.*;

interface NoticeSupplementer {
	Notice supplement(Notice notice, LoggingEvent event)
}