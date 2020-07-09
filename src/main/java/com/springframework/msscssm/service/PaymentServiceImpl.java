package com.springframework.msscssm.service;

import com.springframework.msscssm.domain.Payment;
import com.springframework.msscssm.domain.PaymentEvent;
import com.springframework.msscssm.domain.PaymentState;
import com.springframework.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final StateMachineFactory<PaymentState,PaymentEvent> factory;
    private final PaymentStateChangeListener listener;

    public static final String PAYMENT_ID_HEADER = "payment_id";

    @Override
    public Payment newPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return repository.save(payment);
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState,PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId,sm, PaymentEvent.PER_AUTHORIZE);
        return sm;
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        StateMachine<PaymentState,PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId,sm, PaymentEvent.AUTHORIZE);
        return sm;
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState,PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId,sm , PaymentEvent.AUTH_DECLINED);
        return sm;
    }

    private void sendEvent(Long paymentId,StateMachine<PaymentState,PaymentEvent> sm , PaymentEvent event){
        Message msg = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER,paymentId)
                .build();
        sm.sendEvent(msg);
    }

    private StateMachine<PaymentState,PaymentEvent> build(Long id){
        Payment payment = repository.getOne(id);
        StateMachine<PaymentState,PaymentEvent> machine = factory.getStateMachine(Long.toString(payment.getId()));
        machine.stop();;
        machine.getStateMachineAccessor()
                .doWithAllRegions(sm->{
                    sm.addStateMachineInterceptor(listener);
                    sm.resetStateMachine(new DefaultStateMachineContext<>(payment.getState(),null,null,null));
                });
        machine.start();
        return machine;
    }
}
