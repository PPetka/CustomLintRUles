package com.ppetka.samples.lintrules.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*
import org.jetbrains.uast.util.isMethodCall
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
        //  context.evaluator.parameterHasType()
        return Asd()
    }

    enum class THREAD {
        MAIN, BACKGROUND
    }

    inner class Asd : UElementHandler() {
        var outermostQuaExpressionList: MutableList<UQualifiedReferenceExpression> = ArrayList()
        var currentThread = THREAD.MAIN

        override fun visitMethod(uMethod: UMethod) {
            //println("M " + uMethod.name)
        }

        override fun visitCallExpression(uCallExpression: UCallExpression) {
            val quaReferenceExpr: UQualifiedReferenceExpression? = uCallExpression.getOutermostQualified()
            if (quaReferenceExpr != null && !outermostQuaExpressionList.contains(quaReferenceExpr)) {
                outermostQuaExpressionList.add(quaReferenceExpr)

                val qualifiedChain: List<UExpression> = quaReferenceExpr.getQualifiedChain()
                /*   qualifiedChain.forEachIndexed { index, uExpression ->
                   }*/
                val composeInChainIndexes: Map<Int, String> = RxHelper.getChainExpressionIndex(qualifiedChain, "compose", "TranHolder", listOf("asd"))
                val composeListSize = composeInChainIndexes.size

                if (composeListSize == 1) {
                    println("compose size 1")
                    val subscribeOnIndexes: Map<Int, String> = RxHelper.getChainExpressionIndex(qualifiedChain, "subscribeOn", "Schedulers", listOf("io", "newThread", "computation"))
                    if (subscribeOnIndexes.size == 1) {
                        currentThread = THREAD.BACKGROUND
                    } else if (subscribeOnIndexes.size > 1) {
                        //report multiple subscribe on calls
                    }
                } else if (composeListSize > 1) {
                    println("compose size > 1")
                    //report multiple compose TranHolder asd Calls
                }
            }
        }
    }

    object RxHelper {
        fun getChainExpressionIndex(expressionList: List<UExpression>, outerMethodName: String, innerClassName: String, innerMethodNames: List<String>): Map<Int, String> {
            val indexWithInnerMethodNameMap: MutableMap<Int, String> = HashMap()
            expressionList.forEachIndexed { index, uExpression ->
                if (uExpression is UCallExpression) {
                    if (uExpression.methodName == outerMethodName) {
                        val soughtMethodName: String? = isSoughMethod(uExpression, innerMethodNames, innerClassName)
                        if (soughtMethodName != null) {
                            indexWithInnerMethodNameMap.put(index, soughtMethodName)
                        }
                    }
                } else {
                    println("Other Type: " + uExpression.toString())
                }
            }
            return indexWithInnerMethodNameMap
        }


        private fun isSoughMethod(callExpr: UCallExpression, innerMethodNames: List<String>, innerClassName: String): String? {
            println("       is Method call: " + callExpr.isMethodCall())
            val listSize: Int = callExpr.valueArguments.size
            if (listSize == 1) {
                val firstFuncParamExpr: UExpression = callExpr.valueArguments[0]
                if (firstFuncParamExpr is UQualifiedReferenceExpression) {
                    val paramExprQuaChain: List<UExpression> = firstFuncParamExpr.getOutermostQualified().getQualifiedChain()

                    var isDesiredClass = false
                    var isDesiredMethod = false
                    var methodName: String? = null
                    paramExprQuaChain.forEach {
                        when (it) {
                            is USimpleNameReferenceExpression -> {
                                isDesiredClass = (innerClassName == it.identifier)
                                println("               uSimpNameRefExpr: " + it.identifier)
                            }
                            is UCallExpression -> {
                                isDesiredMethod = innerMethodNames.contains(it.methodName)
                                methodName = it.methodName
                                println("               uCallExpr: " + it.methodName)
                            }
                        }
                    }
                    //     return isDesiredClass && isDesiredMethod
                    if (isDesiredClass && isDesiredMethod) {
                        println("                   got it")
                        return methodName
                    }
                }

                println("expressionType: " + firstFuncParamExpr.getExpressionType().toString())
                println(firstFuncParamExpr.asRecursiveLogString())

                println("Identifier: " + UastLintUtils.getIdentifier(firstFuncParamExpr))
            }

            println("\n")
            return null
        }
    }
}
