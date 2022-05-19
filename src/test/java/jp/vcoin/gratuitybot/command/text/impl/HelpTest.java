package jp.vcoin.gratuitybot.command.text.impl;

import jp.vcoin.gratuitybot.adapter.MessageReceivedEventAdapter;
import jp.vcoin.gratuitybot.config.ApplicationProperties;
import jp.vcoin.gratuitybot.util.MessageSourceWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sx.blah.discord.handle.obj.IUser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class HelpTest {

    @MockBean
    ApplicationProperties applicationProperties;
    @MockBean
    MessageSourceWrapper messageSourceWrapper;

    MessageReceivedEventAdapter eventAdapter;
    IUser actionUser;
    String accountId;

    @Before
    public void setUp() {
        eventAdapter = mock(MessageReceivedEventAdapter.class);
        actionUser = mock(IUser.class);

        when(actionUser.getStringID()).thenReturn(accountId);
        when(eventAdapter.getAuthor()).thenReturn(actionUser);
    }

    @Test
    public void execute() {
    }
}