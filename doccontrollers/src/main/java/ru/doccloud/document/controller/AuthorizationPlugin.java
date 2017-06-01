package ru.doccloud.document.controller;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class AuthorizationPlugin {
	
	private static final Logger logger = LoggerFactory.getLogger(AuthorizationPlugin.class);


	@Pointcut("execution(* ru.doccloud.document.controller.DocumentController.*(..))")
	public void businessMethods() { }

	@Around("businessMethods() && target(documentController)")
	public Object profile(ProceedingJoinPoint pjp, DocumentController documentController) throws Throwable {
		long start = System.currentTimeMillis();
	    logger.debug("AuthorizationPlugin: Going to call the method: {}", pjp.getSignature().getName());
        documentController.setUser();
	    Object output = pjp.proceed();
	    logger.debug("AuthorizationPlugin: Method execution completed.");
	    long elapsedTime = System.currentTimeMillis() - start;
	    logger.debug("AuthorizationPlugin: Method execution time: " + elapsedTime + " milliseconds.");

	    return output;
	}
}
