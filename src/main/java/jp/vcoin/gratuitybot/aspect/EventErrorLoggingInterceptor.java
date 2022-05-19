package jp.vcoin.gratuitybot.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

@Aspect
@Component
@Slf4j
@Order(100)
public class EventErrorLoggingInterceptor {

    @AfterThrowing(value = "execution(* jp.virtualcoin.gratuitybot.event.*.*(..))", throwing = "ex")
    public void throwing(HttpServerErrorException ex) {
        log.error(ex.getResponseBodyAsString());
    }
}
