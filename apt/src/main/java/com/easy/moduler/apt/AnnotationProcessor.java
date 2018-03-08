package com.easy.moduler.apt;

import com.easy.moduler.annotation.RouterRule;
import com.easy.moduler.apt.processor.RouterRuleProcessor;
import com.google.auto.service.AutoService;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static com.easy.moduler.apt.AnnotationProcessor.KEY_MODULE_NAME;

@AutoService(Processor.class)//自动生成 javax.annotation.processing.IProcessor 文件
@SupportedSourceVersion(SourceVersion.RELEASE_8)//java版本支持
@SupportedOptions(KEY_MODULE_NAME)//支持的配置参数
public class AnnotationProcessor extends AbstractProcessor {
    public static final String KEY_MODULE_NAME = "moduleName";
    public Filer mFiler; //文件相关的辅助类
    public Elements mElements; //元素相关的辅助类
    public Messager mMessager; //日志相关的辅助类
    public ProcessingEnvironment mProcessingEnv;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mMessager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
        mProcessingEnv = processingEnv;
        new RouterRuleProcessor().process(roundEnv, this);
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(RouterRule.class.getCanonicalName());
        return types;
    }
}
