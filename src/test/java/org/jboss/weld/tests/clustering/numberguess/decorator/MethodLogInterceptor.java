package org.jboss.weld.tests.clustering.numberguess.decorator;

import java.io.Serializable;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * Logs method calls
 * @author maschmid
 *
 */
@MethodLog
@Interceptor
public class MethodLogInterceptor implements Serializable {

    private static final long serialVersionUID = -4841748774505749002L;

    @AroundInvoke
    public Object log(InvocationContext ctx) throws Exception 
    {
    	// don't actually do anything special
    	return ctx.proceed();
    }
}
