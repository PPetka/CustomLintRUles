package com.ppetka.samples.lintrules.detector.bugs

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.resolveToUElement
import org.jetbrains.uast.util.isMethodCall
import java.util.*

/**
 * Created by Przemysław Petka on 3/12/2018.
 */

class UastScannerQuaChainBugDetector : Detector(), Detector.UastScanner {
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
                Implementation(UastScannerQuaChainBugDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))
    }

    //UastScanner
    override fun visitMethod(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        println(node.methodName)
        report(context, node)
    }

    override fun getApplicableMethodNames(): MutableList<String> {
        return mutableListOf(firstFuncStr, secondFuncStr, thirdFuncStr, overloadedFuncStr)
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
