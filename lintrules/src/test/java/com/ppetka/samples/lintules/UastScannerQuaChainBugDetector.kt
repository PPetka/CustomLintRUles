package com.ppetka.samples.lintules

import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.ppetka.samples.lintrules.detector.bugs.UastScannerQuaChainBugDetector
import org.junit.Test

/**
 * Created by Przemysław Petka on 3/12/2018.
 */

class UastScannerQuaChainBugDetector {
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
          |}""".trimMargin())

    @Test
    fun quaChainTest() {
        TestLintTask.lint().allowCompilationErrors()
                .files(motherCls, firstCls, secondCls, TestFiles.java("""
          |package com.ppetka.samples.customlintrules;
          |
          |class TestCls {
          |
          |    public TestClss firstFunc(FirstCls firstCls) {
          |        return this;
          |    }
          |
          |    public TestClss secondFunc(SecondCls secondCls) {
          |        return this;
          |    }
          |
          |    public TestClss thirdFunc() {
          |        return this;
          |    }
          |
          |
          |    public TestClss overloadedFunc(FirstCls firstCls) {
          |        return this;
          |    }
          |
          |    public TestClss overloadedFunc(SecondCls secondCls) {
          |        return this;
          |    }
          |
          |
          |    public void foo() {
          |        FirstCls f = new FirstCls();
          |        SecondCls s = new SecondCls();
          |
          |        firstFunc(f)
          |             .secondFunc(s)
          |             .thirdFunc()
          |             .overloadedFunc(f);
          |    }
          |
          |
          |}""".trimMargin()))
                .issues(UastScannerQuaChainBugDetector.SOME_ISSUE)
                .run()
                .expect("src/com/ppetka/samples/customlintrules/TestCls.java:31: Error: firstFunc [SOME_ISSUE]\n" +
                        "        firstFunc(f)\n" +
                        "        ~~~~~~~~~~~~\n" +
                        "src/com/ppetka/samples/customlintrules/TestCls.java:31: Error: firstFunc [SOME_ISSUE]\n" +
                        "        firstFunc(f)\n" +
                        "        ^\n" +
                        "src/com/ppetka/samples/customlintrules/TestCls.java:31: Error: firstFunc [SOME_ISSUE]\n" +
                        "        firstFunc(f)\n" +
                        "        ^\n" +
                        "src/com/ppetka/samples/customlintrules/TestCls.java:31: Error: firstFunc [SOME_ISSUE]\n" +
                        "        firstFunc(f)\n" +
                        "        ^\n" +
                        "4 errors, 0 warnings\n")
    }
}
