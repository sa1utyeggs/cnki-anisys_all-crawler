package com.hh.aop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger(ShellBanner.class);
    private final String executionExpression = "execution(* com.hh.function.application.CnkiDatabaseService.*(..))";

    @Around(executionExpression)
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        // 获得类对象名
        String classSimpleName = proceedingJoinPoint.getTarget().getClass().getSimpleName();
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
        logger.info("start:" + classSimpleName + "." + methodName + "(" + params + ")");

        try {
            // 方法执行
            returnValue = proceedingJoinPoint.proceed();
        } catch (Throwable t) {
            logger.error("错误：" + t.getMessage());
            // 不希望在这里处理，只是做一个记录
            throw t;
        } finally {
            logger.info("returns ==> " + returnValue);
            logger.info("end:" + methodName + "(" + params + ")");

        }
        return returnValue;
    }
}

