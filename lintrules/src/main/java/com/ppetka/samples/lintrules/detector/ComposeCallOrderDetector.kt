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
                    println("after compose")
                    val aaa = quaReferenceExpr
                    val subscribeOnThread: THREAD? = getSubscribeOnThread(quaReferenceExpr)
                    println("           subscribeOnThread: $subscribeOnThread")
                    subscribeOnThread?.let {
                        currentThread = subscribeOnThread

                        var observeOnCalls: List<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> = RxHelper.getChainExpressionIndex(
                                quaRefExpression = quaReferenceExpr,
                                outerMethodName = RX_OBSERVEON,
                                innerClassName = listOf(SCHEDULERS, ANDR_SCHEDULERS),
                                innerMethodNames = listOf(SCHE_CALL_IO, SCHE_CALL_NEWTHREAD, SCHE_CALL_COMPUTATION, SCHE_CALL_MAINTHREAD))

                        observeOnCalls = observeOnCalls.filter { it.first < composeIndex }
                        println("observeOnCalls size: ${observeOnCalls.size}, lastIndex: ${observeOnCalls.lastIndex}")
                        if (observeOnCalls.isNotEmpty()) {
                            val triple = observeOnCalls[observeOnCalls.lastIndex]
                            println("observe on before compose: $triple")

                            val clsName = triple.second.getQualifiedName()
                            if (clsName == SCHEDULERS) {
                                currentThread = THREAD.BACKGROUND
                            } else if (clsName == ANDR_SCHEDULERS) {
                                currentThread = THREAD.MAIN
                            }
                        } else {
                            println("   list is empty: compose index: $composeIndex")
                        }

                        //finally check the compose call thread
                        if (currentThread == THREAD.BACKGROUND) {
                            println("compose called on BACKGROUND thread")
                            aaa?.let {
                                javaContext.report(WRONG_COMPOSE_CALL_ORDER_ISSUE, aaa, javaContext.getLocation(aaa), WRONG_COMPOSE_CALL_ORDER_ISSUE.getBriefDescription(TextFormat.TEXT))
                            }
                            //report compose called on background thread
                        } else {
                            println("compose called on MAIN thread")
                        }
                    }
                }
            }
        }

        private fun getSubscribeOnThread(quaRefExpression: UQualifiedReferenceExpression): THREAD? {
            println("getSubscribeOnThread $quaRefExpression")
            val qualifiedChain = quaRefExpression.getQualifiedChain()

            val subscribeOnCallSite: List<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> = RxHelper.getChainExpressionIndex(
                    quaRefExpression = quaRefExpression,
                    outerMethodName = RX_SUBSCRIBEON,
                    innerClassName = listOf(SCHEDULERS, ANDR_SCHEDULERS),
                    innerMethodNames = listOf(SCHE_CALL_IO, SCHE_CALL_NEWTHREAD, SCHE_CALL_COMPUTATION, SCHE_CALL_MAINTHREAD))
            println("getSubscribeOnThread() after  RxHelper.getChainExpressionIndex call:")
            val subOnListSize = subscribeOnCallSite.size
            if (subOnListSize == 1) {
                println("getSubscribeOnThread(), subOnListSize: $subOnListSize, listToStr: $subscribeOnCallSite, clsName: ${subscribeOnCallSite[0].second.getQualifiedName()}, method, ${subscribeOnCallSite[0].third.methodName}")
                val clsName: String? = subscribeOnCallSite[0].second.getQualifiedName()
                println("getSubscribeOnThread(), clsName: $clsName")
                if (clsName == SCHEDULERS) {
                    return THREAD.BACKGROUND
                } else if (clsName == ANDR_SCHEDULERS) {
                    return THREAD.MAIN
                }
            } else if (subOnListSize > 1) {
                quaRefExpression.uastParent?.let {
                    //report multiple sub on calls
                    println("       subscribeOn more than 1 expression: quaChain: ${it}")
                    javaContext.report(MULTIPLE_SUBSCRIBE_ON_ISSUE, it, javaContext.getLocation(it), MULTIPLE_SUBSCRIBE_ON_ISSUE.getBriefDescription(TextFormat.TEXT))

                }
            } else {
                var parent = quaRefExpression.uastParent
                parent?.let { uuu ->
                    uuu.uastParent?.let {
                        println("       subscribeOn missing: quaChain: $it")
                        javaContext.report(MISSING_SUBSCRIBE_ON_ISSUE, it, javaContext.getLocation(it), MISSING_SUBSCRIBE_ON_ISSUE.getBriefDescription(TextFormat.TEXT))
                    }
                }
                //report missing subOnCall
            }
            return null
        }

        private fun containsCompose(quaRefExpression: UQualifiedReferenceExpression): Boolean {
            val composeInChainIndexes: List<Triple<Int, USimpleNameReferenceExpression, UCallExpression>> = RxHelper.getChainExpressionIndex(quaRefExpression, RX_COMPOSE, listOf(DESIRED_CLS), listOf(DESIRED_CLS_METHOD))

            val composeListSize = composeInChainIndexes.size
            if (composeListSize == 1) {
                println("compose size 1: compose list: $composeInChainIndexes")
                composeIndex = composeInChainIndexes[0].first
                return true
            } else if (composeListSize > 1) {
                println("compose size > 1")
                javaContext.report(MULTIPLE_COMPOSE_CALLS_ISSUE, quaRefExpression, javaContext.getLocation(quaRefExpression), MULTIPLE_COMPOSE_CALLS_ISSUE.getBriefDescription(TextFormat.TEXT))
                //report multiple compose TranHolder asd Calls
            }
            return false
        }

        private fun getClsName(clsExpression: USimpleNameReferenceExpression): String? {
            if (clsExpression is UClass) {
                return clsExpression.qualifiedName
            }
            return null
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
                            println("getChainExpressionIndex(), matchedExpression: $matchedExpression , clsName: ${matchedExpression.first.getQualifiedName()}, methodName: ${matchedExpression.second.methodName}")
                            indexClassMethod.add(Triple(index, matchedExpression.first, matchedExpression.second))
                        }
                    }
                } else {
                    println("Not an uCallExpression: " + uExpression.toString())
                }
            }
            println("getChainExpressionIndex() return: indexClassMethod: $indexClassMethod ")
            return indexClassMethod
        }

        //return className , methodName
        private fun callMatchesAtLeastOneOfMethods(callExpr: UCallExpression, mthdNames: List<String>, clsNames: List<String>): Pair<USimpleNameReferenceExpression, UCallExpression>? {
            println("       mcall ${callExpr.isMethodCall()} expression: $callExpr , mthdNames: $mthdNames, clsNames: $clsNames")
            val listSize: Int = callExpr.valueArguments.size
            if (listSize == 1) {
                val firstArg: UExpression = callExpr.valueArguments[0]

                println("       AAAAA CANO TEXT: ${firstArg.getExpressionType()?.canonicalText}, BBBBB: internal cano text ${firstArg.getExpressionType()?.internalCanonicalText}")
                if (firstArg is UQualifiedReferenceExpression) {
                    println("       CCCCCCCCC CANO TEXT: ${firstArg.getExpressionType()?.canonicalText}, DDDDDDDDDDDd: internal cano text ${firstArg.asRecursiveLogString()}")
                    val paramExprQuaChain: List<UExpression> = firstArg.getOutermostQualified().getQualifiedChain()

                    var mthd: UCallExpression? = null
                    var cls: USimpleNameReferenceExpression? = null
                    paramExprQuaChain.forEach { quaChild ->
                        when (quaChild) {
                            is USimpleNameReferenceExpression -> {
                                println("lets try to resolve, resolved name: ${quaChild.resolvedName}, resolved ${quaChild.resolve()} , resolvedtouelement: ${quaChild.resolveToUElement()} ")
                                quaChild.resolveToUElement()?.let {
                                    if (it is UClass) {
                                        val isDesiredClass: Boolean = (clsNames.contains(it.qualifiedName))
                                        if (isDesiredClass) {
                                            cls = quaChild
                                        }
                                        println("is UClass: QuaName; ${it.qualifiedName}")
                                    } else {
                                        println("not a UClass")
                                    }
                                }
                                println("               uSimpNameRefExpr: " + quaChild.identifier)

                            }
                            is UCallExpression -> {
                                val isDesiredMethod = mthdNames.contains(quaChild.methodName)
                                if (isDesiredMethod) {
                                    mthd = quaChild
                                }
                                println("               uCallExpr: " + quaChild.methodName)
                            }
                        }
                    }

                    if (mthd != null && cls != null) {
                        println("                   got it: clsName: ${cls.getQualifiedName()}, methodName: ${mthd?.methodName}")
                        mthd?.let { m ->
                            cls?.let { c ->
                                return Pair(c, m)
                            }
                        }
                    } else {
                        println("                   not this: clsName: ${cls.getQualifiedName()}, methodName: ${mthd?.methodName}")
                    }
                }
            }
            return null
        }
    }
}