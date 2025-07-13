package controllers

import play.api.Logger
import javax.inject._
import play.api._
import play.api.mvc._
//import repositories.UserRepository
//import models.User
import utils.HtmlRendererUtil
import views.html.pages.user.userPage
import views.html.pages.post.postPage
import views.html.{index, main}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class PageController @Inject()(
        val controllerComponents: ControllerComponents
    ) extends BaseController {
    private val logger = Logger(this.getClass)
    /**
     * Create an Action to render an HTML page.
     *
     * The configuration in the `routes` file means that this method
     * will be called when the application receives a `GET` request with
     * a path of `/`.
     */
    def indexPage(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
        HtmlRendererUtil.withLayoutFallback("Home") {
            index()
        }
    }

    def postDetail(id: Long): Action[AnyContent] = Action {
        if (id < 1000) Ok(postPage())
        else NotFound("Post not found")
    }

    def getUserPage: Action[AnyContent] = Action { implicit request =>
        HtmlRendererUtil.withLayoutFallback("User") {
            userPage()
        }
    }

    def getPostPage: Action[AnyContent] = Action { implicit request =>
        HtmlRendererUtil.withLayoutFallback("User") {
            postPage()
        }
    }
}
