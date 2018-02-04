package com.ppetka.samples.lintules

import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.ppetka.samples.lintrules.detector.FieldAnnotationNotAllowedForSomeTypesDetector
import org.junit.Test

/**
 * Created by Przemysław Petka on 04-Feb-18.
 */

class FieldAnnotationNotAllowedForSomeTypesDetectorTest {
    val superCls = TestFiles.java("""
          |package foo.pckg;
          |
          |public class SuperCls {
          |
          |}""".trimMargin())

    val annotationCls = TestFiles.java("""
          |package foo.anno;
          |
          |
          |public @interface FooAnnotation {
          |
          |}""".trimMargin())

    val allowedfieldCls = TestFiles.java("""
          |package allowed.ann.pckg;
          |
          |public class AACLS {
          |
          |}""".trimMargin())


    @Test
    fun emptyClsExtendsSuper() {
        TestLintTask.lint().allowCompilationErrors()
                .files(superCls, TestFiles.java("""
          |package pckg.name.makes.no.diff;
          |import foo.pckg.SuperCls;
          |
          |class Foo extends SuperCls {
          |
          |}""".trimMargin()))
                .issues(FieldAnnotationNotAllowedForSomeTypesDetector.ISSUE)
                .run()
                .expectClean()
    }

    @Test
    fun allowedFieldAnnotated() {
        TestLintTask.lint().allowCompilationErrors()
                .files(annotationCls, superCls, TestFiles.java("""
          |package pckg.name.makes.no.diff;
          |import foo.pckg.SuperCls;
          |import foo.anno.FooAnnotation;
          |
          |class Foo extends SuperCls {
          |
          |     @FooAnnotation
          |     private int someField;
          |}""".trimMargin()))
                .issues(FieldAnnotationNotAllowedForSomeTypesDetector.ISSUE)
                .run()
                .expect("src/pckg/name/makes/no/diff/Foo.java:7: Error: FooAnnotation annotation not allowed for this type [FooAnnotationNotAllowedForGivenType]\n" +
                        "     @FooAnnotation\n" +
                        "     ~~~~~~~~~~~~~~\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun notAllowedFieldAnnotated() {
        TestLintTask.lint().allowCompilationErrors()
                .files(allowedfieldCls, annotationCls, superCls, TestFiles.java("""
          |package pckg.name.makes.no.diff;
          |import foo.pckg.SuperCls;
          |import foo.anno.FooAnnotation;
          |import allowed.ann.pckg.AACLS;
          |
          |class Foo extends SuperCls {
          |
          |     @FooAnnotation
          |     private AACLS someField;
          |}""".trimMargin()))
                .issues(FieldAnnotationNotAllowedForSomeTypesDetector.ISSUE)
                .run()
                .expectClean()
    }
}

