package utils

import play.api.Logger

object LoggerUtil {
    def getLogger(name: String): AppLogger = new AppLogger(name)

    class AppLogger private[LoggerUtil](name: String) {
        private val logger = Logger(name)

        def info(msg: String): Unit = logger.info(s"[$name] $msg")

        def debug(msg: String): Unit = logger.debug(s"[$name] $msg")

        def warn(msg: String): Unit = logger.warn(s"[$name] $msg")

        def error(msg: String): Unit = logger.error(s"[$name] $msg")

        def error(msg: String, ex: Throwable): Unit =
            logger.error(s"[$name] $msg", ex)
    }
}