package com.ppetka.samples.lintules

import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.ppetka.samples.lintrules.detector.AndroidClassInPresenterConstructorDetector
import org.junit.Test

/**
 * Created by Przemysław Petka on 04-Feb-18.
 */

class AndroidClassInPresenterConstructorDetectorTest {

    val presenterClass = TestFiles.java("""
          |package foo.bar;
          |
          |public class Presenter {
          |
          |}""".trimMargin())

    val androidContext = TestFiles.java("""
          |package android.content;
          |
          |public class Context {
          |
          |}""".trimMargin())

    @Test
    fun ctxInConstructor() {
        TestLintTask.lint().allowCompilationErrors()
                .files(androidContext, presenterClass, TestFiles.java("""
          |package pckg.name.makes.no.diff;
          |import foo.bar.Presenter;
          |import android.content.Context;
          |
          |class Foo extends Presenter {
          |
          |     public Presenter(Context ctx){}
          |
          |}""".trimMargin()))
                .issues(AndroidClassInPresenterConstructorDetector.ISSUE)
                .run()
                .expect("src/pckg/name/makes/no/diff/Foo.java:7: Error: Android dependencies not allowed in Presenter classes [NoAndroidClassesAllowedInPresenterIssue]\n" +
                        "     public Presenter(Context ctx){}\n" +
                        "                      ~~~~~~~~~~~\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun ctxInMethod() {
        TestLintTask.lint().allowCompilationErrors()
                .files(androidContext, presenterClass, TestFiles.java("""
          |package pckg.name.makes.no.diff;
          |import foo.bar.Presenter;
          |import android.content.Context;
          |
          |class Foo extends Presenter {
          |
          |     public void someMethod(Context ctx){}
          |
          |     private void anotherMethod(Context ctx){}
          |
          |}""".trimMargin()))
                .issues(AndroidClassInPresenterConstructorDetector.ISSUE)
                .run()
                .expect("src/pckg/name/makes/no/diff/Foo.java:7: Error: Android dependencies not allowed in Presenter classes [NoAndroidClassesAllowedInPresenterIssue]\n" +
                        "     public void someMethod(Context ctx){}\n" +
                        "                            ~~~~~~~~~~~\n" +
                        "src/pckg/name/makes/no/diff/Foo.java:9: Error: Android dependencies not allowed in Presenter classes [NoAndroidClassesAllowedInPresenterIssue]\n" +
                        "     private void anotherMethod(Context ctx){}\n" +
                        "                                ~~~~~~~~~~~\n" +
                        "2 errors, 0 warnings\n")
    }

    @Test
    fun noAndroidDependencies() {
        TestLintTask.lint().allowCompilationErrors()
                .files(presenterClass, TestFiles.java("""
          |package pckg.name.makes.no.diff;
          |import foo.bar.Presenter;
          |
          |class Foo extends Presenter {
          |
          |     public void someMethod(int someInt){}
          |
          |     private void anotherMethod(float someFloat){}
          |
          |}""".trimMargin()))
                .issues(AndroidClassInPresenterConstructorDetector.ISSUE)
                .run()
                .expectClean()
    }

    @Test
    fun superClassWithAndroidDependencies() {
        TestLintTask.lint().allowCompilationErrors()
                .files(androidContext, presenterClass, TestFiles.java("""
          |package foo.bar;
          |import android.content.Context;
          |
          |class Presenter {
          |
          |     public void someMethod(Context ctx){}
          |
          |     private void anotherMethod(float someFloat){}
          |
          |}""".trimMargin()))
                .issues(AndroidClassInPresenterConstructorDetector.ISSUE)
                .run()
                .expectClean()
    }


    @Test
    fun superClassWithoutAndroidDependencies() {
        TestLintTask.lint().allowCompilationErrors()
                .files(presenterClass, TestFiles.java("""
          |package foo.bar;
          |
          |class Presenter {
          |
          |     public void someMethod(int someInt){}
          |
          |     private void anotherMethod(float someFloat){}
          |
          |}""".trimMargin()))
                .issues(AndroidClassInPresenterConstructorDetector.ISSUE)
                .run()
                .expectClean()
    }
}


