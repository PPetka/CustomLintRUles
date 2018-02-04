package com.ppetka.samples.lintrules

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue
import com.ppetka.samples.lintrules.detector.AndroidClassInPresenterConstructorDetector
import com.ppetka.samples.lintrules.detector.FieldAnnotationNotAllowedForSomeTypesDetector
import com.ppetka.samples.lintrules.detector.SimpleDetector
import com.ppetka.samples.lintrules.detector.UnnecessaryFieldDeclarationDetector


import java.util.ArrayList

/**
 * Created by Przemysław Petka on 1/23/2018.
 *
 *
 * This class represent Lint registry which is responsible for holding every custom lint issue to be tracked
 */

class LintRegistry : IssueRegistry() {
    private val issueList: MutableList<Issue>

    init {
        issueList = ArrayList()
        issueList.add(AndroidClassInPresenterConstructorDetector.ISSUE)
        issueList.add(FieldAnnotationNotAllowedForSomeTypesDetector.ISSUE)
        issueList.add(UnnecessaryFieldDeclarationDetector.ISSUE)
        issueList.add(SimpleDetector.ISSUE)
    }

    override fun getIssues(): List<Issue> {
        return issueList
    }
}