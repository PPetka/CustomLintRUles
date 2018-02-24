package com.ppetka.samples.lintrules.detector

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiClassType

import org.jetbrains.uast.UClass
import org.jetbrains.uast.UField

import java.util.EnumSet

import com.android.tools.lint.detector.api.Scope.JAVA_FILE

/**
 * Created by Przemysław Petka on 1/24/2018.
 */

class FieldAnnotationNotAllowedForSomeTypesDetector : Detector(), Detector.UastScanner {
    companion object {
        private const val SUPER_CLASS = "foo.pckg.SuperCls"
        private const val ANNOTATION_PCKG = "foo.anno.FooAnnotation"
        private const val ALLOWED_ANNOTATING_CLASS_PCKG = "allowed.ann.pckg.AACLS"

        val ISSUE: Issue = Issue.create("FooAnnotationNotAllowedForGivenType",
                "**FooAnnotation** annotation not allowed for this type",
                "Current class does not support multiple **FooAnnotation** annotations for sending each object individually",
                Category.CORRECTNESS,
                8,
                Severity.ERROR,
                Implementation(FieldAnnotationNotAllowedForSomeTypesDetector::class.java, EnumSet.of<Scope>(JAVA_FILE)))
    }

    /*IMPLEMENTATION*/
    override fun applicableSuperClasses(): List<String>? {
        return listOf(SUPER_CLASS)
    }

    override fun visitClass(javaContext: JavaContext, uClass: UClass) {
        if (uClass.qualifiedName != SUPER_CLASS) {
            val fields = uClass.fields
            for (field in fields) {
                val annotations = field.annotations
                annotations
                        .filter { ANNOTATION_PCKG == it.qualifiedName }
                        .forEach {
                            if (!fieldIsValidClass(field)) {
                                javaContext.report(ISSUE, uClass, javaContext.getLocation(it), ISSUE.getBriefDescription(TextFormat.TEXT))
                            }
                        }
            }
        }
    }

    private fun fieldIsValidClass(field: UField): Boolean {
        val fieldType = field.type
        if (fieldType is PsiClassType) {
            val fieldClass = fieldType.resolve()
            if (ALLOWED_ANNOTATING_CLASS_PCKG == fieldClass?.qualifiedName) {
                return true
            }
        }
        return false
    }
}