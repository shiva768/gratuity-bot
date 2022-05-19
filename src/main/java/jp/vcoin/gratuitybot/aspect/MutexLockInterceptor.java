package jp.vcoin.gratuitybot.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Aspect
@Component
@Slf4j
@Order(105)
public class MutexLockInterceptor {

    Lock lock = new ReentrantLock();

    @Around("execution(* jp.vcoin.gratuitybot.service.VirtualCoinWalletService.move*(..)) || execution(* jp.vcoin.gratuitybot.service.VirtualCoinWalletService.sendMany(..))")
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        lock.lock();
        try {
            return joinPoint.proceed(joinPoint.getArgs());
        } finally {
            lock.unlock();
        }
    }
}
