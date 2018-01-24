package com.ppetka.samples.lintrules.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Detector.UastScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Severity;

import static com.android.tools.lint.detector.api.Scope.JAVA_FILE;

import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by Przemysław Petka on 1/21/2018.
 *
 */

public class AndroidClassInPresenterConstructorDetector extends Detector implements UastScanner {
    /*ISSUE*/
    public static final Issue ISSUE = Issue.create("NoAndroidClassesAllowedInPresenterIssue",
            "No android classes allowed in Presenter constructor",
            "\"Network module should not be depended on android classes for testing reasons\"",
            Category.CORRECTNESS,
            10,
            Severity.ERROR,
            new Implementation(AndroidClassInPresenterConstructorDetector.class, EnumSet.of(JAVA_FILE)));

    /*IMPLEMENTATION*/
    @Override
    public List<Class<? extends UElement>> getApplicableUastTypes() {
        return Collections.<Class<? extends UElement>>singletonList(UClass.class);
    }

    @Override
    public UElementHandler createUastHandler(final JavaContext context) {

        return new UElementHandler() {
            @Override
            public void visitClass(UClass uClass) {
            /*    context.report(ISSUE, uClass, context.getLocation(uClass.getPsi()),
                        "Report every encountered class: **!!!!!!**");*/
            }
        };
    }
}