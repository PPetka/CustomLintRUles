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
        private const val SUPER_CLASS = "foo.pckg.SuperCls"
        private const val FIELD_CLASS = "bar.pckgg.FieldCls"

        val ISSUE = Issue.create("FieldRedeclaration",
                "This field is re-declared",
                "Unnecessary field declaration, please remove this field as it's already declared in base class",
                Category.CORRECTNESS,
                3,
                Severity.ERROR,
                Implementation(UnnecessaryFieldDeclarationDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))!!
    }

    override fun applicableSuperClasses(): List<String>? {
        return listOf(SUPER_CLASS)
    }

    override fun visitClass(javaContext: JavaContext, uClass: UClass) {
        if (uClass.qualifiedName != SUPER_CLASS) {
            val fields = uClass.fields
            fields.forEach {
                val type = it.type
                if (type is PsiClassType) {
                    val resolvedClass = type.resolve()
                    if (FIELD_CLASS == resolvedClass?.qualifiedName) {
                        javaContext.report(ISSUE, uClass, javaContext.getLocation(it), ISSUE.getBriefDescription(TextFormat.TEXT))
                    }
                }
            }
        }
    }
}
