package com.ppetka.samples.lintrules;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.ppetka.samples.lintrules.detector.AndroidClassInPresenterConstructorDetector;
import com.ppetka.samples.lintrules.detector.FieldAnnotationNotAllowedForSomeTypesDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Przemysław Petka on 1/23/2018.
 * <p>
 * This class represent Lint registry which is responsible for holding every custom lint issue to be tracked
 */

public class LintRegistry extends IssueRegistry {
    private List<Issue> issueList;

    public LintRegistry() {
        issueList = new ArrayList<>();
        issueList.add(AndroidClassInPresenterConstructorDetector.ISSUE);
        issueList.add(FieldAnnotationNotAllowedForSomeTypesDetector.ISSUE);
    }

    @Override
    public List<Issue> getIssues() {
        return issueList;
    }
}