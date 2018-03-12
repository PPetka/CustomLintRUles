package com.ppetka.samples.lintrules.detector.bugs

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*
import org.jetbrains.uast.util.isMethodCall
import java.util.*

/**
 * Created by Przemysław Petka on 17-Feb-18.
 */
class UElementHandlerQuaChainBugDetector : Detector(), Detector.UastScanner {
    companion object {
        const val firstFuncStr = "firstFunc"
        const val secondFuncStr = "secondFunc"
        const val thirdFuncStr = "thirdFunc"
        const val overloadedFuncStr = "overloadedFunc"

        val SOME_ISSUE = Issue.create("SOME_ISSUE",
                "SOME_ISSUE",
                "\"SOME_ISSUE\"",
                Category.SECURITY,
                10,
                Severity.ERROR,
                Implementation(UElementHandlerQuaChainBugDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))
    }

    //UElementHandler
    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(UCallExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return UElementHandlerImpl(context)
    }

    inner class UElementHandlerImpl(private val context: JavaContext) : UElementHandler() {
        override fun visitCallExpression(uCallExpression: UCallExpression) {
            report(context, uCallExpression)
        }
    }

    //helper
    private fun report(context: JavaContext, node: UCallExpression) {
        if (node.methodName == firstFuncStr) {
            if (node.isMethodCall()) {
                val resolvedCall = node.resolveToUElement()
                if (resolvedCall is UMethod) {
                    context.report(SOME_ISSUE, node, context.getLocation(node), firstFuncStr)
                }
                println("${node.methodName} is MethodCall, resolvedCall, $resolvedCall")
            } else {
                println("${node.methodName} seems not to be methodCall")
            }
        }

        if (node.methodName == secondFuncStr) {
            if (node.isMethodCall()) {
                val resolvedCall = node.resolveToUElement()
                if (resolvedCall is UMethod) {
                    context.report(SOME_ISSUE, node, context.getLocation(node), firstFuncStr)
                }
                println("${node.methodName} is MethodCall, resolvedCall, $resolvedCall")
            } else {
                println("${node.methodName} seems not to be methodCall")
            }
        }

        if (node.methodName == thirdFuncStr) {
            if (node.isMethodCall()) {
                val resolvedCall = node.resolveToUElement()
                if (resolvedCall is UMethod) {
                    context.report(SOME_ISSUE, node, context.getLocation(node), firstFuncStr)
                }
                println("${node.methodName} is MethodCall, resolvedCall, $resolvedCall")
            } else {
                println("${node.methodName} seems not to be methodCall")
            }
        }

        if (node.methodName == overloadedFuncStr) {
            if (node.isMethodCall()) {
                val resolvedCall = node.resolveToUElement()
                if (resolvedCall is UMethod) {
                    context.report(SOME_ISSUE, node, context.getLocation(node), firstFuncStr)
                }
                println("${node.methodName} is MethodCall, resolvedCall, $resolvedCall")
            } else {
                println("${node.methodName} seems not to be methodCall")
            }
        }
    }
}