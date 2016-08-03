package com.imcode.utils;

import com.imcode.entities.EntityRestProviderInformation;
import com.imcode.entities.MethodRestProviderForEntity;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by ruslan on 02.08.16.
 */
public class ControllerReflectionUtil {

    private static final String API_PATH = "/api/v1/{format}";
    private static final String ENTITY_PACKAGE = "com.imcode.entities";
    public static final String REST_CONTROLLERS_PACKAGE = "com.imcode.controllers.restful";

    private Class<?> controllerClass;
    private String controllerPath;
    private String entityName;

    private Method method;

    public ControllerReflectionUtil(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
        genAndSetControllerPathAndEntityName();
    }

    public Set<Method> getMethodsWithRequestMappingAnnotation() {
        Set<Method> declaredMethods = getAllMethodFromContrllerAndSuperClass();
        Set<Method> requestMapping = declaredMethods.stream()
                .filter(method -> AnnotationUtils.findAnnotation(method, RequestMapping.class) != null)
                .collect(Collectors.toSet());
        return requestMapping;
    }

    public String getControllerPath() {
        return controllerPath;
    }

    public String getEntityClass() {
        return ENTITY_PACKAGE + "." + entityName;
    }

    private void genAndSetControllerPathAndEntityName() {
        String simpleName = controllerClass.getSimpleName();
        if (simpleName.indexOf("RestControllerImpl") == -1) {
            System.out.println();
        }
        entityName = simpleName.substring(0, simpleName.indexOf("RestControllerImpl"));

        controllerPath = API_PATH + "/" + entityNameToLowerCaseInPluralForm(entityName);
    }

    private Set<Method> getAllMethodFromContrllerAndSuperClass() {
        Set<Method> declaredMethods = new LinkedHashSet<>();
        Collections.addAll(declaredMethods, controllerClass.getMethods());
        return declaredMethods;
    }

    public static Set<Class<? extends Object>> getAllClassesFromPackage(String packageName) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
        Set<BeanDefinition> classes = provider.findCandidateComponents(packageName);
        Set<Class<?>> allClasses = classes.stream()
                .map(bean -> {
                    try {
                        return Class.forName(bean.getBeanClassName());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toSet());
        return allClasses;
    }

    public MethodRestProviderForEntity buildPersistenceMethodOf(Method method, EntityRestProviderInformation savedInfo) {
        MethodRestProviderForEntity methodForPersist = new MethodRestProviderForEntity();
        methodForPersist.setEntityRestProviderInformation(savedInfo);
        methodForPersist.setName(getNameOf(method));
        methodForPersist.setUrl(controllerPath + getUrlOf(method));
        methodForPersist.setRequestMethod(getRequestMethodOf(method));
        methodForPersist.setInParameters(getInParametersOf(method));
        methodForPersist.setOutParameter(getOutParameterOf(method));
        return methodForPersist;
    }

    private String getNameOf(Method method) {
        return method.getName();
    }

    private String getUrlOf(Method method) {
        Set<String> values = new HashSet<>();
        Collections.addAll(values, AnnotationUtils.findAnnotation(method, RequestMapping.class).value());
        Optional<String> url = values.stream()
                .filter(value -> value.indexOf("/") != -1)
                .findFirst();
        return url.isPresent() ? url.get() : "";
    }

    private RequestMethod getRequestMethodOf(Method method) {
        return AnnotationUtils.findAnnotation(method, RequestMapping.class).method()[0];
    }

    private String getOutParameterOf(Method method) {
        return genReturnTypeByMethodName(method.getName());
    }

    private Map<String, String> getInParametersOf(Method method) {
        String[] allParameterNames = new DefaultParameterNameDiscoverer().getParameterNames(method);
        return IntStream
                .range(0, method.getParameterCount() - 1)
                .mapToObj(i -> new MethodParameter(method, i))
                .filter(methodParameter -> methodParameter.hasParameterAnnotation(PathVariable.class) ||
                        methodParameter.hasParameterAnnotation(RequestParam.class))
                .collect(Collectors.toMap(methodParameter -> allParameterNames[methodParameter.getParameterIndex()],
                        methodParameter -> GenericTypeResolver.resolveParameterType(methodParameter, controllerClass).getSimpleName()));

    }

    private String entityNameToLowerCaseInPluralForm(String entityName) {
        StringBuilder modifiedEntityName = new StringBuilder(entityName.toLowerCase());
        char lastLater = modifiedEntityName.charAt(modifiedEntityName.length() - 1);
        switch (lastLater) {
            case 'y': {
                modifiedEntityName.deleteCharAt(modifiedEntityName.length() - 1).append("ies");
                break;
            }

            case 's': {
                modifiedEntityName.append("es");
                break;
            }

            default:
                modifiedEntityName.append('s');
        }
        return modifiedEntityName.toString();
    }

    private String genReturnTypeByMethodName(String methodName) {
        boolean isMatch = methodName.matches("(?i)(.*)" + entityNameToLowerCaseInPluralForm(entityName)
                + "(.*)" + "|((?i).*)all(.*)");
        return isMatch ? "List<" + entityName + ">" : entityName;
    }


}
