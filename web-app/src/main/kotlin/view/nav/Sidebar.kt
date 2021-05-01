package view.nav

import io.github.kartoffelsup.nuntius.client.NuntiusApiService
import kotlinx.coroutines.CoroutineScope
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.ReactElement
import react.dom.li
import react.dom.nav
import react.dom.span
import react.dom.ul
import react.router.dom.hashRouter
import react.router.dom.navLink
import react.router.dom.route
import react.router.dom.switch
import service.user.UserService
import view.home
import view.login.loginForm

interface SidebarProps : RProps {
    var coroutineScope: CoroutineScope
    var nuntiusApi: NuntiusApiService
    var userService: UserService
}

class Sidebar : RComponent<SidebarProps, RState>() {

    override fun RBuilder.render() {
        hashRouter {
            nav(classes = "sidebar") {
                ul(classes = "mdc-list") {
                    navLink<RProps>(to = "/") {
                        li(classes = "mdc-list-item") {
                            span(classes = "mdc-list-item__graphic material-icons") {
                                +"home"
                            }
                            span(classes = "mdc-list-item__text") {
                                +"Home"
                            }
                        }
                    }
                    navLink<RProps>(
                        to = "/view/login",
                        activeClassName = "mdc-ripple-upgraded mdc-ripple-upgraded--background-focused"
                    ) {
                        li(classes = "mdc-list-item") {
                            span(classes = "mdc-list-item__graphic material-icons") {
                                +"person"
                            }
                            span(classes = "mdc-list-item__text") {
                                +"Login"
                            }
                        }
                    }
                }
            }
            switch {
                route(path = "/view/login") {
                    loginForm(props.coroutineScope, props.userService) {}
                }
                route(path = "/", exact = true) {
                    home {}
                }
            }
        }
    }
}

fun RBuilder.sidebar(
    coroutineScope: CoroutineScope,
    nuntiusApiService: NuntiusApiService,
    userService: UserService,
    handler: SidebarProps.() -> Unit
): ReactElement {
    return child(Sidebar::class) {
        this.attrs {
            this.coroutineScope = coroutineScope
            this.nuntiusApi = nuntiusApiService
            this.userService = userService
            handler()
        }
    }
}
