package com.ppetka.samples.lintrules

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue
import com.ppetka.samples.lintrules.detector.AndroidClassInPresenterConstructorDetector
import com.ppetka.samples.lintrules.detector.ComposeCallOrderDetector
import com.ppetka.samples.lintrules.detector.FieldAnnotationNotAllowedForSomeTypesDetector
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
        //rx chain issues
        issueList.add(ComposeCallOrderDetector.WRONG_COMPOSE_CALL_ORDER_ISSUE)
        issueList.add(ComposeCallOrderDetector.MULTIPLE_COMPOSE_CALLS_ISSUE)
        issueList.add(ComposeCallOrderDetector.MULTIPLE_SUBSCRIBE_ON_ISSUE)
        issueList.add(ComposeCallOrderDetector.MISSING_SUBSCRIBE_ON_ISSUE)
    }

    override fun getIssues(): List<Issue> {
        return issueList
    }
}