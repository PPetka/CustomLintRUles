package com.ppetka.samples.lintrules.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Detector.UastScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;

import static com.android.tools.lint.detector.api.Scope.JAVA_FILE;

import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UParameter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by Przemysław Petka on 1/21/2018.
 */

public class AndroidClassInPresenterConstructorDetector extends Detector implements UastScanner {
    private static final String PRESENTER_PACKAGE_NAME = "com.ppetka.samples.customlintrules.Presenter";
    private static final String ANDROID_STARTING_PACKAGE_NAME = "android.";

    public static final Issue ISSUE = Issue.create("NoAndroidClassesAllowedInPresenterIssue",
            "Android dependencies not allowed in Presenter classes",
            "\"Network module should not be depended on android classes, and holds android logic, please satisfy presenter with necessary dependencies from outside\"",
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
    public UElementHandler createUastHandler(final JavaContext javaContext) {

        return new UElementHandler() {
            @Override
            public void visitClass(UClass uClass) {
                //visit only Presenter classes
                if (PRESENTER_PACKAGE_NAME.equals(uClass.getQualifiedName())) {
                    UMethod[] methods = uClass.getMethods();
                    //check only for constructors
                    for (UMethod method : methods) {
                        if (method.isConstructor()) {
                            //check constructor arguments for android classes
                            List<UParameter> uastParameters = method.getUastParameters();
                            for (UParameter uastParameter : uastParameters) {
                                String argType = uastParameter.getType().getCanonicalText();
                                if (argType.startsWith(ANDROID_STARTING_PACKAGE_NAME)) {
                                    javaContext.report(ISSUE, uClass, javaContext.getLocation(uastParameter.getPsi()), ISSUE.getBriefDescription(TextFormat.TEXT));
                                }
                            }
                        }
                    }
                }
            }
        };
    }
}