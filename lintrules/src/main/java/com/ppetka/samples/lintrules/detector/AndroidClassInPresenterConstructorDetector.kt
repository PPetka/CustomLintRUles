package com.ppetka.samples.lintrules.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.android.tools.lint.detector.api.Detector.UastScanner

import com.android.tools.lint.detector.api.Scope.JAVA_FILE

import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UParameter

import java.util.Collections
import java.util.EnumSet

/**
 * Created by Przemysław Petka on 1/21/2018.
 */

class AndroidClassInPresenterConstructorDetector : Detector(), UastScanner {

    /*IMPLEMENTATION*/

    companion object {
        private const val PRESENTER_PACKAGE_NAME = "com.ppetka.samples.customlintrules.Presenter"
        private const val ANDROID_STARTING_PACKAGE_NAME = "android."

        val ISSUE = Issue.create("NoAndroidClassesAllowedInPresenterIssue",
                "Android dependencies not allowed in Presenter classes",
                "\"Network module should not be depended on android classes, and holds android logic, please satisfy presenter with necessary dependencies from outside\"",
                Category.CORRECTNESS,
                10,
                Severity.ERROR,
                Implementation(AndroidClassInPresenterConstructorDetector::class.java, EnumSet.of<Scope>(JAVA_FILE)))!!
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf<Class<out UElement>>(UClass::class.java)
    }

    override fun createUastHandler(javaContext: JavaContext?): UElementHandler {

        return object : UElementHandler() {
            override fun visitClass(uClass: UClass?) {
                //visit only Presenter classes
                if (PRESENTER_PACKAGE_NAME == uClass!!.qualifiedName) {
                    val methods = uClass.methods
                    //check only for constructors
                    methods
                            .filter { it.isConstructor }
                            .forEach {
                                val uastParameters = it.uastParameters
                                for (uastParameter in uastParameters) {
                                    val argType = uastParameter.type.canonicalText
                                    if (argType.startsWith(ANDROID_STARTING_PACKAGE_NAME)) {
                                        javaContext?.report(ISSUE, uClass, javaContext.getLocation(uastParameter.psi), ISSUE.getBriefDescription(TextFormat.TEXT))
                                    }
                                }
                            }
                }
            }
        }
    }
}