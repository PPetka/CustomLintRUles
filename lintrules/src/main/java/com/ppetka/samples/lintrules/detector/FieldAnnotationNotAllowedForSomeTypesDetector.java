package com.ppetka.samples.lintrules.detector;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Severity;
import java.util.EnumSet;

import static com.android.tools.lint.detector.api.Scope.JAVA_FILE;

/**
 * Created by Przemysław Petka on 1/24/2018.
 */

public class FieldAnnotationNotAllowedForSomeTypesDetector extends Detector implements Detector.UastScanner {
    public static final Issue ISSUE = Issue.create("SendAnnotationNotAllowedForGivenType",
            "**@Send** annotation not allowed for this type",
            "Current class does not support multiple **@Send** annotations for sending each object individually, Please add this object to **Package** object",
            Category.CORRECTNESS,
            8,
            Severity.ERROR,
            new Implementation(FieldAnnotationNotAllowedForSomeTypesDetector.class, EnumSet.of(JAVA_FILE)));

    /*IMPLEMENTATION*/

}