package com.hh.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.HashMap;

/**
 * @author 86183
 */
@Aspect
public class ShellBanner {
    private final String executionExpression = "execution(* com.hh.utils.DataBaseUtils.*(..))";

    @Around(executionExpression)
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        String methodName = signature.getName();
        Class[] parameterTypes = signature.getParameterTypes();
        String[] parameterNames = signature.getParameterNames();
        Object[] parameterValues = proceedingJoinPoint.getArgs();
        Object returnValue = null;
        int length = parameterTypes.length;
        HashMap<String, Object> params = new HashMap<>(length);
        for (int i = 0; i < length; i++) {
            params.put(parameterTypes[i].getSimpleName() + " " + parameterNames[i], parameterValues[i].toString());
        }
        System.out.println("===================== start:" + methodName + "(" + params + ") =====================");

        try {
            // 方法执行
            returnValue = proceedingJoinPoint.proceed();
        } catch (Throwable t) {
            System.out.println("!!!!!!!! 错误：" + t.getMessage() + " !!!!!!!!");
            // 不希望在这里处理，只是做一个记录
            throw t;
        } finally {
            System.out.println("returns ============> " + returnValue);
            System.out.println("=====================   end:" + methodName + "(" + params + ") =====================");

        }
        return returnValue;
    }
}

