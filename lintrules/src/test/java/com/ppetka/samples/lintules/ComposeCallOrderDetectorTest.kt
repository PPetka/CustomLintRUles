package com.ppetka.samples.lintules

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.ppetka.samples.lintrules.detector.ComposeCallOrderDetector
import com.ppetka.samples.lintules.libs.ExternalLibrarys.Companion.rxJava2
import org.junit.Test

/**
 * Created by Przemysław Petka on 04-Feb-18.
 */

class ComposeCallOrderDetectorTest {

    @Test
    fun callingCompositeDisposableAddAll() {
        lint().allowCompilationErrors()
                .files(rxJava2(), java("""
          |package foo;
          |
          |import io.reactivex.Observable;
          |
          |class Example extends Observable{
          |     @Override
          |     protected void subscribeActual(Observer observer) {
          |
          |     }
          |
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.ISSUE)
                .run()
                .expectClean()
    }
}
