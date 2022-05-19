package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.service.VirtualCoinWalletService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Text;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import java.util.Locale;

@Component
@Text
public class Deposit implements MessageCommand<MessageReceivedEvent> {

    private final VirtualCoinWalletService virtualCoinWalletService;
    private final MessageSourceWrapper messageSourceWrapper;

    @Autowired
    public Deposit(VirtualCoinWalletService virtualCoinWalletService, MessageSourceWrapper messageSourceWrapper) {
        this.virtualCoinWalletService = virtualCoinWalletService;
        this.messageSourceWrapper = messageSourceWrapper;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {
        final IUser author = eventAdapter.getAuthor();
        final String authorId = author.getStringID();
        final String address = virtualCoinWalletService.getNewAddress(authorId);

        String message = messageSourceWrapper.getMessage("discord.bot.deposit", locale, author, "" + MessageBuilder.Styles.INLINE_CODE + address + MessageBuilder.Styles.INLINE_CODE);
        eventAdapter.sendMessage(message);
    }
}
