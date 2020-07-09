package com.springframework.msscssm.config.actions;

import com.springframework.msscssm.domain.PaymentEvent;
import com.springframework.msscssm.domain.PaymentState;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Random;

import static com.springframework.msscssm.service.PaymentServiceImpl.PAYMENT_ID_HEADER;

@Component
public class PreAuthAction implements Action<PaymentState, PaymentEvent> {

    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> stateContext) {
        System.out.println("PreAuth was called!");
        if(new Random().nextInt(10) <8 ){
            System.out.println("Approved");
            stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                    .setHeader(PAYMENT_ID_HEADER,stateContext.getMessageHeader(PAYMENT_ID_HEADER)).build());
        }else{
            System.out.println("Declined");
            stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
                    .setHeader(PAYMENT_ID_HEADER,stateContext.getMessageHeader(PAYMENT_ID_HEADER)).build());
        }
    }
}
