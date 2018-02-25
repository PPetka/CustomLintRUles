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
    val motherCls = TestFiles.java("""
          |package com.ppetka.samples.customlintrules;
          |
          |class MotherCls{
          |
          |}""".trimMargin())

    val firstCls = TestFiles.java("""
          |package com.ppetka.samples.customlintrules;
          |
          |class FirstCls extends MotherCls {
          |
          |}""".trimMargin())

    val secondCls = TestFiles.java("""
          |package com.ppetka.samples.customlintrules;
          |
          |class SecondCls extends MotherCls {
          |
          |     public static void mmmmmmmmmmmm(Integer innn){
          |
          |     }
          |
          |}""".trimMargin())


    @Test
    fun bComposeBeforeObserveOn() {
        TestLintTask.lint().allowCompilationErrors()
                .files(motherCls, firstCls, secondCls, TestFiles.java("""
          |package com.ppetka.samples.customlintrules;
          |
          |class TestCls {
          |
          |    public TestClss aaaaaaaaaaa() {
          |        return this;
          |    }
          |
          |    public TestClss bbbbbbbbbbb() {
          |        return this;
          |    }
          |
          |    public TestClss ccccccccccc() {
          |        return this;
          |    }
          |
          |    public TestClss ddddddddddd() {
          |        return this;
          |    }
          |
          |    public TestClss func(FirstCls firstCls) {
          |        return this;
          |    }
          |
          |    public TestClss func(SecondCls secondCls) {
          |        return this;
          |    }
          |          |
          |    public TestClss func() {
          |        return this;
          |    }
          |
          |
          |    public void testingFunc() {
          |        FirstCls firstCls = new FirstCls();
          |
          |       SecondCls.mmmmmmmmmmmm(5);
          |
          |     bbbbbbbbbbb()
          |                .aaaaaaaaaaa()
          |                .ccccccccccc()
          |                .ddddddddddd()
          |                .func(firstCls);
          |    }
          |
          |
          |}""".trimMargin()))
                .issues(LearningDetector.SOME_ISSUE)
                .run()
                .expectClean()
    }
}
