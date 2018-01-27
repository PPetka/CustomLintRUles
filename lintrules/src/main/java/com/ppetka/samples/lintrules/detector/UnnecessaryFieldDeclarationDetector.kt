package com.ppetka.samples.lintrules.detector

import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiClassType
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import java.util.*

/**
 * Created by Przemysław Petka on 1/27/2018.
 */

class UnnecessaryFieldDeclarationDetector : Detector(), Detector.UastScanner {

    companion object {
        private const val SUPER_CLASS = "com.ppetka.samples.lintrules.test.SuperAlleluja"
        private const val FIELD_CLASS = "com.ppetka.samples.lintrules.test.FieldClass"

        val ISSUE = Issue.create("FieldRedeclaration",
                "This field is re-declared",
                "Unnecessary field declaration, please remove this field as it's already declared in base class",
                Category.CORRECTNESS,
                3,
                Severity.ERROR,
                Implementation(UnnecessaryFieldDeclarationDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))!!
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf<Class<out UElement>>(UClass::class.java)
    }

    override fun createUastHandler(javaContext: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitClass(uClass: UClass) {
                val evaluator: JavaEvaluator = javaContext.evaluator
                if (evaluator.extendsClass(uClass.psi, SUPER_CLASS, true)) {
                    val fields = uClass.fields
                    fields.forEach {
                        val type = it.type
                        if (type is PsiClassType) {
                            val resolvedClass = type.resolve()
                            if (FIELD_CLASS == resolvedClass?.qualifiedName) {
                                javaContext.report(UnnecessaryFieldDeclarationDetector.ISSUE, uClass, javaContext.getLocation(it), UnnecessaryFieldDeclarationDetector.ISSUE.getBriefDescription(TextFormat.TEXT))
                            }
                        }
                    }
                }
            }
        }
    }
}
