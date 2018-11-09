package com.radutopor.viewmodelfactory.processor;

import com.google.auto.service.AutoService;
import com.radutopor.viewmodelfactory.annotations.Provided;
import com.radutopor.viewmodelfactory.annotations.ViewModelFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ViewModelFactoryProcessor extends AbstractProcessor {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {{
            add(ViewModelFactory.class.getCanonicalName());
        }};
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(ViewModelFactory.class)) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                createViewModelFactory2((ExecutableElement) element);
            } else if (element.getKind() == ElementKind.CLASS) {
                List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());
                if (constructors.size() > 1) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Multiple constructors - Annotate one with `ViewModelFactory` instead of class");
                    return true;
                } else {
                    createViewModelFactory2(constructors.get(0));
                }
            } else {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "`ViewModelFactory` annotation can only be applied to classes or constructors");
                return true;
            }
        }
        return false;
    }

    private void createViewModelFactory2(ExecutableElement vmConstructor) {
        TypeElement viewModel = (TypeElement) vmConstructor.getEnclosingElement();
        String packageName = processingEnv.getElementUtils().getPackageOf(viewModel).getQualifiedName().toString();
        String vmName = viewModel.getSimpleName().toString();

        StringBuilder factoryMembers = new StringBuilder();
        StringBuilder factoryConstrParams = new StringBuilder();
        StringBuilder factoryConstrStatements = new StringBuilder();
        StringBuilder vmCreateArgs = new StringBuilder();
        StringBuilder factory2Members = new StringBuilder();
        StringBuilder factory2ConstrParams = new StringBuilder();
        StringBuilder factory2ConstrStatements = new StringBuilder();
        StringBuilder factoryCreateParams = new StringBuilder();
        StringBuilder factoryCreateArgs = new StringBuilder();

        for (VmConstrParam vmConstrParam : getVmConstrParams(vmConstructor)) {
            append(factoryMembers, "private final %s;\n", vmConstrParam.getTypeAndName());
            append(factoryConstrParams, ", %s", vmConstrParam.getTypeAndName());
            append(factoryConstrStatements, "this.%1$s = %1$s;\n", vmConstrParam.getName());
            append(vmCreateArgs, ", %s", vmConstrParam.getName());
            if (vmConstrParam.isProvided()) {
                append(factory2Members, "private final Provider<%s> %sProvider;\n", vmConstrParam.getType(), vmConstrParam.getName());
                append(factory2ConstrParams, ", Provider<%s> %sProvider", vmConstrParam.getType(), vmConstrParam.getName());
                append(factory2ConstrStatements, "this.%1$sProvider = %1$sProvider;\n", vmConstrParam.getName());
                append(factoryCreateArgs, ", %sProvider.get()", vmConstrParam.getName());
            } else {
                append(factoryCreateParams, ", %s", vmConstrParam.getTypeAndName());
                append(factoryCreateArgs, ", %s", vmConstrParam.getName());
            }
        }

        trimFirstSeparator(factoryConstrParams);
        trimFirstSeparator(vmCreateArgs);
        trimFirstSeparator(factory2ConstrParams);
        trimFirstSeparator(factoryCreateParams);
        trimFirstSeparator(factoryCreateArgs);

        String factory2File = String.format("" +
                        "package %1$s;\n" +
                        "\n" +
                        "import javax.inject.Inject;\n" +
                        "import javax.inject.Provider;\n" +
                        "\n" +
                        "import androidx.annotation.NonNull;\n" +
                        "import androidx.lifecycle.ViewModel;\n" +
                        "import androidx.lifecycle.ViewModelProvider;\n" +
                        "\n" +
                        "public class %2$sFactory2 {\n" +
                        "    private static class %2$sFactory implements ViewModelProvider.Factory {\n" +
                        "        %3$s\n" +
                        "\n" +
                        "        public %2$sFactory(%4$s) {\n" +
                        "            %5$s\n" +
                        "        }\n" +
                        "\n" +
                        "        @NonNull\n" +
                        "        @Override\n" +
                        "        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {\n" +
                        "            return (T) new %2$s(%6$s);\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    %7$s\n" +
                        "\n" +
                        "    @Inject\n" +
                        "    public %2$sFactory2(%8$s) {\n" +
                        "        %9$s\n" +
                        "    }\n" +
                        "\n" +
                        "    public ViewModelProvider.Factory create(%10$s) {\n" +
                        "        return new %2$sFactory(%11$s);\n" +
                        "    }\n" +
                        "}\n",
                packageName,
                vmName,
                factoryMembers,
                factoryConstrParams,
                factoryConstrStatements,
                vmCreateArgs,
                factory2Members,
                factory2ConstrParams,
                factory2ConstrStatements,
                factoryCreateParams,
                factoryCreateArgs);

        try {
            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(viewModel.getQualifiedName() + "Factory2");
            Writer writer = builderFile.openWriter();
            writer.write(factory2File);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    private List<VmConstrParam> getVmConstrParams(ExecutableElement vmConstructor) {
        List<VmConstrParam> vmConstrParams = new ArrayList<>();
        for (VariableElement param : vmConstructor.getParameters()) {
            vmConstrParams.add(new VmConstrParam(
                    param.asType().toString(),
                    param.getSimpleName().toString(),
                    param.getAnnotation(Provided.class) != null));
        }
        return vmConstrParams;
    }

    private void append(StringBuilder stringBuilder, String format, String... args) {
        stringBuilder.append(String.format(format, args));
    }

    private void trimFirstSeparator(StringBuilder stringBuilder) {
        if (stringBuilder.length() > 2) {
            stringBuilder.delete(0, 2);
        }
    }
}
