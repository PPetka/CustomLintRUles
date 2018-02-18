package com.ppetka.samples.lintrules.detector.test

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.*
import java.util.*

//todo check those methods
//  context.evaluator.parameterHasType()
//


/**
 * Created by Przemysław Petka on 17-Feb-18.
 */
class LearningDetector : Detector(), Detector.UastScanner {
    companion object {
        const val SCHEDULERS = "io.reactivex.schedulers.Schedulers"

        val SOME_ISSUE = Issue.create("SOME_ISSUE",
                "SOME_ISSUE",
                "\"SOME_ISSUE\"",
                Category.CORRECTNESS,
                10,
                Severity.ERROR,
                Implementation(LearningDetector::class.java, EnumSet.of<Scope>(Scope.JAVA_FILE)))
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(UExpression::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return UElementHandlerImpl(context)

    }

    inner class UElementHandlerImpl(private val javaContext: JavaContext) : UElementHandler() {

        override fun visitExpressionList(uExpressionList: UExpressionList?) {
            uExpressionList?.expressions?.forEach {
                it.printType()
            }
        }

/*        override fun visitCallExpression(uCallExpression: UCallExpression?) {
            val methodName = uCallExpression?.methodName
            println("uCallExpressionn, $methodName")
            uCallExpression?.printType()

            if (uCallExpression?.gpot(false, UQualifiedReferenceExpression::class.java) != null) {
                println("MATCHES")
            } else {
                println("DOES NOT MATCHES")
            }


            *//*  if (uCallExpression?.getParentOfType(strict = true, firstParentClass = UIfExpression::class.java) != null) {
                  println("   $methodName has strict IfExpression parent")
              }
              if (uCallExpression?.getParentOfType(strict = true, firstParentClass = UMethod::class.java) != null) {
                  println("   $methodName has strict UMethod parent")
              }

              if (uCallExpression?.getParentOfType(strict = true, firstParentClass = USwitchExpression::class.java) != null) {
                  println("   $methodName has strict USwitchExpression parent")
              }
              uCallExpression?.getParen*//*
            println("\n")
        }*/
    }

    //////////////////////////////////////////
    fun <T : UElement> UElement.gpot(parentClass: Class<out UElement>, strict: Boolean = true): T? {
        var element = (if (strict) uastParent else this) ?: return null
        while (true) {
            if (parentClass.isInstance(element)) {
                @Suppress("UNCHECKED_CAST")
                return element as T
            }
            element = element.uastParent ?: return null
        }
    }

    ///////////////////////////////////////////
    fun <T : UElement> UElement.gpot(
            strict: Boolean = true,
            firstParentClass: Class<out T>,
            vararg parentClasses: Class<out T>
    ): T? {
        var element = (if (strict) uastParent else this) ?: return null
        println(element.asRecursiveLogString())
        println("   GPOT, ELEMENT:, ${element.asLogString()} ,STRICT: $strict, FIRST_PARENT_CLASS: $firstParentClass, PARENT_CLASSES, $parentClasses ")
        while (true) {
            if (firstParentClass.isInstance(element)) {
                println("   gpot() first parent, ${element.asLogString()}")
                @Suppress("UNCHECKED_CAST")
                return element as T
            }
            if (parentClasses.any { it.isInstance(element) }) {
                println("   gpot() parentClasses, ${element.asLogString()}")
                @Suppress("UNCHECKED_CAST")
                return element as T
            }
            element = element.uastParent ?: return null
            println("   gpot() last, ${element.asLogString()}")
        }
    }

    //////////////////////////////////////////
    fun <T : UElement> UElement.gpot(
            parentClass: Class<out UElement>,
            strict: Boolean = true,
            vararg terminators: Class<out UElement>
    ): T? {
        var element = (if (strict) uastParent else this) ?: return null
        println(element.asRecursiveLogString())
        println("   GPOT, ELEMENT:, ${element.asLogString()} ,STRICT: $strict, FIRST_PARENT_CLASS: $parentClass, PARENT_CLASSES, $terminators ")
        while (true) {
            if (parentClass.isInstance(element)) {
                println("   gpot() parentClass, ${element.asLogString()}")
                @Suppress("UNCHECKED_CAST")
                return element as T
            }
            if (terminators.any { it.isInstance(element) }) {
                println("gpot() terminator, ${element.asLogString()}")
                return null
            }
            element = element.uastParent ?: return null
            println("   gpot() last, ${element.asLogString()}")
        }
    }

    fun UExpression.printType() {
        when (this) {
            is UCallableReferenceExpression -> {
                println("uCallExpression is UCallableReferenceExpression")
            }
            is UNamedExpression -> {
                println("uCallExpression is UNamedExpression")
            }
            is UClassLiteralExpression -> {
                println("uCallExpression is UClassLiteralExpression")
            }
            is UDeclarationsExpression -> {
                println("uCallExpression is UDeclarationsExpression")
            }
            is USwitchExpression -> {
                println("uCallExpression is USwitchExpression")
            }
            is UIfExpression -> {
                println("uCallExpression is UIfExpression")
            }
            is UBlockExpression -> {
                println("uCallExpression is UBlockExpression")
            }
            is UThisExpression -> {
                println("uCallExpression is UThisExpression")
            }
            is UBinaryExpression -> {
                println("uCallExpression is UBinaryExpression")
            }
            is UBreakExpression -> {
                println("uCallExpression is UBreakExpression")
            }
            is UContinueExpression -> {
                println("uCallExpression is UContinueExpression")
            }
            is UForExpression -> {
                println("uCallExpression is UForExpression")
            }
            is UInstanceExpression -> {
                println("uCallExpression is UInstanceExpression")
            }
            is UJumpExpression -> {
                println("uCallExpression is UJumpExpression")
            }
            is ULabeledExpression -> {
                println("uCallExpression is ULabeledExpression")
            }
            is USimpleNameReferenceExpression -> {
                println("uCallExpression is USimpleNameReferenceExpression")
            }
            is USwitchClauseExpressionWithBody -> {
                println("uCallExpression is USwitchClauseExpressionWithBody")
            }
            is UTypeReferenceExpression -> {
                println("uCallExpression is UTypeReferenceExpression")
            }
            is USwitchClauseExpression -> {
                println("uCallExpression is USwitchClauseExpression")
            }
            is UObjectLiteralExpression -> {
                println("uCallExpression is UObjectLiteralExpression")
            }
            is UForEachExpression -> {
                println("uCallExpression is UForEachExpression")
            }
            is UDoWhileExpression -> {
                println("uCallExpression is UDoWhileExpression")
            }
            is UExpressionList -> {
                println("uCallExpression is UExpressionList")
            }
            is UArrayAccessExpression -> {
                println("uCallExpression is UArrayAccessExpression")
            }
            is UQualifiedReferenceExpression -> {
                println("uCallExpression is UQualifiedReferenceExpression")
            }
            is UWhileExpression -> {
                println("uCallExpression is UWhileExpression")
            }
            is UUnaryExpression -> {
                println("uCallExpression is UUnaryExpression")
            }
            is UTryExpression -> {
                println("uCallExpression is UTryExpression")
            }
            is UThrowExpression -> {
                println("uCallExpression is UThrowExpression")
            }
            is UReturnExpression -> {
                println("uCallExpression is UReturnExpression")
            }
            is UReferenceExpression -> {
                println("uCallExpression is UReferenceExpression")
            }
            is UPostfixExpression -> {
                println("uCallExpression is UPostfixExpression")
            }
            is UPrefixExpression -> {
                println("uCallExpression is UPrefixExpression")
            }
            is ULambdaExpression -> {
                println("uCallExpression is ULambdaExpression")
            }
            is UCallExpression -> {
                println("uCallExpression is UCallExpression")
            }
            else -> {
                println("uCallExpression is UNKNOWN")
            }
        }
    }
}