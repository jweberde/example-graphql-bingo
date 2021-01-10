package weber.de.example.graphql.bingo.infra;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.lang.reflect.Method;
import java.util.Set;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@Aspect
@Slf4j
public class ValidateMethodAspectJInterceptor {


    @Autowired
    private Validator validator;

    @Around("@annotation(ValidateMethod)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        log.debug("ValidateMethod: {}", point.getTarget().getClass());

        final MethodSignature signature = (MethodSignature) point.getSignature();
        final Method method = signature.getMethod();
        final Set<ConstraintViolation<Object>> constraintViolations = this.validator.forExecutables().validateParameters(point.getThis(), method, point.getArgs());
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
        return point.proceed();
    }
}
