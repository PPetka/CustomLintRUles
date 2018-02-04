package com.ppetka.samples.lintules

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.ppetka.samples.lintrules.detector.UnnecessaryFieldDeclarationDetector
import org.junit.Test

class UnnecessaryFieldDeclarationDetectorTest {
    val superCls = java("""
          |package foo.pckg;
          |
          |class SuperCls {
          |
          |}""".trimMargin())

    val fieldCls = java("""
          |package bar.pckgg;
          |
          |class FieldCls {
          |
          |}""".trimMargin())


    @Test
    fun emptyClsExtendsSuper() {
        lint().allowCompilationErrors()
                .files(superCls, java("""
          |package pckg.name.makes.no.diff;
          |import foo.pckg.SuperCls;
          |
          |class Foo extends SuperCls {
          |
          |}""".trimMargin()))
                .issues(UnnecessaryFieldDeclarationDetector.ISSUE)
                .run()
                .expectClean()
    }

    @Test
    fun emptySuperCls() {
        lint().allowCompilationErrors()
                .files(java("""
          |package foo.pckg;
          |
          |class SuperCls {
          |
          |}""".trimMargin()))
                .issues(UnnecessaryFieldDeclarationDetector.ISSUE)
                .run()
                .expectClean()
    }

    @Test
    fun superClassWithNotAllowedField() {
        lint().allowCompilationErrors()
                .files(java("""
          |package foo.pckg;
          |import bar.pckgg.FieldCls;
          |
          |class SuperCls {
          |     private FieldCls cls;
          |
          |}""".trimMargin()))
                .issues(UnnecessaryFieldDeclarationDetector.ISSUE)
                .run()
                .expectClean()
    }

    @Test
    fun classWithNotAllowedField() {
        lint().allowCompilationErrors()
                .files(superCls, fieldCls, java("""
          |package pckg.name.makes.no.diff;
          |import foo.pckg.SuperCls;
          |import bar.pckgg.FieldCls;
          |
          |class Foo extends SuperCls {
          |     private FieldCls cls;
          |
          |}""".trimMargin()))
                .issues(UnnecessaryFieldDeclarationDetector.ISSUE)
                .run()
                .expect("src/pckg/name/makes/no/diff/Foo.java:6: Error: This field is re-declared [FieldRedeclaration]\n" +
                        "     private FieldCls cls;\n" +
                        "     ~~~~~~~~~~~~~~~~~~~~~\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }
}