package com.ppetka.samples.lintules

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.ppetka.samples.lintrules.detector.ComposeCallOrderDetector
import com.ppetka.samples.lintules.libs.ExternalLibrarys.Companion.rxAndroid2
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

    val otherTransformCLS = java("""
          |package bar.far;
          |
          |class AnotherTransformerCls{
          |
          |public static <T> SingleTransformer<T, T> fooBar() {
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
        lint().allowCompilationErrors()
                .files(transoferCLS, rxAndroid2(), rxJava2(), java("""
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
          |          thirdMethod();
          |}
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.WRONG_COMPOSE_CALL_ORDER_ISSUE)
                .run()
                .expect("src/com/ppetka/samples/customlintrules/S.java:10: Error: WrongComposeCallOrder [WrongComposeCallOrder]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun bNoObserveOnCall() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxJava2(), java("""
          |package com.ppetka.samples.customlintrules;
          |
          |import io.reactivex.schedulers.Schedulers;
          |import fooo.tran.TranHolder;
          |
          |class S{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .subscribeOn(Schedulers.computation())
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
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.WRONG_COMPOSE_CALL_ORDER_ISSUE)
                .run()
                .expect("src/com/ppetka/samples/customlintrules/S.java:9: Error: WrongComposeCallOrder [WrongComposeCallOrder]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun mNoObserveOnCall() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxJava2(), java("""
          |package com.ppetka.samples.customlintrules;
          |import fooo.tran;
          |
          |class S{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .subscribeOn(AndroidSchedulers.mainThread())
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
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.WRONG_COMPOSE_CALL_ORDER_ISSUE)
                .run()
                .expectClean()
    }

    @Test
    fun mComposeAfterObserveOn() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxJava2(), rxAndroid2(), java("""
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
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.WRONG_COMPOSE_CALL_ORDER_ISSUE)
                .run()
                .expectClean()
    }

    @Test
    fun multipleSubscribeOnCalls() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxJava2(), rxAndroid2(), java("""
          |package com.ppetka.samples.customlintrules;
          |
          |import io.reactivex.schedulers.Schedulers;
          |import io.reactivex.android.schedulers.AndroidSchedulers;
          |import fooo.tran.TranHolder;
          |
          |
          |class S{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .subscribeOn(Schedulers.io())
          |         .observeOn(AndroidSchedulers.mainThread())
          |         .compose(TranHolder.asd())
          |         .subscribeOn(Schedulers.io())
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
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.MULTIPLE_SUBSCRIBE_ON_ISSUE)
                .run()
                .expect("src/com/ppetka/samples/customlintrules/S.java:11: Error: MultipleSubscribeOn [MultipleSubscribeOn]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun multipleSubscribeOnCalls2() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxAndroid2(), rxJava2(), java("""
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
          |         .subscribeOn(Schedulers.io())
          |         .observeOn(AndroidSchedulers.mainThread())
          |         .subscribeOn(AndroidSchedulers.mainThread())
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
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.MULTIPLE_SUBSCRIBE_ON_ISSUE)
                .run()
                .expect("src/com/ppetka/samples/customlintrules/S.java:10: Error: MultipleSubscribeOn [MultipleSubscribeOn]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun multipleSpecificComposeCalls() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxAndroid2(), rxJava2(), java("""
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
          |         .subscribeOn(Schedulers.io())
          |         .compose(TranHolder.asd())
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
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.MULTIPLE_COMPOSE_CALLS_ISSUE)
                .run()
                .expect("src/com/ppetka/samples/customlintrules/S.java:10: Error: MultipleComposeOn [MultipleComposeOn]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun multipleUnrelatedComposeCalls() {
        lint().allowCompilationErrors()
                .files(otherTransformCLS, transoferCLS, rxJava2(), java("""
          |package com.ppetka.samples.customlintrules;
          |import fooo.tran;
          |
          |class S{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .compose(AnotherTransformerCls.fooBar())
          |         .subscribeOn(Schedulers.io())
          |         .observeOn(AndroidSchedulers.mainThread())
          |         .compose(AnotherTransformerCls.fooBar())
          |         .compose(TranHolder.asd())
          |         .compose(AnotherTransformerCls.fooBar())
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
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.MULTIPLE_COMPOSE_CALLS_ISSUE)
                .run()
                .expectClean()
    }

    @Test
    fun missingSubscribeOn() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxAndroid2(), rxJava2(), java("""
          |package com.ppetka.samples.customlintrules;
          |
          |import io.reactivex.android.schedulers.AndroidSchedulers;
          |import fooo.tran.TranHolder;
          |
          |class S{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
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
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.MISSING_SUBSCRIBE_ON_ISSUE)
                .run()
                .expect("src/com/ppetka/samples/customlintrules/S.java:9: Error: MissingSubscribeOn [MissingSubscribeOn]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun bMultipleThreadSwitching() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxAndroid2(), rxJava2(), java("""
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
          |         .subscribeOn(Schedulers.io())
          |         .observeOn(AndroidSchedulers.mainThread())
          |         .observeOn(Schedulers.newThread())
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
          |          thirdMethod();
          |}
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.WRONG_COMPOSE_CALL_ORDER_ISSUE)
                .run()
                .expect("src/com/ppetka/samples/customlintrules/S.java:10: Error: WrongComposeCallOrder [WrongComposeCallOrder]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }
}
