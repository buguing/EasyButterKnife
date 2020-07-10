package com.wellee.butterknife.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wellee.butterknife.annotation.BindView;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)
public class ButterKnifeProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class);
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> bindViewElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);

        // 解析 Element
        Map<Element, List<Element>> analysisElementMap = new LinkedHashMap<>();
        for (Element bindViewElement : bindViewElements) {
            Element enclosingElement = bindViewElement.getEnclosingElement();

            List<Element> elements = analysisElementMap.get(enclosingElement);
            if (elements == null) {
                elements = new ArrayList<>();
                analysisElementMap.put(enclosingElement, elements);
            }

            elements.add(bindViewElement);
        }

        // 生成 java 类
        for (Map.Entry<Element, List<Element>> entry : analysisElementMap.entrySet()) {
            Element enclosingElement = entry.getKey();
            List<Element> elements = entry.getValue();

            String classNameStr = enclosingElement.getSimpleName().toString();

            ClassName unbinderClassName = ClassName.get("com.wellee.butterknife", "Unbinder");
            // 组装类:  xxx_ViewBinding implements Unbinder
            TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(classNameStr + "_ViewBinding")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addSuperinterface(unbinderClassName);

            ClassName callSuperClassName = ClassName.get("androidx.annotation", "CallSuper");
            // 组装unbind 方法
            MethodSpec.Builder unbindMethodBuilder = MethodSpec.methodBuilder("unbind")
                    .addAnnotation(Override.class)
                    .addAnnotation(callSuperClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID);

            ClassName uiThreadClassName = ClassName.get("androidx.annotation", "UiThread");
            ClassName parameterClassName = ClassName.bestGuess(classNameStr);
            // 组装构造函数: public xxx_ViewBinding(xxx target)
            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                    .addAnnotation(uiThreadClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(parameterClassName, "target");

            // 添加 target.textView1 = Utils.findViewById(target,R.id.tv1);
            for (Element bindViewElement : elements) {
                String filedName = bindViewElement.getSimpleName().toString();
                ClassName utilClassName = ClassName.get("com.wellee.butterknife", "Utils");
                int resId = bindViewElement.getAnnotation(BindView.class).value();
                constructorBuilder.addStatement("target.$L = $T.findViewById(target,$L)",
                        filedName, utilClassName, resId);
            }

            typeSpecBuilder.addMethod(constructorBuilder.build());
            typeSpecBuilder.addMethod(unbindMethodBuilder.build());

            try {
                // 写入生成 java 类
                String packageName = mElementUtils.getPackageOf(enclosingElement).getQualifiedName().toString();
                JavaFile.builder(packageName, typeSpecBuilder.build())
                        .addFileComment("ButterKnife自动生成")
                        .build().writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("翻车了");
            }
        }

        return false;
    }
}
