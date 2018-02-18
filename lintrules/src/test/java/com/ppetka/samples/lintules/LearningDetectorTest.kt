package com.ppetka.samples.lintules

import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.ppetka.samples.lintrules.detector.ComposeCallOrderDetector
import com.ppetka.samples.lintrules.detector.test.LearningDetector
import com.ppetka.samples.lintules.libs.ExternalLibrarys
import org.junit.Test

/**
 * Created by Przemysław Petka on 17-Feb-18.
 */

class LearningDetectorTest {
    val transoferCLSdd = TestFiles.java("""
          |package fooo.comecls;
          |
          |class SomeCls{
          |
          |}""".trimMargin())

    @Test
    fun switchCase() {
        TestLintTask.lint().allowCompilationErrors()
                .files(TestFiles.java("""
          |package com.ppetka.samples.customlintrules;
          |
          |
          |class TestCls{
          |
          |public void firstMethod() {
          |   if(true){
          |    int month = 8;
          |    String monthString;
          |    switch (month) {
          |       case 1: {
          |           monthString = "January";
          |           break;
          |       }
          |       case 2: {
          |
          |             callMethod();
          |
          |
          |           monthString = "February";
          |           break;
          |       }
          |    }
          |   }
          |
          |}
          |
          |public void callMethod(){}
          |
          |
          |
          |""".trimMargin()))
                .issues(LearningDetector.SOME_ISSUE)
                .run()
                .expectClean()
    }

    val transoferCLS = TestFiles.java("""
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
    fun bComposeBeforeObserveOn() {
        TestLintTask.lint().allowCompilationErrors()
                .files(transoferCLS, ExternalLibrarys.rxAndroid2(), ExternalLibrarys.rxJava2(), TestFiles.java("""
          |package com.ppetka.samples.customlintrules;
          |
          |import io.reactivex.schedulers.Schedulers;
          |import io.reactivex.android.schedulers.AndroidSchedulers;
          |import fooo.tran.TranHolder;
          |
          |class S{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .subscribeOn(Schedulers.newThread())
          |         .compose(TranHolder.asd())
          |         .observeOn(AndroidSchedulers.mainThread())
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
          |}
          |}""".trimMargin()))
                .issues(LearningDetector.SOME_ISSUE)
                .run()
                .expectClean()
    }
}
