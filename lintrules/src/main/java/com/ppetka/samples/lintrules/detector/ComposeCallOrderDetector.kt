package com.ppetka.samples.lintrules.detector

import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.UClass
import java.util.*

/**
 * Created by Przemysław Petka on 04-Feb-18.
 */
class ComposeCallOrderDetector : Detector(), Detector.UastScanner {
    companion object {
        private const val OBSERVABLE_CLS = "io.reactivex.Observable"

        val ISSUE = Issue.create("WrongComposeCallOrder",
                "WrongComposeCallOrder",
                "\"WrongComposeCallOrder\"",
                Category.CORRECTNESS,
                10,
                Severity.ERROR,
                Implementation(ComposeCallOrderDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))!!
    }

    /*IMPLEMENTATION*/
    override fun applicableSuperClasses(): List<String>? {
        return listOf(OBSERVABLE_CLS)
    }

    override fun visitClass(javaContext: JavaContext, uClass: UClass) {
     //   javaContext.report(ISSUE, uClass, javaContext.getLocation(uClass.psi), ISSUE.getBriefDescription(TextFormat.TEXT))
    }
}