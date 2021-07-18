package moe.ore;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.*;

@AutoService(Processor.class)  //自动注册
@SupportedSourceVersion(SourceVersion.RELEASE_8) //指定java版本
public class TarsProcessor extends AbstractProcessor {


    private Filer filer;
    private Types typeUtils;
    Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnv.getFiler();
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Tars.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    //  isSubtype types * tips：https://stackoverflow.com/questions/12749517/types-isassignable-and-issubtype-misunderstanding
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> tarsClassElement = roundEnvironment.getElementsAnnotatedWith(Tars.class);
        Set<? extends Element> tarsFieIdElement = roundEnvironment.getElementsAnnotatedWith(TarsId.class);
        HashMap<Element, Set<Element>> initializeMapping = new HashMap<>();
        for (Element tarsClass : tarsClassElement) {
            initializeMapping.put(tarsClass, new HashSet<>());
        }
        for (Element tarsFieId : tarsFieIdElement) {
            initializeMapping.get(tarsFieId.getEnclosingElement()).add(tarsFieId);
        }

        for (Map.Entry<Element, Set<Element>> tarsClass : initializeMapping.entrySet()) {
            MethodSpec.Builder writeToBuilder = MethodSpec.methodBuilder("writeTo")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(ClassName.get("moe.ore.tars", "TarsOutputStream"), "output")
                    .addStatement("super.writeTo($N)", "output");

            MethodSpec.Builder readFromBuilder = MethodSpec.methodBuilder("readFrom")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(ClassName.get("moe.ore.tars", "TarsInputStream"), "input")
                    .addStatement("super.readFrom($N)", "input");

            for (Element tarsFieId : tarsClass.getValue()) {
                TarsId annotation = tarsFieId.getAnnotation(TarsId.class);
                boolean isObject = typeUtils.isSubtype(tarsFieId.asType(), elementUtils.getTypeElement("java.lang.Object").asType());
                if (isObject) {
                    writeToBuilder.beginControlFlow("if ($L != null)", tarsFieId.getSimpleName().toString());
                    writeToBuilder.addStatement("output.write($L, $L)", tarsFieId.getSimpleName().toString(), annotation.tag());
                    writeToBuilder.endControlFlow();
                } else {
                    writeToBuilder.addStatement("output.write($L, $L)", tarsFieId.getSimpleName().toString(), annotation.tag());
                }

                // TODO: 2021/7/18 因为读取要传入字段类型 如果在 kt 里面的话 lateinit 修饰符的字段会报错
                // TODO: 2021/7/18 在 kt 里面 字段默认修饰符 是 private 的 需要添加 @JvmField 才能在生成的类去访问 否则需要去调用他生成的 get set 方法。 如果调用 get 方法会导致不能在 kt check 代码之前判断字段是否为 null 然后报错 最佳解决办法就是 让 @TarsId 注解去'继承' @JvmField 来实现一个注解两个效果 但是似乎没成功
//                boolean isObject = typeUtils.isSubtype(element1.asType(), elementUtils.getTypeElement("java.lang.Object").asType());
//                if (isObject) {
//                    readFromBuilder.beginControlFlow("if($L!=null)", element1.getSimpleName().toString());
//                    readFromBuilder.addStatement("input.read($L,$L,$L)", element1.getSimpleName().toString(), annotation.value(), false);
//                    readFromBuilder.endControlFlow();
//                } else {
                readFromBuilder.addStatement("$L = ($T) input.read($L, $L, $L)", tarsFieId.getSimpleName().toString(), tarsFieId.asType(), tarsFieId.getSimpleName().toString(), annotation.tag(), annotation.require());
//                }
            }
            MethodSpec writeToMethod = writeToBuilder.build();
            MethodSpec readFromMethod = readFromBuilder.build();

            // tarsWrapper class
            TypeSpec tarsWrapper = TypeSpec.classBuilder(tarsClass.getKey().getSimpleName().toString() + "Tars")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(writeToMethod)
                    .addMethod(readFromMethod)
                    .superclass(tarsClass.getKey().asType())
                    .build();
            try {
                // build com.example.Class.java
                JavaFile javaFile = JavaFile.builder(tarsClass.getKey().getEnclosingElement().getSimpleName().toString(), tarsWrapper)
                        .addFileComment("This codes are generated automatically from Ore. Do not modify!")
                        .indent("    ")
                        .build();
                // write to file
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

}

