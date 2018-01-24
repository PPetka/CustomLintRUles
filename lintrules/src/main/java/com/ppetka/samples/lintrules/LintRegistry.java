package com.ppetka.samples.lintrules;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.ppetka.samples.lintrules.detector.AndroidClassInPresenterConstructorDetector;

import java.util.Collections;
import java.util.List;

/**
 * Created by Przemysław Petka on 1/23/2018.
 *
 * This class represent Lint registry which is responsible for holding every custom lint issue to be tracked
 */

public class LintRegistry extends IssueRegistry {
    private List<Issue> mIssues = Collections.singletonList(
            AndroidClassInPresenterConstructorDetector.ISSUE
    );

    public LintRegistry() {

    }

    @Override
    public List<Issue> getIssues() {
        return mIssues;
    }
}