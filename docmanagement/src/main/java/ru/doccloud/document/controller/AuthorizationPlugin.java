package ru.doccloud.document.controller;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.doccloud.document.service.RepositoryDocumentCrudService;

@Component
@Aspect
public class AuthorizationPlugin {
	
	private static final Logger logger = LoggerFactory.getLogger(AuthorizationPlugin.class);

    
	@Pointcut("execution(* ru.doccloud.document.service.RepositoryDocumentCrudService.*(..))")
	public void businessMethods() { }
	
	@Around("businessMethods() && target(crud)")
	public Object profile(ProceedingJoinPoint pjp, RepositoryDocumentCrudService crud) throws Throwable {
		long start = System.currentTimeMillis();
	    logger.debug("AuthorizationPlugin: Going to call the method: {}", pjp.getSignature().getName());
	    crud.getRepository().setUser();
	    Object output = pjp.proceed();
	    logger.debug("AuthorizationPlugin: Method execution completed.");
	    long elapsedTime = System.currentTimeMillis() - start;
	    logger.debug("AuthorizationPlugin: Method execution time: " + elapsedTime + " milliseconds.");
	
	    return output;
	}
}
