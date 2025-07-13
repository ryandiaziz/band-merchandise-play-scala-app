package utils

import play.api.mvc._
import play.twirl.api.Html
import scala.util.Try
import play.api.mvc.Results._

object HtmlRendererUtil {
    def withLayoutFallback(title: String)(content: => Html)(implicit request: Request[_]): Result = {
        val isAjax = request.headers.get("X-Requested-With").contains("XMLHttpRequest")

        Try {
            if (isAjax) {
                Ok(content) // partial
            } else {
                Ok(views.html.main(title)(content)) // layout
            }
        }.recover {
            case ex =>
                // Log error, fallback ke pesan aman
                val fallbackHtml = Html("<p>Terjadi kesalahan saat menampilkan halaman.</p>")
                if (isAjax) Ok(fallbackHtml)
                else Ok(views.html.main("Error")(fallbackHtml))
        }.get
    }
}
