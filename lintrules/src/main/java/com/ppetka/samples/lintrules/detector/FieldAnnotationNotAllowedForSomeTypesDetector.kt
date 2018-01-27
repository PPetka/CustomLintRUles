package com.ppetka.samples.lintrules.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.ppetka.samples.lintrules.Send

import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UField

import java.util.Arrays
import java.util.Collections
import java.util.EnumSet

import com.android.tools.lint.detector.api.Scope.JAVA_FILE

/**
 * Created by Przemysław Petka on 1/24/2018.
 */

class FieldAnnotationNotAllowedForSomeTypesDetector : Detector(), Detector.UastScanner {
    companion object {
        private val ANNOTATION_PCKG = "com.ppetka.samples.customlintrules.Send"
        private val ALLOWED_ANNOTATING_CLASS_PCKG = "java.lang.Float"

        private val WANTED_CLASS_PKCG_NAMES = Arrays.asList(
                "com.ppetka.samples.lintrules.detector.FieldAnnotationNotAllowedForSomeTypesDetector",
                "com.ppetka.samples.customlintrules.MainActivity")

        val ISSUE = Issue.create("SendAnnotationNotAllowedForGivenType",
                "**@Send** annotation not allowed for this typee",
                "Current class does not support multiple **@Send** annotations for sending each object individually, Please add this object to **Package** object",
                Category.CORRECTNESS,
                8,
                Severity.ERROR,
                Implementation(FieldAnnotationNotAllowedForSomeTypesDetector::class.java, EnumSet.of<Scope>(JAVA_FILE)))!!
    }

    /*IMPLEMENTATION*/
    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf<Class<out UElement>>(UClass::class.java)
    }

    override fun createUastHandler(javaContext: JavaContext?): UElementHandler {
        return object : UElementHandler() {
            override fun visitClass(uClass: UClass?) {
                if (WANTED_CLASS_PKCG_NAMES.contains(uClass!!.qualifiedName)) {
                    val fields = uClass.fields
                    for (field in fields) {
                        val annotations = field.annotations
                        annotations
                                .filter { ANNOTATION_PCKG == it.qualifiedName }
                                .forEach {
                                    if (fieldIsInvalidClass(field)) {
                                        javaContext?.report(ISSUE, uClass, javaContext.getLocation(it), ISSUE.getBriefDescription(TextFormat.TEXT))
                                    }
                                }
                    }
                }
            }
        }
    }

    private fun fieldIsInvalidClass(field: UField): Boolean {
        val fieldType = field.type
        if (fieldType is PsiClassType) {
            val fieldClass = fieldType.resolve()
                if (ALLOWED_ANNOTATING_CLASS_PCKG != fieldClass?.qualifiedName) {
                    return true
                }
        }
        return false
    }
}