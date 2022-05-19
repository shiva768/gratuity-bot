package jp.vcoin.gratuitybot.domain;

import jp.vcoin.gratuitybot.util.Formatter;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import lombok.Setter;
import sx.blah.discord.util.EmbedBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Locale;

@Entity
@Table(name = "record")
@Setter
public class RecordValue {

    @Id
    @Column(name = "UserId")
    private String userId;
    @Column(name = "SendGratuity")
    private int sendGratuity;
    @Column(name = "SendGratuityAmount")
    private BigDecimal sendGratuityAmount;
    @Column(name = "SendStar")
    private int sendStar;
    @Column(name = "SendStarAmount")
    private BigDecimal sendStarAmount;
    @Column(name = "ReceivedGratuity")
    private int receivedGratuity;
    @Column(name = "ReceivedGratuityAmount")
    private BigDecimal receivedGratuityAmount;
    @Column(name = "ReceivedStar")
    private int receivedStar;
    @Column(name = "ReceivedStarAmount")
    private BigDecimal receivedStarAmount;
    @Column(name = "SendStamp1")
    private int sendEmoji1;
    @Column(name = "ReceivedStamp1")
    private int receivedEmoji1;
    @Column(name = "SendStamp10")
    private int sendEmoji10;
    @Column(name = "ReceivedStamp10")
    private int receivedEmoji10;
    @Column(name = "SendStamp100")
    private int sendEmoji100;
    @Column(name = "ReceivedStamp100")
    private int receivedEmoji100;
    @Column(name = "SendStamp1000")
    private int sendEmoji1000;
    @Column(name = "ReceivedStamp1000")
    private int receivedEmoji1000;

    public static RecordValue defaultValue(String authorId) {
        final RecordValue recordValue = new RecordValue();
        recordValue.setUserId(authorId);
        recordValue.setReceivedStarAmount(new BigDecimal(0));
        recordValue.setReceivedGratuityAmount(new BigDecimal(0));
        recordValue.setSendStarAmount(new BigDecimal(0));
        recordValue.setSendGratuityAmount(new BigDecimal(0));
        return recordValue;
    }

    public void send(BigDecimal amount) {
        sendGratuity++;
        sendGratuityAmount = sendGratuityAmount.add(amount);
    }

    @SuppressWarnings("Duplicates")
    public void sendEmoji(BigDecimal amount) {
        // TODO ここの実装がやっつけすぎるので、どうにかしたい(reflection?)
        switch (amount.intValue()) {
            case 1:
                sendEmoji1++;
                break;
            case 10:
                sendEmoji10++;
                break;
            case 100:
                sendEmoji100++;
                break;
            case 1000:
                sendEmoji1000++;
                break;
            default:
        }
    }

    public void receive(BigDecimal amount) {
        receivedGratuity++;
        receivedGratuityAmount = receivedGratuityAmount.add(amount);
    }

    @SuppressWarnings("Duplicates")
    public void receiveEmoji(BigDecimal amount) {
        switch (amount.intValue()) {
            case 1:
                receivedEmoji1++;
                break;
            case 10:
                receivedEmoji10++;
                break;
            case 100:
                receivedEmoji100++;
                break;
            case 1000:
                receivedEmoji1000++;
                break;
            default:
        }
    }

    public EmbedBuilder getBuilder(MessageSourceWrapper messageSourceWrapper, Locale locale) {
        return new EmbedBuilder()
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.record.gratuity-count.send.title", locale),
                        sendGratuity + messageSourceWrapper.getMessage("discord.bot.record.gratuity-count.send.unit", locale),
                        true
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.record.gratuity.send.title", locale),
                        Formatter.formatValue().format(sendGratuityAmount) + messageSourceWrapper.getMessage("discord.bot.record.gratuity.send.unit", locale),
                        true
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.record.gratuity-count.received.title", locale),
                        receivedGratuity + messageSourceWrapper.getMessage("discord.bot.record.gratuity-count.received.unit", locale),
                        true
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.record.gratuity.received.title", locale),
                        Formatter.formatValue().format(receivedGratuityAmount) + messageSourceWrapper.getMessage("discord.bot.record.gratuity.received.unit", locale),
                        true
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.record.1emoji-count.send.title", locale),
                        sendEmoji1 + messageSourceWrapper.getMessage("discord.bot.record.1emoji-count.send.unit", locale),
                        true
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.record.1emoji-count.received.title", locale),
                        receivedEmoji1 + messageSourceWrapper.getMessage("discord.bot.record.1emoji-count.received.unit", locale),
                        true
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.record.10emoji-count.send.title", locale),
                        sendEmoji10 + messageSourceWrapper.getMessage("discord.bot.record.10emoji-count.send.unit", locale),
                        true
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.record.10emoji-count.received.title", locale),
                        receivedEmoji10 + messageSourceWrapper.getMessage("discord.bot.record.10emoji-count.received.unit", locale),
                        true
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.record.100emoji-count.send.title", locale),
                        sendEmoji100 + messageSourceWrapper.getMessage("discord.bot.record.100emoji-count.send.unit", locale),
                        true
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.record.100emoji-count.received.title", locale),
                        receivedEmoji100 + messageSourceWrapper.getMessage("discord.bot.record.100emoji-count.received.unit", locale),
                        true
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.record.1000emoji-count.send.title", locale),
                        sendEmoji1000 + messageSourceWrapper.getMessage("discord.bot.record.1000emoji-count.send.unit", locale),
                        true
                )
                .appendField(
                        messageSourceWrapper.getMessage("discord.bot.record.1000emoji-count.received.title", locale),
                        receivedEmoji1000 + messageSourceWrapper.getMessage("discord.bot.record.1000emoji-count.received.unit", locale),
                        true
                );
    }
}
