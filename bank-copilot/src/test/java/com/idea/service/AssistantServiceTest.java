package com.idea.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idea.dto.AssistantChatRequest;
import com.idea.dto.PayeeTransferDTO;
import com.idea.dto.TransactionQueryDTO;
import com.idea.entity.Account;
import com.idea.entity.FaqKnowledge;
import com.idea.entity.PageResult;
import com.idea.entity.PayeeContact;
import com.idea.entity.Result;
import com.idea.llm.QwenClient;
import com.idea.mapper.PayeeContactMapper;
import com.idea.mapper.TransactionMapper;
import com.idea.service.impl.AccountServiceImpl;
import com.idea.service.impl.TransactionServiceImpl;
import com.idea.vo.AssistantChatResponse;
import com.idea.vo.TransactionVO;
import com.idea.vo.TransferValidateVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantServiceTest {

    @Mock
    private QwenClient qwenClient;
    @Mock
    private AccountServiceImpl accountService;
    @Mock
    private TransactionServiceImpl transactionService;
    @Mock
    private StatisticsService statisticsService;
    @Mock
    private PayeeService payeeService;
    @Mock
    private FaqKnowledgeService faqKnowledgeService;
    @Mock
    private AssistantTestLogService assistantTestLogService;
    @Mock
    private PayeeContactMapper payeeContactMapper;
    @Mock
    private TransactionMapper transactionMapper;

    private AssistantService assistantService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        assistantService = new AssistantService(
                qwenClient,
                accountService,
                transactionService,
                statisticsService,
                payeeService,
                faqKnowledgeService,
                assistantTestLogService,
                payeeContactMapper,
                transactionMapper
        );
    }

    @Test
    void chatShouldHandleEmotionBeforeCallingQwen() {
        Result result = assistantService.chat(1L, request("我很生气，我要投诉"));

        AssistantChatResponse response = (AssistantChatResponse) result.getData();
        assertEquals("emotion_support", response.getTrace().getIntent());
        assertTrue(response.getReply().contains("人工客服"));
        verifyNoInteractions(qwenClient);
        verify(assistantTestLogService).recordRuntime(eq(1L), any(AssistantChatRequest.class), eq("我很生气，我要投诉"), same(result), anyLong(), isNull());
    }

    @Test
    void chatShouldQueryBalanceAfterIntentParsing() {
        when(qwenClient.chat(anyList())).thenReturn(qwenContent("""
                {"intent":"balance","toAccountNo":"","payeeAlias":"","amount":"","description":"","txnType":"","limit":"","timeRange":"","metric":"","groupBy":"","needMonth":false}
                """));
        when(accountService.getAccountInfo(1L)).thenReturn(account());

        Result result = assistantService.chat(1L, request("查一下我的余额"));

        AssistantChatResponse response = (AssistantChatResponse) result.getData();
        assertEquals("balance", response.getTrace().getIntent());
        assertTrue(response.getReply().contains("6222000012345678"));
        assertTrue(response.getReply().contains("90700.67"));
        verify(assistantTestLogService).recordRuntime(eq(1L), any(AssistantChatRequest.class), eq("查一下我的余额"), same(result), anyLong(), isNull());
    }

    @Test
    void chatShouldReturnFaqAnswerWhenIntentIsOtherAndKnowledgeHits() {
        when(qwenClient.chat(anyList())).thenReturn(qwenContent("""
                {"intent":"other","toAccountNo":"","payeeAlias":"","amount":"","description":"","txnType":"","limit":"","timeRange":"","metric":"","groupBy":"","needMonth":false}
                """));
        FaqKnowledge faq = new FaqKnowledge();
        faq.setId(6L);
        faq.setStandardQuestion("银行卡丢失后如何处理？");
        faq.setAnswer("银行卡丢失后请立即挂失。");
        FaqKnowledgeService.ScoredFaq scoredFaq = new FaqKnowledgeService.ScoredFaq(faq, 0.92);
        FaqKnowledgeService.FaqSearchResult faqResult = FaqKnowledgeService.FaqSearchResult.hit(scoredFaq, List.of(scoredFaq));
        when(faqKnowledgeService.search("卡丢了怎么挂失")).thenReturn(faqResult);

        Result result = assistantService.chat(1L, request("卡丢了怎么挂失"));

        AssistantChatResponse response = (AssistantChatResponse) result.getData();
        assertEquals("faq", response.getTrace().getIntent());
        assertTrue(response.getReply().contains("立即挂失"));
        verify(faqKnowledgeService).logQuery(1L, "卡丢了怎么挂失", faqResult);
    }

    @Test
    void chatShouldCreatePendingTransferThenCancelIt() {
        when(qwenClient.chat(anyList())).thenReturn(qwenContent("""
                {"intent":"transfer","toAccountNo":"","payeeAlias":"弟弟","amount":"300","description":"午餐费","repeatMode":"","txnType":"","limit":"","timeRange":"","metric":"","groupBy":"","needMonth":false}
                """));
        PayeeContact payee = payee();
        when(payeeContactMapper.selectByUserIdAndAlias(1L, "弟弟")).thenReturn(List.of(payee));
        when(payeeContactMapper.selectByUserIdAndAccountNo(1L, "6222000098765432")).thenReturn(payee);
        when(payeeService.validateTransfer(eq(1L), eq(6L), any(PayeeTransferDTO.class)))
                .thenReturn(Result.success(validateVo()));

        Result pending = assistantService.chat(1L, request("给弟弟转300元，备注午餐费"));
        AssistantChatResponse pendingResponse = (AssistantChatResponse) pending.getData();
        assertEquals("awaiting_confirmation", pendingResponse.getTrace().getStatus());
        assertTrue(pendingResponse.getReply().contains("回复【确认】执行"));

        Result cancelled = assistantService.chat(1L, request("取消"));
        AssistantChatResponse cancelledResponse = (AssistantChatResponse) cancelled.getData();
        assertEquals("cancelled", cancelledResponse.getTrace().getStatus());
        assertTrue(cancelledResponse.getReply().contains("已取消"));
        verify(payeeService, never()).transfer(anyLong(), anyLong(), any(PayeeTransferDTO.class));
    }

    @Test
    void chatShouldAskMonthAndContinuePendingTransactionQuery() {
        when(qwenClient.chat(anyList()))
                .thenReturn(qwenContent("""
                        {"intent":"transactions","txnType":"收入","limit":"5","year":"","month":"","day":"17","timeRange":"day","needMonth":true}
                        """))
                .thenReturn(qwenContent("""
                        {"intent":"other","month":"1","needMonth":false}
                        """));

        Result needMonth = assistantService.chat(1L, request("查17号收入记录"));
        AssistantChatResponse needMonthResponse = (AssistantChatResponse) needMonth.getData();
        assertEquals("awaiting_more_context", needMonthResponse.getTrace().getStatus());
        assertTrue(needMonthResponse.getReply().contains("几月17号"));

        when(accountService.getAccountInfo(1L)).thenReturn(account());
        when(transactionService.getTransactions(any(TransactionQueryDTO.class)))
                .thenReturn(Result.success(new PageResult<>(1, 1, 5, List.of(transaction()))));

        Result completed = assistantService.chat(1L, request("1月"));
        AssistantChatResponse completedResponse = (AssistantChatResponse) completed.getData();
        assertEquals("completed", completedResponse.getTrace().getStatus());
        assertTrue(completedResponse.getReply().contains("工资"));

        ArgumentCaptor<TransactionQueryDTO> captor = ArgumentCaptor.forClass(TransactionQueryDTO.class);
        verify(transactionService).getTransactions(captor.capture());
        assertEquals("收入", captor.getValue().getType());
        assertTrue(captor.getValue().getStartTime().contains("-01-17T00:00"));
    }

    private AssistantChatRequest request(String message) {
        AssistantChatRequest request = new AssistantChatRequest();
        request.setMessage(message);
        return request;
    }

    private JsonNode qwenContent(String content) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode choices = root.putArray("choices");
        ObjectNode choice = choices.addObject();
        ObjectNode message = choice.putObject("message");
        message.put("content", content.trim());
        return root;
    }

    private Account account() {
        Account account = new Account();
        account.setId(1L);
        account.setUserId(1L);
        account.setAccountName("王奇");
        account.setAccountNo("6222000012345678");
        account.setAccountType("储蓄卡");
        account.setStatus("正常");
        account.setBalance(new BigDecimal("90700.67"));
        return account;
    }

    private PayeeContact payee() {
        PayeeContact payee = new PayeeContact();
        payee.setId(6L);
        payee.setUserId(1L);
        payee.setPayeeName("刘明");
        payee.setAccountNo("6222000098765432");
        payee.setAlias("弟弟");
        return payee;
    }

    private TransferValidateVO validateVo() {
        TransferValidateVO vo = new TransferValidateVO();
        vo.setFromAccountNoMasked("6222****5678");
        vo.setToAccountNoMasked("6222****5432");
        vo.setAmount(new BigDecimal("300"));
        vo.setBalance(new BigDecimal("90700.67"));
        vo.setDescription("午餐费");
        return vo;
    }

    private TransactionVO transaction() {
        TransactionVO vo = new TransactionVO();
        vo.setTradeTime("2026-01-17 09:00:00");
        vo.setType("收入");
        vo.setAmount(new BigDecimal("5000.00"));
        vo.setCounterpartyAccountNo("6222000098765432");
        vo.setDescription("工资");
        return vo;
    }
}
