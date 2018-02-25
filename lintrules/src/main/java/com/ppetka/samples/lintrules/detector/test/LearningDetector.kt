package com.ppetka.samples.lintrules.detector.test

import com.android.tools.lint.client.api.JavaEvaluator
import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import com.ppetka.samples.lintrules.detector.ComposeCallOrderDetector
import org.jetbrains.uast.*
import org.jetbrains.uast.util.isMethodCall
import java.util.*

//todo check those methods
//  context.evaluator.parameterHasType()
//


/**
 * Created by Przemysław Petka on 17-Feb-18.
 */
class LearningDetector : Detector(), Detector.UastScanner {
    companion object {

        val SOME_ISSUE = Issue.create("SOME_ISSUE",
                "SOME_ISSUE",
                "\"SOME_ISSUE\"",
                Category.SECURITY,
                10,
                Severity.ERROR,
                Implementation(LearningDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(UCallExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return UElementHandlerImpl(context)
    }

    inner class UElementHandlerImpl(private val javaContext: JavaContext) : UElementHandler() {
        var ctr = 0
        override fun visitCallExpression(uCallExpression: UCallExpression) {
            //println(" ${uCallExpression.methodName}, declaration:  ${uCallExpression.getContainingDeclaration().toString()}")

            /*   if (ctr == 0) {
                   println(uCallExpression.getContainingUClass()?.asRecursiveLogString())
               }
               ctr++*/
            println(" uCallExpression, ${uCallExpression.asRecursiveLogString()}")
       /*     val qua: UQualifiedReferenceExpression? = uCallExpression.getParentOfType(UQualifiedReferenceExpression::class.java, true)

            qua?.let {
                println("qua logString, ${qua.asLogString()}")
                val uMethod = qua.tryResolve() as? UMethod
                uMethod?.let { println("resolved UMETHOD!!!!!!!!, mthdName: ${uMethod.name}") }
            }*/

            if (uCallExpression.isMethodCall()) {
                val resolvedCall = uCallExpression.resolveToUElement()
                if (resolvedCall is UMethod) {
                    println("resolved uMethod, ${resolvedCall.name}")
                    val evaluator: JavaEvaluator = javaContext.evaluator
                    if (evaluator.isMemberInClass(resolvedCall, "com.ppetka.samples.customlintrules.SecondCls")) {
                        println("   is member of SecondCls")
                        if (resolvedCall.uastParameters.isNotEmpty()) {
                            if (evaluator.parameterHasType(resolvedCall, 0, "java.lang.Integer")) {
                                println("   isInteger")
                            }
                        }
                    }
                }
            }
            println()
        }
    }
}