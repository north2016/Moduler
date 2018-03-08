package com.easy.moduler.apt.processor;


import com.easy.moduler.annotation.RouterRule;
import com.easy.moduler.apt.AnnotationProcessor;
import com.easy.moduler.apt.inter.IProcessor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

import static com.easy.moduler.apt.AnnotationProcessor.KEY_MODULE_NAME;

/**
 * routerRule注解处理器
 * Created by evan on 2017/6/7.
 */
public class RouterRuleProcessor implements IProcessor {

    @Override
    public void process(RoundEnvironment roundEnv, AnnotationProcessor mAbstractProcessor) {
        try {
            ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ParameterizedTypeName.get(
                            ClassName.get(Class.class),
                            WildcardTypeName.subtypeOf(ClassName.get("android.app", "Activity"))
                    )
            );
            ParameterSpec parameterSpec = ParameterSpec.builder(parameterizedTypeName, "rules")
                    .build();

            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("initRule")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(parameterSpec);

            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(RouterRule.class);
            if (elements == null || elements.size() == 0) {
                return;
            }
            for (Element element : elements) {
                if (element == null || element.getKind() != ElementKind.CLASS) {
                    error(mAbstractProcessor.mMessager, "Only Classes can be annotated with " + RouterRule.class.getCanonicalName());
                    return;
                }

                RouterRule routerRule = element.getAnnotation(RouterRule.class);
                String[] value = routerRule.value();
                if (value != null) {
                    for (String url : value) {
                        methodSpecBuilder.addStatement("rules.put($S, $T.class)", url, element);
                    }
                }
            }

            MethodSpec methodSpec = methodSpecBuilder.build();

            String moduleName = mAbstractProcessor.mProcessingEnv.getOptions().get(KEY_MODULE_NAME);
            TypeSpec typeSpec = TypeSpec.classBuilder(moduleName + "_AutoRouterRuleCreator")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(AnnotationSpec.builder(AutoService.class)
                            .addMember("value", "$T.class", mAbstractProcessor.mElements.getTypeElement("com.easy.moduler.lib.router.IRouterRulesCreator"))
                            .build())
                    .addSuperinterface(ClassName.get(mAbstractProcessor.mElements.getTypeElement("com.easy.moduler.lib.router.IRouterRulesCreator")))
                    .addMethod(methodSpec)
                    .build();

            JavaFile.builder(getClass().getPackage().getName(), typeSpec).build().writeTo(mAbstractProcessor.mFiler);

            // elements.clear();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            error(mAbstractProcessor.mMessager, e.getMessage());
        }
    }

    private void error(Messager messager, String error) {
        messager.printMessage(Diagnostic.Kind.ERROR, this.getClass().getCanonicalName() + " : " + error);
    }

}
