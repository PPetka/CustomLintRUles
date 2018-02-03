package com.ppetka.samples.lintules

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.ppetka.samples.lintrules.detector.UnnecessaryFieldDeclarationDetector
import org.junit.Test

class UnnecessaryFieldDeclarationDetectorTest {
    val fooClass = java("""
          |package pckg.namd;
          |
          |class Foo {
          |
          |}""".trimMargin())


    @Test
    fun emptyClassTest() {
        lint().allowCompilationErrors()
                .files(fooClass, java("""
          |package pckg.name.makes.no.diff;
          |class Example {
          |  Foo foo;
          |
          |  public void foo() {
          |
          |  }
          |}""".trimMargin()))
                .issues(UnnecessaryFieldDeclarationDetector.ISSUE)
                .run()
                .expectClean()
    }
}