package org.jboss.weld.tests.clustering.numberguess.decorator;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * Call Game.reset() upon bean construction.
 * @author <a href="http://community.jboss.org/people/jharting">Jozef Hartinger</a>
 *
 */
@AutoReset
@Interceptor
public class AutoResetInterceptor implements Serializable {

    private static final long serialVersionUID = -4841748774505749002L;

    @PostConstruct
    public void init(InvocationContext ctx) {
        try {
            ctx.proceed();
        } catch (Exception e) {
            throw new RuntimeException();
        }
        if (ctx.getTarget() instanceof Game) {
            Game game = (Game) ctx.getTarget();
            game.reset();
        }
    }
}
