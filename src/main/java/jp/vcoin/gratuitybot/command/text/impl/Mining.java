package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.domain.BalanceValue;
import jp.vcoin.gratuitybot.exception.BalanceShortfallException;
import jp.vcoin.gratuitybot.exception.RPCException;
import jp.vcoin.gratuitybot.service.VirtualCoinWalletService;
import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.command.text.MessageCommand;
import jp.vcoin.gratuitybot.marker.Text;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Optional;

@Component
@Text
@Slf4j
@Profile({"stage", "dev"})
public class Mining implements MessageCommand<MessageReceivedEvent> {

    private final ApplicationProperties applicationProperties;
    private final VirtualCoinWalletService virtualCoinWalletService;

    @Autowired
    public Mining(ApplicationProperties applicationProperties, VirtualCoinWalletService virtualCoinWalletService) {
        this.applicationProperties = applicationProperties;
        this.virtualCoinWalletService = virtualCoinWalletService;
    }

    @Override
    public void execute(MessageReceivedEventAdapter eventAdapter, Locale locale) {
        int miningCount = Integer.parseInt(eventAdapter.getContent(0));
        if (miningCount < 1001) {
            BalanceValue before = virtualCoinWalletService.getBalance("");
            if (exec(miningCount)) {
                BalanceValue after = virtualCoinWalletService.getBalance("");
                BigDecimal diff = after.getBalance().subtract(before.getBalance());
                try {
                    virtualCoinWalletService.move("", eventAdapter.getAuthor().getStringID(), diff);
                } catch (RPCException | BalanceShortfallException e) {
                    e.printStackTrace();
                }
                eventAdapter.sendMessage("mining");
            }
        }
    }

    private boolean exec(int miningCount) {
        final String dockerComposePath = Optional.ofNullable(System.getenv("DOCKER_COMPOSE_PATH")).orElse("");
        String[] command = {"bash", "-c", dockerComposePath + applicationProperties.getDebugMiningCommand() + " " + miningCount};
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(command);
            String errorString = IOUtils.toString(process.getErrorStream(), Charset.defaultCharset());
            log.debug("error:" + errorString);
            log.debug("stout:" + IOUtils.toString(process.getInputStream(), Charset.defaultCharset()));
            if (errorString == null || errorString.isEmpty())
                return true;
        } catch (IOException e) {
            log.debug("command exec error", e);
        }
        return false;
    }
}
