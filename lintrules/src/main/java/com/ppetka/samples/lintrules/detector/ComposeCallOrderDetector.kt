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
        val RX_SUBSCRIBEON = "subscribeOn"
        val RX_OBSERVEON = "observeOn"

        val SCHEDULERS = "Schedulers"
        val ANDR_SCHEDULERS = "AndroidSchedulers"

        val SCHE_CALL_IO = "io"
        val SCHE_CALL_NEWTHREAD = "newThread"
        val SCHE_CALL_COMPUTATION = "computation"
        val SCHE_CALL_MAINTHREAD = "mainThread"

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
        MAIN, BACKGROUND, NONE
    }

    inner class Asd : UElementHandler() {
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

                val qualifiedChain: List<UExpression> = quaReferenceExpr.getQualifiedChain()
                if (containsCompose(qualifiedChain)) {
                    val subscribeOnThread: THREAD? = getSubscribeOnThread(qualifiedChain)
                    subscribeOnThread?.let {
                        currentThread = subscribeOnThread
                        var observeOnCalls: List<Triple<Int, String, String>> = RxHelper.getChainExpressionIndex(
                                qualifiedChain,
                                RX_OBSERVEON,
                                listOf(SCHEDULERS),
                                listOf(SCHE_CALL_IO, SCHE_CALL_NEWTHREAD, SCHE_CALL_COMPUTATION))

                        observeOnCalls = observeOnCalls.filter { it.first < composeIndex }
                        val triple = observeOnCalls[observeOnCalls.lastIndex]
                        if (triple.second == SCHEDULERS) {
                            currentThread = subscribeOnThread
                        } else if (triple.second == ANDR_SCHEDULERS) {
                            currentThread = subscribeOnThread
                        }

                        if (currentThread == THREAD.BACKGROUND) {
                            println("compose called on background thread")
                            //report compose called on background thread
                        }
                    }
                }
            }
        }

        private fun getSubscribeOnThread(quaChain: List<UExpression>): THREAD? {
            //todo check full class
            //io.reactivex.schedulers.Schedulers
            //io.reactivex.android.schedulers.AndroidSchedulers
            val subscribeOnCallSite: List<Triple<Int, String, String>> = RxHelper.getChainExpressionIndex(
                    quaChain,
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
            } else {
                //report missing subOnCall
            }
            return null
        }

        private fun containsCompose(quaChain: List<UExpression>): Boolean {
            val composeInChainIndexes: List<Triple<Int, String, String>> = RxHelper.getChainExpressionIndex(quaChain, "compose", listOf("TranHolder"), listOf("asd"))

            val composeListSize = composeInChainIndexes.size
            if (composeListSize == 1) {
                println("compose size 1")
                return true
            } else if (composeListSize > 1) {
                println("compose size > 1")
                //report multiple compose TranHolder asd Calls
            }
            return false
        }
    }

    object RxHelper {

        //return index, innerClass, innerMethod
        fun getChainExpressionIndex(expressionList: List<UExpression>, outerMethodName: String, innerClassName: List<String>, innerMethodNames: List<String>): List<Triple<Int, String, String>> {
            val indexClassMethod: MutableList<Triple<Int, String, String>> = ArrayList()
            expressionList.forEachIndexed { index, uExpression ->
                if (uExpression is UCallExpression) {
                    if (uExpression.methodName == outerMethodName) {
                        val classMethod: Pair<String, String>? = isSearchedCallExpression(uExpression, innerMethodNames, innerClassName)
                        classMethod?.let {
                            indexClassMethod.add(Triple(index, classMethod.first, classMethod.second))
                        }
                    }
                } else {
                    println("Other Type: " + uExpression.toString())
                }
            }
            return indexClassMethod
        }


        //return className , methodName
        private fun isSearchedCallExpression(callExpr: UCallExpression, mthdNames: List<String>, clsNames: List<String>): Pair<String, String>? {
            println("       is Method call: " + callExpr.isMethodCall())
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
                        println("                   got it")
                        return Pair(clsName, mthdName)
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
