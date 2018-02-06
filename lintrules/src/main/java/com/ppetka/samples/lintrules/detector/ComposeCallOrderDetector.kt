package com.ppetka.samples.lintrules.detector

import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.*
import org.jetbrains.uast.util.isMethodCall
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Przemysław Petka on 04-Feb-18.
 */
class ComposeCallOrderDetector : Detector(), Detector.UastScanner {
    companion object {
        private const val SOME_CLS = "com.ppetka.samples.customlintrules.S"

        val ISSUE = Issue.create("WrongComposeCallOrder",
                "WrongComposeCallOrder",
                "\"WrongComposeCallOrder\"",
                Category.CORRECTNESS,
                10,
                Severity.ERROR,
                Implementation(ComposeCallOrderDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))!!
    }


    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(UMethod::class.java, UCallExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return Asd()
    }

    inner class Asd : UElementHandler() {
        var outermostQuaExpressionList: MutableList<UQualifiedReferenceExpression> = ArrayList()

        override fun visitMethod(uMethod: UMethod) {
            //println("M " + uMethod.name)
        }

        override fun visitCallExpression(uCallExpression: UCallExpression) {
            val quaReferenceExpr: UQualifiedReferenceExpression? = uCallExpression.getOutermostQualified()
            if (quaReferenceExpr != null && !outermostQuaExpressionList.contains(quaReferenceExpr)) {
                outermostQuaExpressionList.add(quaReferenceExpr)

                val qualifiedChain = quaReferenceExpr.getQualifiedChain()
                if (qualifiedChain.toString().contains("compose")) {
                    qualifiedChain.forEach {
                        when (it) {
                            is UCallExpression -> {
                                println("Call Expr: " + it.methodName)
                            }
                            else -> {
                                println("Other Type: " + it.toString())
                            }
                        }
                    }
                }
                println("\n")

                //   println("call: " + uCallExpression.methodName + ", o: " + quaReferenceExpr.toString())
            }
        }
    }

}