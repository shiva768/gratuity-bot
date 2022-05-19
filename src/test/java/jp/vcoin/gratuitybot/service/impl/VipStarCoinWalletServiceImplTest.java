package jp.vcoin.gratuitybot.service.impl;

import jp.vcoin.gratuitybot.domain.RecordValue;
import jp.vcoin.gratuitybot.exception.BalanceShortfallException;
import jp.vcoin.gratuitybot.exception.RPCException;
import jp.vcoin.gratuitybot.json.VirtualCoinRequestJson;
import jp.vcoin.gratuitybot.repository.VirtualCoinWalletRepository;
import jp.vcoin.gratuitybot.service.VirtualCoinRecordService;
import jp.vcoin.gratuitybot.service.VirtualCoinWalletService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class VirtualCoinWalletServiceImplTest {

    @MockBean
    VirtualCoinWalletRepository virtualCoinWalletRepository;
    @MockBean
    VirtualCoinRecordService virtualCoinRecordService;

    @Before
    public void setup() {
        VirtualCoinWalletServiceImpl.GetBalanceResponse balance = new VirtualCoinWalletServiceImpl.GetBalanceResponse();
        balance.result = new BigDecimal(3000);
        VirtualCoinWalletServiceImpl.GetBalanceResponse allBalance = new VirtualCoinWalletServiceImpl.GetBalanceResponse();
        allBalance.result = new BigDecimal(5000);
        doReturn(balance).when(virtualCoinWalletRepository).request(
                any(),
                eq(VirtualCoinWalletServiceImpl.GetBalanceResponse.class)
        );
        doReturn(allBalance).when(virtualCoinWalletRepository).request(
                any(),
                eq(VirtualCoinWalletServiceImpl.GetBalanceResponse.class)
        );
    }

    @Test
    public void move_1() throws NoSuchFieldException, IllegalAccessException {
        // setup
        String fromId = "from";
        String toId = "to";
        BigDecimal amount = new BigDecimal(1);

        ArgumentCaptor<VirtualCoinRequestJson> captorRequestJson = ArgumentCaptor.forClass(VirtualCoinRequestJson.class);
        final VirtualCoinWalletServiceImpl.MoveResponse moveResponse = new VirtualCoinWalletServiceImpl.MoveResponse();
        moveResponse.setResult(true);
        when(virtualCoinWalletRepository.request(captorRequestJson.capture(), eq(VirtualCoinWalletServiceImpl.MoveResponse.class))).thenReturn(moveResponse);
        when(virtualCoinRecordService.getRecord(fromId)).thenReturn(RecordValue.defaultValue(fromId));
        when(virtualCoinRecordService.getRecord(toId)).thenReturn(RecordValue.defaultValue(toId));
        ArgumentCaptor<RecordValue> captorFrom = ArgumentCaptor.forClass(RecordValue.class);
        ArgumentCaptor<RecordValue> captorTo = ArgumentCaptor.forClass(RecordValue.class);
        doNothing().when(virtualCoinRecordService).gratuity(captorFrom.capture(), captorTo.capture());


        // when
        VirtualCoinWalletService walletService = new VirtualCoinWalletServiceImpl(virtualCoinWalletRepository, virtualCoinRecordService);
        try {
            walletService.move(fromId, toId, amount);
        } catch (RPCException e) {
        } catch (BalanceShortfallException e) {
            e.printStackTrace();
        }

        // then
        final List<Object> params = captorRequestJson.getValue().getParams();
        assertThat(params.get(0), is(fromId));
        assertThat(params.get(1), is(toId));
        assertThat(params.get(2), is(amount));
        final RecordValue fromValue = captorFrom.getValue();
        final RecordValue toValue = captorTo.getValue();
        final Field sendGratuityField = getRecordValueDeclared("sendGratuity");
        final Field sendGratuityAmountField = getRecordValueDeclared("sendGratuityAmount");
        final Field receivedGratuityField = getRecordValueDeclared("receivedGratuity");
        final Field receivedGratuityAmountField = getRecordValueDeclared("receivedGratuityAmount");
        final Field sendEmoji1Field = getRecordValueDeclared("sendEmoji1");
        final Field sendEmoji10Field = getRecordValueDeclared("sendEmoji10");
        final Field sendEmoji100Field = getRecordValueDeclared("sendEmoji100");
        final Field sendEmoji1000Field = getRecordValueDeclared("sendEmoji1000");
        final Field receivedEmoji1Field = getRecordValueDeclared("receivedEmoji1");
        final Field receivedEmoji10Field = getRecordValueDeclared("receivedEmoji10");
        final Field receivedEmoji100Field = getRecordValueDeclared("receivedEmoji100");
        final Field receivedEmoji1000Field = getRecordValueDeclared("receivedEmoji1000");

        Assert.assertThat(sendGratuityField.get(fromValue), is(1));
        Assert.assertThat(sendGratuityAmountField.get(fromValue), is(new BigDecimal(1)));
        Assert.assertThat(receivedGratuityField.get(fromValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(fromValue), is(0));

        Assert.assertThat(sendGratuityField.get(toValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(toValue), is(1));
        Assert.assertThat(receivedGratuityAmountField.get(toValue), is(new BigDecimal(1)));
        Assert.assertThat(sendEmoji1Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(toValue), is(0));

        verify(virtualCoinWalletRepository, times(3)).request(any(), any());
        verify(virtualCoinRecordService, times(2)).getRecord(anyString());
        verify(virtualCoinRecordService, times(1)).gratuity(any(), any());
    }

    @Test
    public void move_10() throws NoSuchFieldException, IllegalAccessException, RPCException, BalanceShortfallException {
        // setup
        String fromId = "from";
        String toId = "to";
        BigDecimal amount = new BigDecimal(10);

        ArgumentCaptor<VirtualCoinRequestJson> captorRequestJson = ArgumentCaptor.forClass(VirtualCoinRequestJson.class);
        final VirtualCoinWalletServiceImpl.MoveResponse moveResponse = new VirtualCoinWalletServiceImpl.MoveResponse();
        moveResponse.setResult(true);
        when(virtualCoinWalletRepository.request(captorRequestJson.capture(), eq(VirtualCoinWalletServiceImpl.MoveResponse.class))).thenReturn(moveResponse);
        when(virtualCoinRecordService.getRecord(fromId)).thenReturn(RecordValue.defaultValue(fromId));
        when(virtualCoinRecordService.getRecord(toId)).thenReturn(RecordValue.defaultValue(toId));
        ArgumentCaptor<RecordValue> captorFrom = ArgumentCaptor.forClass(RecordValue.class);
        ArgumentCaptor<RecordValue> captorTo = ArgumentCaptor.forClass(RecordValue.class);
        doNothing().when(virtualCoinRecordService).gratuity(captorFrom.capture(), captorTo.capture());


        // when
        VirtualCoinWalletService walletService = new VirtualCoinWalletServiceImpl(virtualCoinWalletRepository, virtualCoinRecordService);
        walletService.move(fromId, toId, amount);

        // then
        final List<Object> params = captorRequestJson.getValue().getParams();
        assertThat(params.get(0), is(fromId));
        assertThat(params.get(1), is(toId));
        assertThat(params.get(2), is(amount));
        final RecordValue fromValue = captorFrom.getValue();
        final RecordValue toValue = captorTo.getValue();
        final Field sendGratuityField = getRecordValueDeclared("sendGratuity");
        final Field sendGratuityAmountField = getRecordValueDeclared("sendGratuityAmount");
        final Field receivedGratuityField = getRecordValueDeclared("receivedGratuity");
        final Field receivedGratuityAmountField = getRecordValueDeclared("receivedGratuityAmount");
        final Field sendEmoji1Field = getRecordValueDeclared("sendEmoji1");
        final Field sendEmoji10Field = getRecordValueDeclared("sendEmoji10");
        final Field sendEmoji100Field = getRecordValueDeclared("sendEmoji100");
        final Field sendEmoji1000Field = getRecordValueDeclared("sendEmoji1000");
        final Field receivedEmoji1Field = getRecordValueDeclared("receivedEmoji1");
        final Field receivedEmoji10Field = getRecordValueDeclared("receivedEmoji10");
        final Field receivedEmoji100Field = getRecordValueDeclared("receivedEmoji100");
        final Field receivedEmoji1000Field = getRecordValueDeclared("receivedEmoji1000");

        Assert.assertThat(sendGratuityField.get(fromValue), is(1));
        Assert.assertThat(sendGratuityAmountField.get(fromValue), is(new BigDecimal(10)));
        Assert.assertThat(receivedGratuityField.get(fromValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(fromValue), is(0));

        Assert.assertThat(sendGratuityField.get(toValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(toValue), is(1));
        Assert.assertThat(receivedGratuityAmountField.get(toValue), is(new BigDecimal(10)));
        Assert.assertThat(sendEmoji1Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(toValue), is(0));

        verify(virtualCoinWalletRepository, times(3)).request(any(), any());
        verify(virtualCoinRecordService, times(2)).getRecord(anyString());
        verify(virtualCoinRecordService, times(1)).gratuity(any(), any());
    }

    @Test
    public void move_100() throws NoSuchFieldException, IllegalAccessException, RPCException, BalanceShortfallException {
        // setup
        String fromId = "from";
        String toId = "to";
        BigDecimal amount = new BigDecimal(100);

        ArgumentCaptor<VirtualCoinRequestJson> captorRequestJson = ArgumentCaptor.forClass(VirtualCoinRequestJson.class);
        final VirtualCoinWalletServiceImpl.MoveResponse moveResponse = new VirtualCoinWalletServiceImpl.MoveResponse();
        moveResponse.setResult(true);
        when(virtualCoinWalletRepository.request(captorRequestJson.capture(), eq(VirtualCoinWalletServiceImpl.MoveResponse.class))).thenReturn(moveResponse);
        when(virtualCoinRecordService.getRecord(fromId)).thenReturn(RecordValue.defaultValue(fromId));
        when(virtualCoinRecordService.getRecord(toId)).thenReturn(RecordValue.defaultValue(toId));
        ArgumentCaptor<RecordValue> captorFrom = ArgumentCaptor.forClass(RecordValue.class);
        ArgumentCaptor<RecordValue> captorTo = ArgumentCaptor.forClass(RecordValue.class);
        doNothing().when(virtualCoinRecordService).gratuity(captorFrom.capture(), captorTo.capture());


        // when
        VirtualCoinWalletService walletService = new VirtualCoinWalletServiceImpl(virtualCoinWalletRepository, virtualCoinRecordService);
        walletService.move(fromId, toId, amount);

        // then
        final List<Object> params = captorRequestJson.getValue().getParams();
        assertThat(params.get(0), is(fromId));
        assertThat(params.get(1), is(toId));
        assertThat(params.get(2), is(amount));
        final RecordValue fromValue = captorFrom.getValue();
        final RecordValue toValue = captorTo.getValue();
        final Field sendGratuityField = getRecordValueDeclared("sendGratuity");
        final Field sendGratuityAmountField = getRecordValueDeclared("sendGratuityAmount");
        final Field receivedGratuityField = getRecordValueDeclared("receivedGratuity");
        final Field receivedGratuityAmountField = getRecordValueDeclared("receivedGratuityAmount");
        final Field sendEmoji1Field = getRecordValueDeclared("sendEmoji1");
        final Field sendEmoji10Field = getRecordValueDeclared("sendEmoji10");
        final Field sendEmoji100Field = getRecordValueDeclared("sendEmoji100");
        final Field sendEmoji1000Field = getRecordValueDeclared("sendEmoji1000");
        final Field receivedEmoji1Field = getRecordValueDeclared("receivedEmoji1");
        final Field receivedEmoji10Field = getRecordValueDeclared("receivedEmoji10");
        final Field receivedEmoji100Field = getRecordValueDeclared("receivedEmoji100");
        final Field receivedEmoji1000Field = getRecordValueDeclared("receivedEmoji1000");

        Assert.assertThat(sendGratuityField.get(fromValue), is(1));
        Assert.assertThat(sendGratuityAmountField.get(fromValue), is(new BigDecimal(100)));
        Assert.assertThat(receivedGratuityField.get(fromValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(fromValue), is(0));

        Assert.assertThat(sendGratuityField.get(toValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(toValue), is(1));
        Assert.assertThat(receivedGratuityAmountField.get(toValue), is(new BigDecimal(100)));
        Assert.assertThat(sendEmoji1Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(toValue), is(0));

        verify(virtualCoinWalletRepository, times(3)).request(any(), any());
        verify(virtualCoinRecordService, times(2)).getRecord(anyString());
        verify(virtualCoinRecordService, times(1)).gratuity(any(), any());
    }

    @Test
    public void move_1200() throws NoSuchFieldException, IllegalAccessException {
        // setup
        String fromId = "from";
        String toId = "to";
        BigDecimal amount = new BigDecimal(1200);

        ArgumentCaptor<VirtualCoinRequestJson> captorRequestJson = ArgumentCaptor.forClass(VirtualCoinRequestJson.class);
        final VirtualCoinWalletServiceImpl.MoveResponse moveResponse = new VirtualCoinWalletServiceImpl.MoveResponse();
        moveResponse.setResult(true);
        when(virtualCoinWalletRepository.request(captorRequestJson.capture(), eq(VirtualCoinWalletServiceImpl.MoveResponse.class))).thenReturn(moveResponse);
        when(virtualCoinRecordService.getRecord(fromId)).thenReturn(RecordValue.defaultValue(fromId));
        when(virtualCoinRecordService.getRecord(toId)).thenReturn(RecordValue.defaultValue(toId));
        ArgumentCaptor<RecordValue> captorFrom = ArgumentCaptor.forClass(RecordValue.class);
        ArgumentCaptor<RecordValue> captorTo = ArgumentCaptor.forClass(RecordValue.class);
        doNothing().when(virtualCoinRecordService).gratuity(captorFrom.capture(), captorTo.capture());


        // when
        VirtualCoinWalletService walletService = new VirtualCoinWalletServiceImpl(virtualCoinWalletRepository, virtualCoinRecordService);
        try {
            walletService.move(fromId, toId, amount);
        } catch (RPCException e) {
            e.printStackTrace();
        } catch (BalanceShortfallException e) {
            e.printStackTrace();
        }

        // then
        final List<Object> params = captorRequestJson.getValue().getParams();
        assertThat(params.get(0), is(fromId));
        assertThat(params.get(1), is(toId));
        assertThat(params.get(2), is(amount));
        final RecordValue fromValue = captorFrom.getValue();
        final RecordValue toValue = captorTo.getValue();
        final Field sendGratuityField = getRecordValueDeclared("sendGratuity");
        final Field sendGratuityAmountField = getRecordValueDeclared("sendGratuityAmount");
        final Field receivedGratuityField = getRecordValueDeclared("receivedGratuity");
        final Field receivedGratuityAmountField = getRecordValueDeclared("receivedGratuityAmount");
        final Field sendEmoji1Field = getRecordValueDeclared("sendEmoji1");
        final Field sendEmoji10Field = getRecordValueDeclared("sendEmoji10");
        final Field sendEmoji100Field = getRecordValueDeclared("sendEmoji100");
        final Field sendEmoji1000Field = getRecordValueDeclared("sendEmoji1000");
        final Field receivedEmoji1Field = getRecordValueDeclared("receivedEmoji1");
        final Field receivedEmoji10Field = getRecordValueDeclared("receivedEmoji10");
        final Field receivedEmoji100Field = getRecordValueDeclared("receivedEmoji100");
        final Field receivedEmoji1000Field = getRecordValueDeclared("receivedEmoji1000");

        Assert.assertThat(sendGratuityField.get(fromValue), is(1));
        Assert.assertThat(sendGratuityAmountField.get(fromValue), is(new BigDecimal(1200)));
        Assert.assertThat(receivedGratuityField.get(fromValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(fromValue), is(0));

        Assert.assertThat(sendGratuityField.get(toValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(toValue), is(1));
        Assert.assertThat(receivedGratuityAmountField.get(toValue), is(new BigDecimal(1200)));
        Assert.assertThat(sendEmoji1Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(toValue), is(0));

        verify(virtualCoinWalletRepository, times(3)).request(any(), any());
        verify(virtualCoinRecordService, times(2)).getRecord(anyString());
        verify(virtualCoinRecordService, times(1)).gratuity(any(), any());
    }

    @Test
    public void move_失敗() {
        // setup
        String fromId = "from";
        String toId = "to";
        BigDecimal amount = new BigDecimal(1);

        ArgumentCaptor<VirtualCoinRequestJson> captorRequestJson = ArgumentCaptor.forClass(VirtualCoinRequestJson.class);
        final VirtualCoinWalletServiceImpl.MoveResponse moveResponse = new VirtualCoinWalletServiceImpl.MoveResponse();
        moveResponse.setResult(false);
        when(virtualCoinWalletRepository.request(captorRequestJson.capture(), eq(VirtualCoinWalletServiceImpl.MoveResponse.class))).thenReturn(moveResponse);
        when(virtualCoinRecordService.getRecord(fromId)).thenReturn(RecordValue.defaultValue(fromId));
        when(virtualCoinRecordService.getRecord(toId)).thenReturn(RecordValue.defaultValue(toId));
        ArgumentCaptor<RecordValue> captorFrom = ArgumentCaptor.forClass(RecordValue.class);
        ArgumentCaptor<RecordValue> captorTo = ArgumentCaptor.forClass(RecordValue.class);
        doNothing().when(virtualCoinRecordService).gratuity(captorFrom.capture(), captorTo.capture());


        // when
        VirtualCoinWalletService walletService = new VirtualCoinWalletServiceImpl(virtualCoinWalletRepository, virtualCoinRecordService);
        try {
            walletService.move(fromId, toId, amount);
        } catch (RPCException e) {
        } catch (BalanceShortfallException e) {
            e.printStackTrace();
        }

        // then
        verify(virtualCoinRecordService, times(2)).getRecord(anyString());
        verify(virtualCoinRecordService, times(1)).gratuity(any(), any());
        verify(virtualCoinWalletRepository, times(3)).request(any(), any());
    }

    @Test
    public void move_初期F_TS3_TSA150_初期T_TR5_TRA300_plus_2300() throws NoSuchFieldException, IllegalAccessException, RPCException, BalanceShortfallException {
        // setup
        String fromId = "from";
        String toId = "to";
        BigDecimal amount = new BigDecimal(2300);

        ArgumentCaptor<VirtualCoinRequestJson> captorRequestJson = ArgumentCaptor.forClass(VirtualCoinRequestJson.class);
        final VirtualCoinWalletServiceImpl.MoveResponse moveResponse = new VirtualCoinWalletServiceImpl.MoveResponse();
        moveResponse.setResult(true);
        when(virtualCoinWalletRepository.request(captorRequestJson.capture(), eq(VirtualCoinWalletServiceImpl.MoveResponse.class))).thenReturn(moveResponse);
        final RecordValue _fromValue = RecordValue.defaultValue(fromId);
        _fromValue.setSendGratuity(3);
        _fromValue.setSendGratuityAmount(new BigDecimal(150));
        when(virtualCoinRecordService.getRecord(fromId)).thenReturn(_fromValue);
        final RecordValue _toValue = RecordValue.defaultValue(toId);
        _toValue.setReceivedGratuity(5);
        _toValue.setReceivedGratuityAmount(new BigDecimal(300));
        when(virtualCoinRecordService.getRecord(toId)).thenReturn(_toValue);
        ArgumentCaptor<RecordValue> captorFrom = ArgumentCaptor.forClass(RecordValue.class);
        ArgumentCaptor<RecordValue> captorTo = ArgumentCaptor.forClass(RecordValue.class);
        doNothing().when(virtualCoinRecordService).gratuity(captorFrom.capture(), captorTo.capture());

        // when
        VirtualCoinWalletService walletService = new VirtualCoinWalletServiceImpl(virtualCoinWalletRepository, virtualCoinRecordService);
        walletService.move(fromId, toId, amount);

        // then
        final List<Object> params = captorRequestJson.getValue().getParams();
        assertThat(params.get(0), is(fromId));
        assertThat(params.get(1), is(toId));
        assertThat(params.get(2), is(amount));
        final RecordValue fromValue = captorFrom.getValue();
        final RecordValue toValue = captorTo.getValue();
        final Field sendGratuityField = getRecordValueDeclared("sendGratuity");
        final Field sendGratuityAmountField = getRecordValueDeclared("sendGratuityAmount");
        final Field receivedGratuityField = getRecordValueDeclared("receivedGratuity");
        final Field receivedGratuityAmountField = getRecordValueDeclared("receivedGratuityAmount");
        final Field sendEmoji1Field = getRecordValueDeclared("sendEmoji1");
        final Field sendEmoji10Field = getRecordValueDeclared("sendEmoji10");
        final Field sendEmoji100Field = getRecordValueDeclared("sendEmoji100");
        final Field sendEmoji1000Field = getRecordValueDeclared("sendEmoji1000");
        final Field receivedEmoji1Field = getRecordValueDeclared("receivedEmoji1");
        final Field receivedEmoji10Field = getRecordValueDeclared("receivedEmoji10");
        final Field receivedEmoji100Field = getRecordValueDeclared("receivedEmoji100");
        final Field receivedEmoji1000Field = getRecordValueDeclared("receivedEmoji1000");

        Assert.assertThat(sendGratuityField.get(fromValue), is(4));
        Assert.assertThat(sendGratuityAmountField.get(fromValue), is(new BigDecimal(2450)));
        Assert.assertThat(receivedGratuityField.get(fromValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(fromValue), is(0));

        Assert.assertThat(sendGratuityField.get(toValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(toValue), is(6));
        Assert.assertThat(receivedGratuityAmountField.get(toValue), is(new BigDecimal(2600)));
        Assert.assertThat(sendEmoji1Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(toValue), is(0));

        verify(virtualCoinRecordService, times(2)).getRecord(anyString());
        verify(virtualCoinRecordService, times(1)).gratuity(any(), any());
        verify(virtualCoinWalletRepository, times(3)).request(any(), any());
    }

    private Field getRecordValueDeclared(String field) throws NoSuchFieldException {
        final Field declaredField = RecordValue.class.getDeclaredField(field);
        declaredField.setAccessible(true);
        return declaredField;
    }

    @Test
    public void moveEmoji_1() throws NoSuchFieldException, IllegalAccessException {
        // setup
        String fromId = "from";
        String toId = "to";
        BigDecimal amount = new BigDecimal(1);

        ArgumentCaptor<VirtualCoinRequestJson> captorRequestJson = ArgumentCaptor.forClass(VirtualCoinRequestJson.class);
        final VirtualCoinWalletServiceImpl.MoveResponse moveResponse = new VirtualCoinWalletServiceImpl.MoveResponse();
        moveResponse.setResult(true);
        when(virtualCoinWalletRepository.request(captorRequestJson.capture(), eq(VirtualCoinWalletServiceImpl.MoveResponse.class))).thenReturn(moveResponse);
        when(virtualCoinRecordService.getRecord(fromId)).thenReturn(RecordValue.defaultValue(fromId));
        when(virtualCoinRecordService.getRecord(toId)).thenReturn(RecordValue.defaultValue(toId));
        ArgumentCaptor<RecordValue> captorFrom = ArgumentCaptor.forClass(RecordValue.class);
        ArgumentCaptor<RecordValue> captorTo = ArgumentCaptor.forClass(RecordValue.class);
        doNothing().when(virtualCoinRecordService).gratuity(captorFrom.capture(), captorTo.capture());


        // when
        VirtualCoinWalletService walletService = new VirtualCoinWalletServiceImpl(virtualCoinWalletRepository, virtualCoinRecordService);
        try {
            walletService.moveEmoji(fromId, toId, amount);
        } catch (RPCException e) {
        } catch (BalanceShortfallException e) {
            e.printStackTrace();
        }

        // then
        final List<Object> params = captorRequestJson.getValue().getParams();
        assertThat(params.get(0), is(fromId));
        assertThat(params.get(1), is(toId));
        assertThat(params.get(2), is(amount));
        final RecordValue fromValue = captorFrom.getValue();
        final RecordValue toValue = captorTo.getValue();
        final Field sendGratuityField = getRecordValueDeclared("sendGratuity");
        final Field sendGratuityAmountField = getRecordValueDeclared("sendGratuityAmount");
        final Field receivedGratuityField = getRecordValueDeclared("receivedGratuity");
        final Field receivedGratuityAmountField = getRecordValueDeclared("receivedGratuityAmount");
        final Field sendEmoji1Field = getRecordValueDeclared("sendEmoji1");
        final Field sendEmoji10Field = getRecordValueDeclared("sendEmoji10");
        final Field sendEmoji100Field = getRecordValueDeclared("sendEmoji100");
        final Field sendEmoji1000Field = getRecordValueDeclared("sendEmoji1000");
        final Field receivedEmoji1Field = getRecordValueDeclared("receivedEmoji1");
        final Field receivedEmoji10Field = getRecordValueDeclared("receivedEmoji10");
        final Field receivedEmoji100Field = getRecordValueDeclared("receivedEmoji100");
        final Field receivedEmoji1000Field = getRecordValueDeclared("receivedEmoji1000");

        Assert.assertThat(sendGratuityField.get(fromValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(fromValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(fromValue), is(1));
        Assert.assertThat(sendEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(fromValue), is(0));

        Assert.assertThat(sendGratuityField.get(toValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(toValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(toValue), is(1));
        Assert.assertThat(receivedEmoji10Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(toValue), is(0));

        verify(virtualCoinWalletRepository, times(3)).request(any(), any());
        verify(virtualCoinRecordService, times(2)).getRecord(anyString());
        verify(virtualCoinRecordService, times(1)).gratuity(any(), any());
    }

    @Test
    public void moveEmoji_10() throws NoSuchFieldException, IllegalAccessException, RPCException, BalanceShortfallException {
        // setup
        String fromId = "from";
        String toId = "to";
        BigDecimal amount = new BigDecimal(10);

        ArgumentCaptor<VirtualCoinRequestJson> captorRequestJson = ArgumentCaptor.forClass(VirtualCoinRequestJson.class);
        final VirtualCoinWalletServiceImpl.MoveResponse moveResponse = new VirtualCoinWalletServiceImpl.MoveResponse();
        moveResponse.setResult(true);
        when(virtualCoinWalletRepository.request(captorRequestJson.capture(), eq(VirtualCoinWalletServiceImpl.MoveResponse.class))).thenReturn(moveResponse);
        when(virtualCoinRecordService.getRecord(fromId)).thenReturn(RecordValue.defaultValue(fromId));
        when(virtualCoinRecordService.getRecord(toId)).thenReturn(RecordValue.defaultValue(toId));
        ArgumentCaptor<RecordValue> captorFrom = ArgumentCaptor.forClass(RecordValue.class);
        ArgumentCaptor<RecordValue> captorTo = ArgumentCaptor.forClass(RecordValue.class);
        doNothing().when(virtualCoinRecordService).gratuity(captorFrom.capture(), captorTo.capture());


        // when
        VirtualCoinWalletService walletService = new VirtualCoinWalletServiceImpl(virtualCoinWalletRepository, virtualCoinRecordService);
        walletService.moveEmoji(fromId, toId, amount);

        // then
        final List<Object> params = captorRequestJson.getValue().getParams();
        assertThat(params.get(0), is(fromId));
        assertThat(params.get(1), is(toId));
        assertThat(params.get(2), is(amount));
        final RecordValue fromValue = captorFrom.getValue();
        final RecordValue toValue = captorTo.getValue();
        final Field sendGratuityField = getRecordValueDeclared("sendGratuity");
        final Field sendGratuityAmountField = getRecordValueDeclared("sendGratuityAmount");
        final Field receivedGratuityField = getRecordValueDeclared("receivedGratuity");
        final Field receivedGratuityAmountField = getRecordValueDeclared("receivedGratuityAmount");
        final Field sendEmoji1Field = getRecordValueDeclared("sendEmoji1");
        final Field sendEmoji10Field = getRecordValueDeclared("sendEmoji10");
        final Field sendEmoji100Field = getRecordValueDeclared("sendEmoji100");
        final Field sendEmoji1000Field = getRecordValueDeclared("sendEmoji1000");
        final Field receivedEmoji1Field = getRecordValueDeclared("receivedEmoji1");
        final Field receivedEmoji10Field = getRecordValueDeclared("receivedEmoji10");
        final Field receivedEmoji100Field = getRecordValueDeclared("receivedEmoji100");
        final Field receivedEmoji1000Field = getRecordValueDeclared("receivedEmoji1000");

        Assert.assertThat(sendGratuityField.get(fromValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(fromValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(fromValue), is(1));
        Assert.assertThat(sendEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(fromValue), is(0));

        Assert.assertThat(sendGratuityField.get(toValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(toValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(toValue), is(1));
        Assert.assertThat(receivedEmoji100Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(toValue), is(0));

        verify(virtualCoinWalletRepository, times(3)).request(any(), any());
        verify(virtualCoinRecordService, times(2)).getRecord(anyString());
        verify(virtualCoinRecordService, times(1)).gratuity(any(), any());
    }

    @Test
    public void moveEmoji_100() throws NoSuchFieldException, IllegalAccessException, RPCException, BalanceShortfallException {
        // setup
        String fromId = "from";
        String toId = "to";
        BigDecimal amount = new BigDecimal(100);

        ArgumentCaptor<VirtualCoinRequestJson> captorRequestJson = ArgumentCaptor.forClass(VirtualCoinRequestJson.class);
        final VirtualCoinWalletServiceImpl.MoveResponse moveResponse = new VirtualCoinWalletServiceImpl.MoveResponse();
        moveResponse.setResult(true);
        when(virtualCoinWalletRepository.request(captorRequestJson.capture(), eq(VirtualCoinWalletServiceImpl.MoveResponse.class))).thenReturn(moveResponse);
        when(virtualCoinRecordService.getRecord(fromId)).thenReturn(RecordValue.defaultValue(fromId));
        when(virtualCoinRecordService.getRecord(toId)).thenReturn(RecordValue.defaultValue(toId));
        ArgumentCaptor<RecordValue> captorFrom = ArgumentCaptor.forClass(RecordValue.class);
        ArgumentCaptor<RecordValue> captorTo = ArgumentCaptor.forClass(RecordValue.class);
        doNothing().when(virtualCoinRecordService).gratuity(captorFrom.capture(), captorTo.capture());


        // when
        VirtualCoinWalletService walletService = new VirtualCoinWalletServiceImpl(virtualCoinWalletRepository, virtualCoinRecordService);
        walletService.moveEmoji(fromId, toId, amount);

        // then
        final List<Object> params = captorRequestJson.getValue().getParams();
        assertThat(params.get(0), is(fromId));
        assertThat(params.get(1), is(toId));
        assertThat(params.get(2), is(amount));
        final RecordValue fromValue = captorFrom.getValue();
        final RecordValue toValue = captorTo.getValue();
        final Field sendGratuityField = getRecordValueDeclared("sendGratuity");
        final Field sendGratuityAmountField = getRecordValueDeclared("sendGratuityAmount");
        final Field receivedGratuityField = getRecordValueDeclared("receivedGratuity");
        final Field receivedGratuityAmountField = getRecordValueDeclared("receivedGratuityAmount");
        final Field sendEmoji1Field = getRecordValueDeclared("sendEmoji1");
        final Field sendEmoji10Field = getRecordValueDeclared("sendEmoji10");
        final Field sendEmoji100Field = getRecordValueDeclared("sendEmoji100");
        final Field sendEmoji1000Field = getRecordValueDeclared("sendEmoji1000");
        final Field receivedEmoji1Field = getRecordValueDeclared("receivedEmoji1");
        final Field receivedEmoji10Field = getRecordValueDeclared("receivedEmoji10");
        final Field receivedEmoji100Field = getRecordValueDeclared("receivedEmoji100");
        final Field receivedEmoji1000Field = getRecordValueDeclared("receivedEmoji1000");

        Assert.assertThat(sendGratuityField.get(fromValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(fromValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(fromValue), is(1));
        Assert.assertThat(sendEmoji1000Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(fromValue), is(0));

        Assert.assertThat(sendGratuityField.get(toValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(toValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(toValue), is(1));
        Assert.assertThat(receivedEmoji1000Field.get(toValue), is(0));

        verify(virtualCoinWalletRepository, times(3)).request(any(), any());
        verify(virtualCoinRecordService, times(2)).getRecord(anyString());
        verify(virtualCoinRecordService, times(1)).gratuity(any(), any());
    }

    @Test
    public void moveEmoji_1000() throws NoSuchFieldException, IllegalAccessException, RPCException, BalanceShortfallException {
        // setup
        String fromId = "from";
        String toId = "to";
        BigDecimal amount = new BigDecimal(1000);

        ArgumentCaptor<VirtualCoinRequestJson> captorRequestJson = ArgumentCaptor.forClass(VirtualCoinRequestJson.class);
        final VirtualCoinWalletServiceImpl.MoveResponse moveResponse = new VirtualCoinWalletServiceImpl.MoveResponse();
        moveResponse.setResult(true);
        when(virtualCoinWalletRepository.request(captorRequestJson.capture(), eq(VirtualCoinWalletServiceImpl.MoveResponse.class))).thenReturn(moveResponse);
        when(virtualCoinRecordService.getRecord(fromId)).thenReturn(RecordValue.defaultValue(fromId));
        when(virtualCoinRecordService.getRecord(toId)).thenReturn(RecordValue.defaultValue(toId));
        ArgumentCaptor<RecordValue> captorFrom = ArgumentCaptor.forClass(RecordValue.class);
        ArgumentCaptor<RecordValue> captorTo = ArgumentCaptor.forClass(RecordValue.class);
        doNothing().when(virtualCoinRecordService).gratuity(captorFrom.capture(), captorTo.capture());


        // when
        VirtualCoinWalletService walletService = new VirtualCoinWalletServiceImpl(virtualCoinWalletRepository, virtualCoinRecordService);
        walletService.moveEmoji(fromId, toId, amount);

        // then
        final List<Object> params = captorRequestJson.getValue().getParams();
        assertThat(params.get(0), is(fromId));
        assertThat(params.get(1), is(toId));
        assertThat(params.get(2), is(amount));
        final RecordValue fromValue = captorFrom.getValue();
        final RecordValue toValue = captorTo.getValue();
        final Field sendGratuityField = getRecordValueDeclared("sendGratuity");
        final Field sendGratuityAmountField = getRecordValueDeclared("sendGratuityAmount");
        final Field receivedGratuityField = getRecordValueDeclared("receivedGratuity");
        final Field receivedGratuityAmountField = getRecordValueDeclared("receivedGratuityAmount");
        final Field sendEmoji1Field = getRecordValueDeclared("sendEmoji1");
        final Field sendEmoji10Field = getRecordValueDeclared("sendEmoji10");
        final Field sendEmoji100Field = getRecordValueDeclared("sendEmoji100");
        final Field sendEmoji1000Field = getRecordValueDeclared("sendEmoji1000");
        final Field receivedEmoji1Field = getRecordValueDeclared("receivedEmoji1");
        final Field receivedEmoji10Field = getRecordValueDeclared("receivedEmoji10");
        final Field receivedEmoji100Field = getRecordValueDeclared("receivedEmoji100");
        final Field receivedEmoji1000Field = getRecordValueDeclared("receivedEmoji1000");

        Assert.assertThat(sendGratuityField.get(fromValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(fromValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(fromValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(fromValue), is(1));
        Assert.assertThat(receivedEmoji1Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(fromValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(fromValue), is(0));

        Assert.assertThat(sendGratuityField.get(toValue), is(0));
        Assert.assertThat(sendGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(receivedGratuityField.get(toValue), is(0));
        Assert.assertThat(receivedGratuityAmountField.get(toValue), is(new BigDecimal(0)));
        Assert.assertThat(sendEmoji1Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji10Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji100Field.get(toValue), is(0));
        Assert.assertThat(sendEmoji1000Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji10Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji100Field.get(toValue), is(0));
        Assert.assertThat(receivedEmoji1000Field.get(toValue), is(1));

        verify(virtualCoinWalletRepository, times(3)).request(any(), any());
        verify(virtualCoinRecordService, times(2)).getRecord(anyString());
        verify(virtualCoinRecordService, times(1)).gratuity(any(), any());
    }
}