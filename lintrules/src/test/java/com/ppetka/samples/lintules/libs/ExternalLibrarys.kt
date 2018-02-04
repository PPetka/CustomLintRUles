package com.ppetka.samples.lintules.libs

import com.android.tools.lint.checks.infrastructure.TestFiles

/**
 * Created by Przemysław Petka on 04-Feb-18.
 */
class ExternalLibrarys {
    companion object {
        fun rxJava2() = TestFiles.bytes("libs/rxjava-2.1.7.jar", javaClass.getResourceAsStream("/rxjava-2.1.7.jar").readBytes())
        fun rxAndroid2() = TestFiles.bytes("libs/rxandroid-2.0.1.jar", javaClass.getResourceAsStream("/rxandroid-2.0.1.jar").readBytes())

    }
}
