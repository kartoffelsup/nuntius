package view.nav

import csstype.ClassName
import io.github.kartoffelsup.nuntius.client.NuntiusApiService
import kotlinx.coroutines.CoroutineScope
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.nav
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.ul
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter
import react.router.dom.NavLink
import service.user.UserService
import view.Home
import view.login.LoginForm
import view.register.RegisterForm

external interface SidebarProps : Props {
    var coroutineScope: CoroutineScope
    var nuntiusApi: NuntiusApiService
    var userService: UserService
}

val Sidebar = FC<SidebarProps> { props ->
    HashRouter {
        nav {
            className = ClassName("sidebar")
            ul {
                className = ClassName("mdc-list")
                NavLink {
                    to = "/"
                    li {
                        className = ClassName("mdc-list-item")
                        span {
                            className = ClassName("mdc-list-item__graphic material-icons")
                            +"home"
                        }
                        span {
                            className = ClassName("mdc-list-item__text")
                            +"Home"
                        }
                    }
                }
                NavLink {
                    to = "/view/login"
                    className = ClassName("mdc-ripple-upgraded mdc-ripple-upgraded--background-focused")
                    li {
                        className = ClassName("mdc-list-item")
                        span {
                            className = ClassName("mdc-list-item__graphic material-icons")
                            +"person"
                        }
                        span {
                            className = ClassName("\"mdc-list-item__text\"")
                            +"Login"
                        }
                    }
                }
                NavLink {
                    to = "/view/signup"
                    className = ClassName("mdc-ripple-upgraded mdc-ripple-upgraded--background-focused")
                    li {
                        className = ClassName("mdc-list-item")
                        span {
                            className = ClassName("mdc-list-item__graphic material-icons")
                            +"assignment"
                        }
                        span {
                            className = ClassName("\"mdc-list-item__text\"")
                            +"Sign Up"
                        }
                    }
                }
            }
        }
        Routes {
            Route {
                path = "/view/login"
                element = LoginForm.create() {
                    coroutineScope = props.coroutineScope
                    userService = props.userService
                }
            }
            Route {
                path = "/view/signup"
                element = RegisterForm.create() {
                    coroutineScope = props.coroutineScope
                    userService = props.userService
                }
            }
            Route {
                path = "/"
                element = Home.create() {

                }
            }
        }
    }
}
