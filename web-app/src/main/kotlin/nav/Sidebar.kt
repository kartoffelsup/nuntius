package nav

import home
import io.github.kartoffelsup.nuntius.client.NuntiusApiService
import kotlinx.coroutines.CoroutineScope
import login.loginForm
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

interface SidebarProps : RProps {
    var coroutineScope: CoroutineScope
    var nuntiusApi: NuntiusApiService
}

class Sidebar : RComponent<SidebarProps, RState>() {
    override fun RBuilder.render() {
        hashRouter {
            nav(classes = "sidebar") {
                ul(classes = "mdc-list") {
                    navLink(to = "/") {
                        li(classes = "mdc-list-item") {
                            span(classes = "mdc-list-item__graphic material-icons") {
                                +"home"
                            }
                            span(classes = "mdc-list-item__text") {
                                +"Home"
                            }
                        }
                    }
                    navLink(
                        to = "/login",
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
                route(path = "/login") {
                    loginForm(props.coroutineScope, props.nuntiusApi) {
                    }
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
    handler: SidebarProps.() -> Unit
): ReactElement {
    return child(Sidebar::class) {
        this.attrs {
            this.coroutineScope = coroutineScope
            this.nuntiusApi = nuntiusApiService
            handler()
        }
    }
}
