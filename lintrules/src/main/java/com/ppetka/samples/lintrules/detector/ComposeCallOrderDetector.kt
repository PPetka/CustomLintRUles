package com.ppetka.samples.lintrules.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*
import java.util.*
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

        const val DESIRED_CLS = "com.ppetka.samples.customlintrules.SomeCls"
        const val DESIRED_CLS_METHOD = "composeSomething"

        const val RX_SUBSCRIBEON = "subscribeOn"
        const val RX_OBSERVEON = "observeOn"
        const val RX_COMPOSE = "compose"

        const val SCHE_CALL_IO = "io"
        const val SCHE_CALL_NEWTHREAD = "newThread"
        const val SCHE_CALL_COMPUTATION = "computation"
        const val SCHE_CALL_MAINTHREAD = "mainThread"


        val WRONG_COMPOSE_CALL_ORDER_ISSUE: Issue = Issue.create("WrongComposeCallOrder",
                "WrongComposeCallOrder",
                "\"WrongComposeCallOrder\"",
                Category.CORRECTNESS,
                10,
                Severity.ERROR,
                Implementation(ComposeCallOrderDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))

        val MISSING_SUBSCRIBE_ON_ISSUE: Issue = Issue.create("MissingSubscribeOn",
                "MissingSubscribeOn",
                "\"MissingSubscribeOn\"",
                Category.CORRECTNESS,
                10,
                Severity.ERROR,
                Implementation(ComposeCallOrderDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))

        val MULTIPLE_SUBSCRIBE_ON_ISSUE: Issue = Issue.create("MultipleSubscribeOn",
                "MultipleSubscribeOn",
                "\"MultipleSubscribeOn\"",
                Category.CORRECTNESS,
                10,
                Severity.ERROR,
                Implementation(ComposeCallOrderDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE))).addMoreInfo("MOOOOOOOOOOOOOOOOOOOOOOORE")

        val MULTIPLE_COMPOSE_CALLS_ISSUE: Issue = Issue.create("MultipleComposeOn",
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
    }

    inner class AnaliseRxExpressionDetector(private val javaContext: JavaContext) : UElementHandler() {
        var uniqueQuaRefs: MutableSet<UQualifiedReferenceExpression> = HashSet()

        override fun visitQualifiedReferenceExpression(quaReferenceExpr: UQualifiedReferenceExpression) {
            var currentThread: THREAD?
            var composeIndex: Int

            val outermostQueRefExpr = quaReferenceExpr.getOutermostQualified()
            outermostQueRefExpr?.let {
                if (outermostQueRefExpr !in uniqueQuaRefs) {
                    uniqueQuaRefs.add(outermostQueRefExpr)

                    composeIndex = getComposeCallIndex(outermostQueRefExpr)
                    if (composeIndex != 0) {
                        val subscribeOnCallThread: THREAD? = checkSubscribeOnCallThread(outermostQueRefExpr)
                        subscribeOnCallThread?.let {
                            currentThread = subscribeOnCallThread

                            var observeOnCalls: List<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> = outermostQueRefExpr.getNestedChainCalls(
                                    outerMethodName = RX_OBSERVEON,
                                    innerClassName = listOf(SCHEDULERS, ANDR_SCHEDULERS),
                                    innerMethodNames = listOf(SCHE_CALL_IO, SCHE_CALL_NEWTHREAD, SCHE_CALL_COMPUTATION, SCHE_CALL_MAINTHREAD))

                            observeOnCalls = observeOnCalls.filter { it.first < composeIndex }
                            if (observeOnCalls.isNotEmpty()) {
                                val observeOnBeforeComposeCall = observeOnCalls[observeOnCalls.lastIndex]

                                val observeOnArgCls = observeOnBeforeComposeCall.second.getQualifiedName()
                                if (observeOnArgCls == SCHEDULERS) {
                                    currentThread = THREAD.BACKGROUND
                                } else if (observeOnArgCls == ANDR_SCHEDULERS) {
                                    currentThread = THREAD.MAIN
                                }
                            }
                            //finally check the compose call thread
                            if (currentThread == THREAD.BACKGROUND) {
                                val composeCall = outermostQueRefExpr.getQualifiedChain()[composeIndex]
                                javaContext.report(WRONG_COMPOSE_CALL_ORDER_ISSUE, composeCall, javaContext.getLocation(composeCall), WRONG_COMPOSE_CALL_ORDER_ISSUE.getBriefDescription(TextFormat.TEXT))
                            }
                        }
                    }
                }
            }
        }

        private fun checkSubscribeOnCallThread(quaRefExpression: UQualifiedReferenceExpression): THREAD? {
            val subscribeOnCallSite: List<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> = quaRefExpression.getNestedChainCalls(
                    outerMethodName = RX_SUBSCRIBEON,
                    innerClassName = listOf(SCHEDULERS, ANDR_SCHEDULERS),
                    innerMethodNames = listOf(SCHE_CALL_IO, SCHE_CALL_NEWTHREAD, SCHE_CALL_COMPUTATION, SCHE_CALL_MAINTHREAD))

            val subOnListSize = subscribeOnCallSite.size
            if (subOnListSize == 1) {
                val clsName: String? = subscribeOnCallSite[0].second.getQualifiedName()
                if (clsName == SCHEDULERS) {
                    return THREAD.BACKGROUND
                } else if (clsName == ANDR_SCHEDULERS) {
                    return THREAD.MAIN
                }
            } else if (subOnListSize > 1) {
                //report multiple sub on calls
                var callParent: UExpression = subscribeOnCallSite[subOnListSize - 1].third
                callParent = callParent.getParentOfType(true, UCallExpression::class.java) ?: callParent
                javaContext.report(MULTIPLE_SUBSCRIBE_ON_ISSUE, javaContext.getLocation(callParent), callParent.toString())

            } else {
                javaContext.report(MISSING_SUBSCRIBE_ON_ISSUE, javaContext.getLocation(quaRefExpression), MISSING_SUBSCRIBE_ON_ISSUE.getBriefDescription(TextFormat.TEXT))
            }
            return null
        }

        private fun getComposeCallIndex(quaRefExpression: UQualifiedReferenceExpression): Int {
            val composeInChainIndexes: List<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> = quaRefExpression.getNestedChainCalls(RX_COMPOSE, listOf(DESIRED_CLS), listOf(DESIRED_CLS_METHOD))

            val composeListSize = composeInChainIndexes.size
            if (composeListSize == 1) {
                return composeInChainIndexes[0].first
            } else if (composeListSize > 1) {
                javaContext.report(MULTIPLE_COMPOSE_CALLS_ISSUE, quaRefExpression, javaContext.getLocation(quaRefExpression), MULTIPLE_COMPOSE_CALLS_ISSUE.getBriefDescription(TextFormat.TEXT))
            }
            return 0
        }
    }

    fun UQualifiedReferenceExpression.getNestedChainCalls(outerMethodName: String, innerClassName: List<String>, innerMethodNames: List<String>): List<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> {
        val indexClassMethod: MutableList<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> = ArrayList()
        val expressionList = this.getQualifiedChain()
        expressionList.forEachIndexed { index, uExpression ->
            if (uExpression is UCallExpression) {
                if (uExpression.methodName == outerMethodName) {
                    val matchedExpression: Pair<USimpleNameReferenceExpression, UCallExpression>? = uExpression.getSearchedCall(innerClassName, innerMethodNames)
                    matchedExpression?.let {
                        indexClassMethod.add(Triple(index, matchedExpression.first, matchedExpression.second))
                    }
                }
            }
        }
        return indexClassMethod
    }

    private fun UCallExpression.getSearchedCall(clsNames: List<String>, mthdNames: List<String>): Pair<USimpleNameReferenceExpression, UCallExpression>? {
        var pairResult: Pair<USimpleNameReferenceExpression, UCallExpression>? = null
        val listSize: Int = this.valueArguments.size
        if (listSize == 1) {
            val firstArg: UExpression = this.valueArguments[0]
            if (firstArg is UQualifiedReferenceExpression) {
                val paramExprQuaChain: List<UExpression> = firstArg.getOutermostQualified().getQualifiedChain()

                var mthd: UCallExpression? = null
                var cls: USimpleNameReferenceExpression? = null
                paramExprQuaChain.forEach { quaChild ->
                    when (quaChild) {
                        is USimpleNameReferenceExpression -> {
                            quaChild.resolveToUElement()?.let {
                                if (it is UClass) {
                                    val isDesiredClass: Boolean = (clsNames.contains(it.qualifiedName))
                                    if (isDesiredClass) {
                                        cls = quaChild
                                    }
                                }
                            }
                        }
                        is UCallExpression -> {
                            val isDesiredMethod = mthdNames.contains(quaChild.methodName)
                            if (isDesiredMethod) {
                                mthd = quaChild
                            }
                        }
                    }
                }

                (cls to mthd).biLet { c, m ->
                    pairResult = Pair(c, m)
                }
            }
        }
        return pairResult
    }

    fun <T, U, R> Pair<T?, U?>.biLet(body: (T, U) -> R): R? {
        val fst: T? = first
        val scnd: U? = second
        fst?.let { f ->
            scnd?.let { u ->
                return body(f, u)
            }
        }
        return null
    }
}
