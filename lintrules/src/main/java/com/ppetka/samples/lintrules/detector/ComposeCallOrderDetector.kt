package com.ppetka.samples.lintrules.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*
import org.jetbrains.uast.util.isMethodCall
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * Created by Przemysław Petka on 04-Feb-18.
 */
class ComposeCallOrderDetector : Detector(), Detector.UastScanner {

    enum class THREAD {
        MAIN, BACKGROUND
    }

    companion object {
        const val SCHEDULERS = "io.reactivex.schedulers.Schedulers"
        const val ANDR_SCHEDULERS = "io.reactivex.android.schedulers.AndroidSchedulers"

        const val DESIRED_CLS = "fooo.tran.TranHolder"
        const val DESIRED_CLS_METHOD = "asd"

        const val RX_SUBSCRIBEON = "subscribeOn"
        const val RX_OBSERVEON = "observeOn"
        const val RX_COMPOSE = "compose"

        const val SCHE_CALL_IO = "io"
        const val SCHE_CALL_NEWTHREAD = "newThread"
        const val SCHE_CALL_COMPUTATION = "computation"
        const val SCHE_CALL_MAINTHREAD = "mainThread"


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
        return listOf(UQualifiedReferenceExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return AnaliseRxExpressionDetector(context)
        //  context.evaluator.parameterHasType()
    }

    inner class AnaliseRxExpressionDetector(javaContext: JavaContext) : UElementHandler() {
        var uniqueQuaRefs: MutableSet<UQualifiedReferenceExpression> = HashSet()
        val javaContext: JavaContext = javaContext
        var currentThread = THREAD.MAIN
        var composeIndex = 0
        //debug
        var ctr = 1

        override fun visitQualifiedReferenceExpression(quaReferenceExpr: UQualifiedReferenceExpression) {
            if (quaReferenceExpr.getOutermostQualified() !in uniqueQuaRefs) {
                uniqueQuaRefs.add(quaReferenceExpr)

                if (ctr == 1) {
                    println("$ctr, ${quaReferenceExpr.asRecursiveLogString()}")
                }
                println("$ctr, visitQualifiedReferenceExpression(), STARTING $quaReferenceExpr")
                ctr++

                if (containsCompose(quaReferenceExpr)) {
                    val subscribeOnThread: THREAD? = getSubscribeOnThread(quaReferenceExpr)
                    println("visitQualifiedReferenceExpression(), subscribeOnThread: $subscribeOnThread")
                    subscribeOnThread?.let {
                        currentThread = subscribeOnThread

                        var observeOnCalls: List<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> = RxHelper.getChainExpressionIndex(
                                quaRefExpression = quaReferenceExpr,
                                outerMethodName = RX_OBSERVEON,
                                innerClassName = listOf(SCHEDULERS, ANDR_SCHEDULERS),
                                innerMethodNames = listOf(SCHE_CALL_IO, SCHE_CALL_NEWTHREAD, SCHE_CALL_COMPUTATION, SCHE_CALL_MAINTHREAD))

                        observeOnCalls = observeOnCalls.filter { it.first < composeIndex }
                        println("visitQualifiedReferenceExpression(), observeOnCalls size: ${observeOnCalls.size}, lastIndex: ${observeOnCalls.lastIndex}")
                        if (observeOnCalls.isNotEmpty()) {
                            val triple = observeOnCalls[observeOnCalls.lastIndex]
                            println("visitQualifiedReferenceExpression(), observe on before compose: $triple")

                            val clsName = triple.second.getQualifiedName()
                            if (clsName == SCHEDULERS) {
                                currentThread = THREAD.BACKGROUND
                            } else if (clsName == ANDR_SCHEDULERS) {
                                currentThread = THREAD.MAIN
                            }
                        } else {
                            println("visitQualifiedReferenceExpression(), list is empty: compose index: $composeIndex")
                        }

                        //finally check the compose call thread
                        if (currentThread == THREAD.BACKGROUND) {
                            println("visitQualifiedReferenceExpression(), FINALY compose called on BACKGROUND thread")
                            quaReferenceExpr.let {
                                javaContext.report(WRONG_COMPOSE_CALL_ORDER_ISSUE, quaReferenceExpr, javaContext.getLocation(quaReferenceExpr), WRONG_COMPOSE_CALL_ORDER_ISSUE.getBriefDescription(TextFormat.TEXT))
                            }
                            //report compose called on background thread
                        } else {
                            println("visitQualifiedReferenceExpression(), FINALY compose called on MAIN thread")
                        }
                    }
                }
                println("\n")
            }
        }


        private fun getSubscribeOnThread(quaRefExpression: UQualifiedReferenceExpression): THREAD? {
            println("       getSubscribeOnThread(), quaRefExpression: $quaRefExpression")
            val subscribeOnCallSite: List<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> = RxHelper.getChainExpressionIndex(
                    quaRefExpression = quaRefExpression,
                    outerMethodName = RX_SUBSCRIBEON,
                    innerClassName = listOf(SCHEDULERS, ANDR_SCHEDULERS),
                    innerMethodNames = listOf(SCHE_CALL_IO, SCHE_CALL_NEWTHREAD, SCHE_CALL_COMPUTATION, SCHE_CALL_MAINTHREAD))
            println("       getSubscribeOnThread() after  RxHelper.getChainExpressionIndex call:")
            val subOnListSize = subscribeOnCallSite.size
            if (subOnListSize == 1) {
                println("       getSubscribeOnThread(), subOnListSize: $subOnListSize, listToStr: $subscribeOnCallSite, clsName: ${subscribeOnCallSite[0].second.getQualifiedName()}, method, ${subscribeOnCallSite[0].third.methodName}")
                val clsName: String? = subscribeOnCallSite[0].second.getQualifiedName()
                println("       getSubscribeOnThread(), clsName: $clsName")
                if (clsName == SCHEDULERS) {
                    return THREAD.BACKGROUND
                } else if (clsName == ANDR_SCHEDULERS) {
                    return THREAD.MAIN
                }
            } else if (subOnListSize > 1) {
                quaRefExpression.uastParent?.let {
                    //report multiple sub on calls
                    println("       getSubscribeOnThread(), subscribeOn more than 1 expression: quaChain: ${it}")
                    javaContext.report(MULTIPLE_SUBSCRIBE_ON_ISSUE, it, javaContext.getLocation(it), MULTIPLE_SUBSCRIBE_ON_ISSUE.getBriefDescription(TextFormat.TEXT))

                }
            } else {
                quaRefExpression.uastParent?.let { uuu ->
                    uuu.uastParent?.let {
                        println("       getSubscribeOnThread(), subscribeOn missing: quaChain: $it")
                        javaContext.report(MISSING_SUBSCRIBE_ON_ISSUE, it, javaContext.getLocation(it), MISSING_SUBSCRIBE_ON_ISSUE.getBriefDescription(TextFormat.TEXT))
                    }
                }
                //report missing subOnCall
            }
            println("getSubscribeOnThread() return null")
            return null
        }

        private fun containsCompose(quaRefExpression: UQualifiedReferenceExpression): Boolean {
            val composeInChainIndexes: List<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> = RxHelper.getChainExpressionIndex(quaRefExpression, RX_COMPOSE, listOf(DESIRED_CLS), listOf(DESIRED_CLS_METHOD))

            val composeListSize = composeInChainIndexes.size
            if (composeListSize == 1) {
                println("       containsCompose(), compose size 1: compose list: $composeInChainIndexes")
                composeIndex = composeInChainIndexes[0].first
                return true
            } else if (composeListSize > 1) {
                println("       containsCompose(), compose size > 1")
                javaContext.report(MULTIPLE_COMPOSE_CALLS_ISSUE, quaRefExpression, javaContext.getLocation(quaRefExpression), MULTIPLE_COMPOSE_CALLS_ISSUE.getBriefDescription(TextFormat.TEXT))
                //report multiple compose TranHolder asd Calls
            }
            println("       containsCompose() return false")
            return false
        }
    }

    object RxHelper {
        //return index, innerClass, innerMethod
        fun getChainExpressionIndex(quaRefExpression: UQualifiedReferenceExpression, outerMethodName: String, innerClassName: List<String>, innerMethodNames: List<String>): List<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> {
            val indexClassMethod: MutableList<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> = ArrayList()
            val expressionList = quaRefExpression.getQualifiedChain()
            expressionList.forEachIndexed { index, uExpression ->
                if (uExpression is UCallExpression) {
                    if (uExpression.methodName == outerMethodName) {
                        val matchedExpression: Pair<USimpleNameReferenceExpression, UCallExpression>? = callMatchesAtLeastOneOfMethods(uExpression, innerMethodNames, innerClassName)
                        matchedExpression?.let {
                            println("           getChainExpressionIndex(), matchedExpression: $matchedExpression , clsName: ${matchedExpression.first.getQualifiedName()}, methodName: ${matchedExpression.second.methodName}")
                            indexClassMethod.add(Triple(index, matchedExpression.first, matchedExpression.second))
                        }
                    }
                } else {
                    println("           getChainExpressionIndex() Not an uCallExpression: " + uExpression.toString())
                }
            }
            println("           getChainExpressionIndex() return: indexClassMethod: $indexClassMethod ")
            return indexClassMethod
        }

        //return className , methodName
        private fun callMatchesAtLeastOneOfMethods(callExpr: UCallExpression, mthdNames: List<String>, clsNames: List<String>): Pair<USimpleNameReferenceExpression, UCallExpression>? {
            println("           callMatchesAtLeastOneOfMethods() mcall ${callExpr.isMethodCall()} expression: $callExpr , mthdNames: $mthdNames, clsNames: $clsNames")
            val listSize: Int = callExpr.valueArguments.size
            if (listSize == 1) {
                val firstArg: UExpression = callExpr.valueArguments[0]

                println("           callMatchesAtLeastOneOfMethods(), firstArg: ${firstArg.getExpressionType()?.canonicalText}")
                if (firstArg is UQualifiedReferenceExpression) {
                    println("           callMatchesAtLeastOneOfMethods(), firstArg is UQuaReferenceExpr : ${firstArg.getExpressionType()?.canonicalText}")
                    val paramExprQuaChain: List<UExpression> = firstArg.getOutermostQualified().getQualifiedChain()

                    var mthd: UCallExpression? = null
                    var cls: USimpleNameReferenceExpression? = null
                    paramExprQuaChain.forEach { quaChild ->
                        when (quaChild) {
                            is USimpleNameReferenceExpression -> {
                                println("           callMatchesAtLeastOneOfMethods() lets try to resolve, resolved name: ${quaChild.resolvedName}, resolved ${quaChild.resolve()} , resolvedtouelement: ${quaChild.resolveToUElement()} ")

                                quaChild.resolveToUElement()?.let {
                                    if (it is UClass) {
                                        val isDesiredClass: Boolean = (clsNames.contains(it.qualifiedName))
                                        if (isDesiredClass) {
                                            cls = quaChild
                                        }
                                        println("           callMatchesAtLeastOneOfMethods(), UClass: QuaName; ${it.qualifiedName}")
                                    } else {
                                        println("           allMatchesAtLeastOneOfMethods(), not a UClass")
                                    }
                                }
                            }
                            is UCallExpression -> {
                                val isDesiredMethod = mthdNames.contains(quaChild.methodName)
                                if (isDesiredMethod) {
                                    mthd = quaChild
                                }
                                println("           allMatchesAtLeastOneOfMethods(), uCallExpr" + quaChild.methodName)
                            }
                        }
                    }

                    if (mthd != null && cls != null) {
                        println("           allMatchesAtLeastOneOfMethods(), FOUND: clsName: ${cls.getQualifiedName()}, methodName: ${mthd?.methodName}")
                        mthd?.let { m ->
                            cls?.let { c ->
                                return Pair(c, m)
                            }
                        }
                    } else {
                        println("           allMatchesAtLeastOneOfMethods(), NOT FOUND: clsName: ${cls.getQualifiedName()}, methodName: ${mthd?.methodName}")
                    }
                }
            }
            println("callMatchesAtLeastOneOfMethods() return null")
            return null
        }
    }
}