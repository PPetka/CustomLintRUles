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

    val transoferCLS = java("""
          |package fooo.tran;
          |
          |class TranHolder{
          |
          |public static <T> SingleTransformer<T, T> asd() {
          |      Integer innnn = new Interger(4);
          |      SingleTransformer s = new SingleTransformer() {
          |            @Override
          |            public SingleSource apply(Single upstream) {
          |                 return upstream;
          |            }
          |      };
          |      return s;
          |}
          |}""".trimMargin())

    @Test
    fun another() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxJava2(), java("""
          |package com.ppetka.samples.customlintrules;
          |import fooo.tran;
          |
          |class S{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .subscribeOn(Schedulers.io())
          |         .observeOn(AndroidSchedulers.mainThread())
          |         .compose(TranHolder.asd())
          |         .subscribe(new SingleObserver<String>() {
          |             @Override
          |             public void onSubscribe(Disposable d) {}
          |
          |             @Override
          |             public void onSuccess(String s) {}
          |
          |             @Override
          |             public void onError(Throwable e) {}
          |          });
          |          thirdMethod();
          |}
          |
          |private void thirdMethod(){
          |
          | AtomicInteger a = new AtomicInteger(4);
          |}
          |
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.ISSUE)
                .run()
                .expectClean()
    }
}
