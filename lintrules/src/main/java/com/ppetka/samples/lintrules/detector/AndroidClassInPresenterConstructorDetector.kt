package com.ppetka.samples.lintrules.detector

import com.android.tools.lint.detector.api.*
import com.android.tools.lint.detector.api.Detector.UastScanner
import com.android.tools.lint.detector.api.Scope.JAVA_FILE
import org.jetbrains.uast.UClass

import java.util.EnumSet

/**
 * Created by Przemysław Petka on 1/21/2018.
 */

class AndroidClassInPresenterConstructorDetector : Detector(), UastScanner {
    companion object {
        private const val PRESENTER_CLS = "foo.bar.Presenter"
        private const val ANDROID_STARTING_PCKG_NAME = "android."

        val ISSUE: Issue = Issue.create("NoAndroidClassesAllowedInPresenterIssue",
                "Android dependencies not allowed in Presenter classes",
                "\"Network module should not be depended on android classes, and holds android logic, please satisfy presenter with necessary dependencies from outside\"",
                Category.CORRECTNESS,
                10,
                Severity.ERROR,
                Implementation(AndroidClassInPresenterConstructorDetector::class.java, EnumSet.of<Scope>(JAVA_FILE)))
    }

    /*IMPLEMENTATION*/
    override fun applicableSuperClasses(): List<String>? {
        return listOf(PRESENTER_CLS)
    }

    override fun visitClass(javaContext: JavaContext, uClass: UClass) {
        if (uClass.qualifiedName != PRESENTER_CLS) {
            val methods = uClass.methods
            methods
                    .forEach {
                        val uastParameters = it.uastParameters
                        for (uastParameter in uastParameters) {
                            val argType = uastParameter.type.canonicalText
                            if (argType.startsWith(ANDROID_STARTING_PCKG_NAME)) {
                                javaContext.report(ISSUE, uClass, javaContext.getLocation(uastParameter.psi), ISSUE.getBriefDescription(TextFormat.TEXT))
                            }
                        }
                    }
        }
    }
}