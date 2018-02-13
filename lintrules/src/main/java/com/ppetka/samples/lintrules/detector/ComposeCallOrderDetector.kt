package com.ppetka.samples.lintrules.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*
import org.jetbrains.uast.util.isMethodCall
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Przemysław Petka on 04-Feb-18.
 */
class ComposeCallOrderDetector : Detector(), Detector.UastScanner {
    companion object {
        const val RX_SUBSCRIBEON = "subscribeOn"
        const val RX_OBSERVEON = "observeOn"

        const val SCHEDULERS = "Schedulers"
        const val ANDR_SCHEDULERS = "AndroidSchedulers"

        const val SCHE_CALL_IO = "io"
        const val SCHE_CALL_NEWTHREAD = "newThread"
        const val SCHE_CALL_COMPUTATION = "computation"
        const val SCHE_CALL_MAINTHREAD = "mainThread"

        private const val SOME_CLS = "com.ppetka.samples.customlintrules.S"

        val WRONG_COMPOSE_CALL_ORDER_ISSUE = Issue.create("WrongComposeCallOrder",
                "WrongComposeCallOrder",
                "\"WrongComposeCallOrder\"",
                Category.CORRECTNESS,
                10,
                Severity.ERROR,
                Implementation(ComposeCallOrderDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))

        val MISSING_SUBSCRIBE_ON_ISSUE = Issue.create("MissingSubscribeOn",
                "MissingSubscribeOn",
                "\"MissingSubscribeOn\"",
                Category.CORRECTNESS,
                10,
                Severity.ERROR,
                Implementation(ComposeCallOrderDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))

        val MULTIPLE_SUBSCRIBE_ON_ISSUE = Issue.create("MultipleSubscribeOn",
                "MultipleSubscribeOn",
                "\"MultipleSubscribeOn\"",
                Category.CORRECTNESS,
                10,
                Severity.ERROR,
                Implementation(ComposeCallOrderDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))

        val MULTIPLE_COMPOSE_CALLS_ISSUE = Issue.create("MultipleComposeOn",
                "MultipleComposeOn",
                "\"MultipleComposeOn\"",
                Category.CORRECTNESS,
                10,
                Severity.ERROR,
                Implementation(ComposeCallOrderDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))
    }


    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(UMethod::class.java, UCallExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        //  context.evaluator.parameterHasType()
        return Asd(context)
    }

    enum class THREAD {
        MAIN, BACKGROUND, NONE
    }

    inner class Asd(javaContext: JavaContext) : UElementHandler() {
        val javaContext: JavaContext = javaContext
        var outermostQuaExpressionList: MutableList<UQualifiedReferenceExpression> = ArrayList()
        var currentThread = THREAD.MAIN
        var composeIndex = 0

        override fun visitMethod(uMethod: UMethod) {
            //println("M " + uMethod.name)
        }

        override fun visitCallExpression(uCallExpression: UCallExpression) {
            val quaReferenceExpr: UQualifiedReferenceExpression? = uCallExpression.getOutermostQualified()
            if (quaReferenceExpr != null && !outermostQuaExpressionList.contains(quaReferenceExpr)) {
                outermostQuaExpressionList.add(quaReferenceExpr)

                if (containsCompose(quaReferenceExpr)) {
                    val subscribeOnThread: THREAD? = getSubscribeOnThread(quaReferenceExpr)
                    println("           subscribeOnThread: $subscribeOnThread")
                    subscribeOnThread?.let {
                        currentThread = subscribeOnThread
                        var observeOnCalls: List<Triple<Int, String, String>> = RxHelper.getChainExpressionIndex(
                                quaReferenceExpr,
                                RX_OBSERVEON,
                                listOf(SCHEDULERS, ANDR_SCHEDULERS),
                                listOf(SCHE_CALL_IO, SCHE_CALL_NEWTHREAD, SCHE_CALL_COMPUTATION, SCHE_CALL_MAINTHREAD))


                        observeOnCalls = observeOnCalls.filter { it.first < composeIndex }
                        println("observeOnCalls size: ${observeOnCalls.size}, lastIndex: ${observeOnCalls.lastIndex}")
                        if (observeOnCalls.isNotEmpty()) {
                            val triple = observeOnCalls[observeOnCalls.lastIndex]
                            println("observe on before compose: $triple")

                            if (triple.second == SCHEDULERS) {
                                currentThread = THREAD.BACKGROUND
                            } else if (triple.second == ANDR_SCHEDULERS) {
                                currentThread = THREAD.MAIN
                            }
                        } else {
                            println("   list is empty: compose index: $composeIndex")
                        }

                        //finally check the compose call thread
                        if (currentThread == THREAD.BACKGROUND) {
                            println("compose called on BACKGROUND thread")
                            javaContext.report(WRONG_COMPOSE_CALL_ORDER_ISSUE, quaReferenceExpr, javaContext.getLocation(quaReferenceExpr), WRONG_COMPOSE_CALL_ORDER_ISSUE.getBriefDescription(TextFormat.TEXT))
                            //report compose called on background thread
                        } else {
                            println("compose called on MAIN thread")
                        }
                    }
                }
            }
        }

        private fun getSubscribeOnThread(quaRefExpression: UQualifiedReferenceExpression): THREAD? {
            //todo check full class
            //io.reactivex.schedulers.Schedulers
            //io.reactivex.android.schedulers.AndroidSchedulers
            val qualifiedChain = quaRefExpression.getQualifiedChain()

            val subscribeOnCallSite: List<Triple<Int, String, String>> = RxHelper.getChainExpressionIndex(
                    quaRefExpression,
                    RX_SUBSCRIBEON,
                    listOf(SCHEDULERS, ANDR_SCHEDULERS),
                    listOf(SCHE_CALL_IO, SCHE_CALL_NEWTHREAD, SCHE_CALL_COMPUTATION, SCHE_CALL_MAINTHREAD))

            val subOnListSize = subscribeOnCallSite.size
            if (subOnListSize == 1) {
                if (subscribeOnCallSite[0].second == SCHEDULERS) {
                    return THREAD.BACKGROUND
                } else if (subscribeOnCallSite[0].second == ANDR_SCHEDULERS) {
                    return THREAD.MAIN
                }
            } else if (subOnListSize > 1) {
                //report multiple sub on calls
                println("       subscribeOn more than 1 expression: quaChain: $qualifiedChain")
                javaContext.report(MULTIPLE_SUBSCRIBE_ON_ISSUE, qualifiedChain[subscribeOnCallSite[0].first], javaContext.getLocation(qualifiedChain[subscribeOnCallSite[0].first]), MULTIPLE_SUBSCRIBE_ON_ISSUE.getBriefDescription(TextFormat.TEXT))
            } else {
                println("       subscribeOn missing: quaChain: $qualifiedChain")
                javaContext.report(MISSING_SUBSCRIBE_ON_ISSUE, quaRefExpression, javaContext.getLocation(quaRefExpression), MISSING_SUBSCRIBE_ON_ISSUE.getBriefDescription(TextFormat.TEXT))
                //report missing subOnCall
            }
            return null
        }

        private fun containsCompose(quaRefExpression: UQualifiedReferenceExpression): Boolean {
            val composeInChainIndexes: List<Triple<Int, String, String>> = RxHelper.getChainExpressionIndex(quaRefExpression, "compose", listOf("TranHolder"), listOf("asd"))

            val composeListSize = composeInChainIndexes.size
            if (composeListSize == 1) {
                println("compose size 1")
                composeIndex = composeInChainIndexes[0].first
                return true
            } else if (composeListSize > 1) {
                println("compose size > 1")
                javaContext.report(MULTIPLE_COMPOSE_CALLS_ISSUE, quaRefExpression, javaContext.getLocation(quaRefExpression), MULTIPLE_COMPOSE_CALLS_ISSUE.getBriefDescription(TextFormat.TEXT))
                //report multiple compose TranHolder asd Calls
            }
            return false
        }
    }

    object RxHelper {
        //return index, innerClass, innerMethod
        fun getChainExpressionIndex(quaRefExpression: UQualifiedReferenceExpression, outerMethodName: String, innerClassName: List<String>, innerMethodNames: List<String>): List<Triple<Int, String, String>> {
            val indexClassMethod: MutableList<Triple<Int, String, String>> = ArrayList()
            val expressionList = quaRefExpression.getQualifiedChain()
            expressionList.forEachIndexed { index, uExpression ->
                if (uExpression is UCallExpression) {
                    if (uExpression.methodName == outerMethodName) {
                        val classMethod: Pair<String, String>? = isSearchedCallExpression(uExpression, innerMethodNames, innerClassName)
                        classMethod?.let {
                            indexClassMethod.add(Triple(index, classMethod.first, classMethod.second))
                        }
                    }
                } else {
                    println("Not an uCallExpression: " + uExpression.toString())
                }
            }
            return indexClassMethod
        }


        //return className , methodName
        private fun isSearchedCallExpression(callExpr: UCallExpression, mthdNames: List<String>, clsNames: List<String>): Pair<String, String>? {
            println("       mcall ${callExpr.isMethodCall()} expression: $callExpr , mthdNames: $mthdNames, clsNames: $clsNames")
            val listSize: Int = callExpr.valueArguments.size
            if (listSize == 1) {
                val firstArg: UExpression = callExpr.valueArguments[0]
                if (firstArg is UQualifiedReferenceExpression) {
                    val paramExprQuaChain: List<UExpression> = firstArg.getOutermostQualified().getQualifiedChain()

                    var isDesiredClass = false
                    var isDesiredMethod = false
                    var mthdName: String = ""
                    var clsName: String = ""
                    paramExprQuaChain.forEach {
                        when (it) {
                            is USimpleNameReferenceExpression -> {
                                isDesiredClass = (clsNames.contains(it.identifier))
                                if (isDesiredClass) {
                                    clsName = it.identifier
                                }
                                println("               uSimpNameRefExpr: " + it.identifier)
                            }
                            is UCallExpression -> {
                                isDesiredMethod = mthdNames.contains(it.methodName)
                                if (isDesiredMethod) {
                                    it.methodName?.let {
                                        mthdName = it
                                    }
                                }
                                println("               uCallExpr: " + it.methodName)
                            }
                        }
                    }
                    //     return isDesiredClass && isDesiredMethod
                    if (isDesiredClass && isDesiredMethod) {
                        println("                   got it: clsName: $clsName, methodName: $mthdName")
                        return Pair(clsName, mthdName)
                    } else {
                        println("                   not this: $clsName, $mthdName")
                    }
                }

                println("expressionType: " + firstArg.getExpressionType().toString())
                println(firstArg.asRecursiveLogString())

                println("Identifier: " + UastLintUtils.getIdentifier(firstArg))
            }

            println("\n")
            return null
        }
    }
}
