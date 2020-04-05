import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.ReactElement
import react.dom.div
import react.dom.h1

external interface TestProps : RProps


class Test : RComponent<TestProps, RState>() {
    override fun RBuilder.render() {
        div(classes = "main") {
            h1 { +"Hello Test" }
        }
    }
}

fun RBuilder.test(handler: TestProps.() -> Unit): ReactElement {
    return child(Test::class) {
        this.attrs(handler)
    }
}
