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
          |package com.ppetka.samples.customlintrules;
          |
          |class SomeCls{
          |
          |public static <T> SingleTransformer<T, T> composeSomething() {
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
          |package modern;
          |
          |import io.reactivex.schedulers.Schedulers;
          |import io.reactivex.android.schedulers.AndroidSchedulers;
          |import com.ppetka.samples.customlintrules.SomeCls;
          |
          |class Shop{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .subscribeOn(Schedulers.newThread())
          |         .compose(SomeCls.composeSomething())
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
                .issues(ComposeCallOrderDetector.WRONG_COMPOSE_CALL_ORDER_ISSUE)
                .run()
                .expect("src/modern/Shop.java:10: Error: WrongComposeCallOrder [WrongComposeCallOrder]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun bNoObserveOnCall() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxJava2(), java("""
          |package oouuuuuuuuuuuuu.d;
          |
          |import io.reactivex.schedulers.Schedulers;
          |import com.ppetka.samples.customlintrules.SomeCls;
          |
          |class Flask{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .subscribeOn(Schedulers.<String>computation())
          |         .compose(SomeCls.composeSomething())
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
                .issues(ComposeCallOrderDetector.WRONG_COMPOSE_CALL_ORDER_ISSUE)
                .run()
                .expect("src/oouuuuuuuuuuuuu/d/Flask.java:9: Error: WrongComposeCallOrder [WrongComposeCallOrder]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun mNoObserveOnCall() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxJava2(), java("""
          |package vvvvvvvvbnbnbn;
          |import com.ppetka.samples.customlintrules;
          |
          |class BurningSpider{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .subscribeOn(AndroidSchedulers.mainThread())
          |         .compose(SomeCls.composeSomething())
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
                .issues(ComposeCallOrderDetector.WRONG_COMPOSE_CALL_ORDER_ISSUE)
                .run()
                .expectClean()
    }

    @Test
    fun mComposeAfterObserveOn() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxJava2(), rxAndroid2(), java("""
          |package composeSomethingdd;
          |
          |import io.reactivex.schedulers.Schedulers;
          |import io.reactivex.android.schedulers.AndroidSchedulers;
          |import com.ppetka.samples.customlintrules.SomeCls;
          |
          |class SSSS{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .subscribeOn(Schedulers.io())
          |         .observeOn(AndroidSchedulers.mainThread())
          |         .compose(SomeCls.composeSomething())
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
                .issues(ComposeCallOrderDetector.WRONG_COMPOSE_CALL_ORDER_ISSUE)
                .run()
                .expectClean()
    }

    @Test
    fun multipleSubscribeOnCalls() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxJava2(), rxAndroid2(), java("""
          |package cdddsadad;
          |
          |import io.reactivex.schedulers.Schedulers;
          |import io.reactivex.android.schedulers.AndroidSchedulers;
          |import com.ppetka.samples.customlintrules.SomeCls;
          |
          |
          |class ScomposeSomething{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .subscribeOn(Schedulers.io())
          |         .observeOn(AndroidSchedulers.mainThread())
          |         .compose(SomeCls.composeSomething())
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
          |}
          |}""".trimMargin()))
                .issues(ComposeCallOrderDetector.MULTIPLE_SUBSCRIBE_ON_ISSUE)
                .run()
                .expect("src/cdddsadad/ScomposeSomething.java:11: Error: subscribeOn(Schedulers.io()) [MultipleSubscribeOn]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun multipleSpecificComposeCalls() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxAndroid2(), rxJava2(), java("""
          |package xxxccccaaa;
          |
          |import io.reactivex.schedulers.Schedulers;
          |import io.reactivex.android.schedulers.AndroidSchedulers;
          |import com.ppetka.samples.customlintrules.SomeCls;
          |
          |class Saaaaaaaa{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .subscribeOn(Schedulers.io())
          |         .compose(SomeCls.composeSomething())
          |         .observeOn(AndroidSchedulers.mainThread())
          |         .compose(SomeCls.composeSomething())
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
                .issues(ComposeCallOrderDetector.MULTIPLE_COMPOSE_CALLS_ISSUE)
                .run()
                .expect("src/xxxccccaaa/Saaaaaaaa.java:10: Error: MultipleComposeOn [MultipleComposeOn]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun multipleUnrelatedComposeCalls() {
        lint().allowCompilationErrors()
                .files(otherTransformCLS, transoferCLS, rxJava2(), java("""
          |package fffffff;
          |import com.ppetka.samples.customlintrules;
          |
          |class Weedjklasjda{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .compose(AnotherTransformerCls.fooBar())
          |         .subscribeOn(Schedulers.io())
          |         .observeOn(AndroidSchedulers.mainThread())
          |         .compose(AnotherTransformerCls.fooBar())
          |         .compose(SomeCls.composeSomething())
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
          |package vvvvvvv;
          |
          |import io.reactivex.android.schedulers.AndroidSchedulers;
          |import com.ppetka.samples.customlintrules.SomeCls;
          |
          |class Sqqw{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .observeOn(AndroidSchedulers.mainThread())
          |         .compose(SomeCls.composeSomething())
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
                .issues(ComposeCallOrderDetector.MISSING_SUBSCRIBE_ON_ISSUE)
                .run()
                .expect("src/vvvvvvv/Sqqw.java:9: Error: MissingSubscribeOn [MissingSubscribeOn]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }

    @Test
    fun bMultipleThreadSwitching() {
        lint().allowCompilationErrors()
                .files(transoferCLS, rxAndroid2(), rxJava2(), java("""
          |package ggggggg;
          |
          |import io.reactivex.schedulers.Schedulers;
          |import io.reactivex.android.schedulers.AndroidSchedulers;
          |import com.ppetka.samples.customlintrules.SomeCls;
          |
          |class TR{
          |
          |public void someMethooooooood() {
          |     Single.just("BOSS")
          |         .subscribeOn(Schedulers.io())
          |         .observeOn(AndroidSchedulers.mainThread())
          |         .observeOn(Schedulers.newThread())
          |         .compose(SomeCls.composeSomething())
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
                .issues(ComposeCallOrderDetector.WRONG_COMPOSE_CALL_ORDER_ISSUE)
                .run()
                .expect("src/ggggggg/TR.java:10: Error: WrongComposeCallOrder [WrongComposeCallOrder]\n" +
                        "     Single.just(\"BOSS\")\n" +
                        "     ^\n" +
                        "1 errors, 0 warnings\n".trimMargin())
    }
}
