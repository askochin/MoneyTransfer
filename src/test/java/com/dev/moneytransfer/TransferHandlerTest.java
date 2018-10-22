package com.dev.moneytransfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spark.Request;
import spark.Response;

import java.math.BigDecimal;

import static java.lang.String.valueOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferHandlerTest {

    private static final String ACCT_FROM = "a1";
    private static final String ACCT_TO = "a2";
    private static final String AMOUNT = "100";
    private static final long NEW_TRANSFER_ID = 1;

    @Mock
    private Request request;

    @Mock
    private Response response;

    @Mock
    private TransferService service;

    private TransferHandler handler;

    @BeforeEach
    void init() {
        handler = new TransferHandler(service);
        doReturn(ACCT_FROM).when(request).params(":fromAccount");
        doReturn(ACCT_TO).when(request).params(":toAccount");
    }

    @Test
    void shouldHandleTransferRequest() {

        doReturn(AMOUNT).when(request).queryParams("amount");
        doReturn(NEW_TRANSFER_ID).when(service).transfer(ACCT_FROM, ACCT_TO, new BigDecimal(AMOUNT));

        String responseBody = handler.handle(request, response);
        assertEquals(valueOf(NEW_TRANSFER_ID), responseBody);
        verify(response, times(1)).status(200);
        verifyNoMoreInteractions(response);
    }

    @Test
    void shouldSendBadRequestStatusIfAmountMissing() {

        doReturn(null).when(request).queryParams("amount");

        String responseBody = handler.handle(request, response);
        assertEquals("Missing amount param", responseBody);
        verify(response, times(1)).status(400);
        verifyNoMoreInteractions(response);
        verifyNoMoreInteractions(service);
    }

    @Test
    void shouldSendBadRequestStatusIfAmountIncorrect() {

        doReturn("WrongValue").when(request).queryParams("amount");

        String responseBody = handler.handle(request, response);
        assertEquals("Incorrect amount: 'WrongValue'", responseBody);
        verify(response, times(1)).status(400);
        verifyNoMoreInteractions(response);
        verifyNoMoreInteractions(service);
    }

    @Test
    void shouldSendBadRequestStatusInCaseOfIllegalArgumentException() {

        doReturn(AMOUNT).when(request).queryParams("amount");
        doThrow(new IllegalArgumentException("error_message"))
                .when(service).transfer(ACCT_FROM, ACCT_TO, new BigDecimal(AMOUNT));

        String responseBody = handler.handle(request, response);
        assertEquals("error_message", responseBody);
        verify(response, times(1)).status(400);
        verifyNoMoreInteractions(response);
    }

    @Test
    void shouldSendInternalErrorStatusInCaseOfNotIllegalArgumentException() {

        doReturn(AMOUNT).when(request).queryParams("amount");
        doThrow(new RuntimeException("error_message"))
                .when(service).transfer(ACCT_FROM, ACCT_TO, new BigDecimal(AMOUNT));

        String responseBody = handler.handle(request, response);
        assertEquals("Internal server error", responseBody);
        verify(response, times(1)).status(500);
        verifyNoMoreInteractions(response);
    }
}