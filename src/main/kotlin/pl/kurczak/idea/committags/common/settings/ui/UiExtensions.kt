package pl.kurczak.idea.committags.common.settings.ui

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.PropertyBinding
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent
import kotlin.reflect.KMutableProperty0

fun <T : JTextComponent> CellBuilder<T>.onChange(listener: (DocumentEvent?) -> Unit): CellBuilder<T> = this.apply {
    component.document.addDocumentListener(object : DocumentListener {
        override fun changedUpdate(e: DocumentEvent?) = listener(e)
        override fun insertUpdate(e: DocumentEvent?) = listener(e)
        override fun removeUpdate(e: DocumentEvent?) = listener(e)
    })
}

fun <T : JTextField> CellBuilder<T>.withBinding(prop: KMutableProperty0<String>): CellBuilder<T> {
    return withBinding(
        { component.text },
        { _, it -> component.text = it },
        PropertyBinding(prop.getter, prop.setter)
    )
}

fun CellBuilder<DialogPanel>.withPanelBindings(): CellBuilder<DialogPanel> {
    return onApply {
        component.apply()
    }.onReset {
        component.reset()
    }.onIsModified {
        component.isModified()
    }
}
