package com.ppetka.samples.lintrules.detector;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;

import org.jetbrains.uast.UAnnotation;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UField;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static com.android.tools.lint.detector.api.Scope.JAVA_FILE;

/**
 * Created by Przemysław Petka on 1/24/2018.
 */

public class FieldAnnotationNotAllowedForSomeTypesDetector extends Detector implements Detector.UastScanner {
    private static final String ANNOTATION_PCKG = "com.ppetka.samples.lintrules.Send";
    private static final String ALLOWED_ANNOTATING_CLASS_PCKG = "java.lang.Float";

    private static final List<String> WANTED_CLASS_PKCG_NAMES = Arrays.asList(
            "com.ppetka.samples.lintrules.detector.FieldAnnotationNotAllowedForSomeTypesDetector",
            "com.ppetka.samples.customlintrules.MainActivity");

    public static final Issue ISSUE = Issue.create("SendAnnotationNotAllowedForGivenType",
            "**@Send** annotation not allowed for this type",
            "Current class does not support multiple **@Send** annotations for sending each object individually, Please add this object to **Package** object",
            Category.CORRECTNESS,
            8,
            Severity.ERROR,
            new Implementation(FieldAnnotationNotAllowedForSomeTypesDetector.class, EnumSet.of(JAVA_FILE)));

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
                if (WANTED_CLASS_PKCG_NAMES.contains(uClass.getQualifiedName())) {
                    UField[] fields = uClass.getFields();
                    for (UField field : fields) {
                        List<UAnnotation> annotations = field.getAnnotations();
                        for (UAnnotation annotation : annotations) {
                            if (ANNOTATION_PCKG.equals(annotation.getQualifiedName())) {
                                if (fieldIsInvalidClass(field)) {
                                    javaContext.report(ISSUE, uClass, javaContext.getLocation(annotation), ISSUE.getBriefDescription(TextFormat.TEXT));
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private boolean fieldIsInvalidClass(UField field) {
        PsiType fieldType = field.getType();
        if (fieldType instanceof PsiClassType) {
            PsiClass fieldClass = ((PsiClassType) fieldType).resolve();
            if (fieldClass != null) {
                if (!ALLOWED_ANNOTATING_CLASS_PCKG.equals(fieldClass.getQualifiedName())) {
                    return true;
                }
            }
        }
        return false;
    }
}