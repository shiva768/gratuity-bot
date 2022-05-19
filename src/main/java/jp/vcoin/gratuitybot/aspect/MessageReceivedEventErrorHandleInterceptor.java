package jp.vcoin.gratuitybot.aspect;

import jp.vcoin.gratuitybot.service.LocaleService;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.Locale;

@Aspect
@Component
@Slf4j
@Order(101)
public class MessageReceivedEventErrorHandleInterceptor {

    private final MessageSourceWrapper messageSourceWrapper;
    private final LocaleService localeService;

    @Autowired
    public MessageReceivedEventErrorHandleInterceptor(MessageSourceWrapper messageSourceWrapper, LocaleService localeService) {
        this.messageSourceWrapper = messageSourceWrapper;
        this.localeService = localeService;
    }

    @AfterThrowing(value = "execution(* jp.vcoin.gratuitybot.event.MessageCreate.handle(..))", throwing = "ex")
    public void throwing(JoinPoint joinPoint, HttpServerErrorException ex) {
        final MessageReceivedEvent messageReceivedEvent = (MessageReceivedEvent) joinPoint.getArgs()[0];
        final Locale locale = localeService.getLocale(messageReceivedEvent);

        messageReceivedEvent.getChannel().sendMessage(messageSourceWrapper.getMessage("discord.bot.failed-operation", locale, messageReceivedEvent.getAuthor()));
    }
}
