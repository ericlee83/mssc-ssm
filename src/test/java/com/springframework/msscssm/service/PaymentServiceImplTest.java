package com.springframework.msscssm.service;

import com.springframework.msscssm.domain.Payment;
import com.springframework.msscssm.domain.PaymentEvent;
import com.springframework.msscssm.domain.PaymentState;
import com.springframework.msscssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("11.27")).build();
    }

    @Test
    @Transactional
    void preAuth() {
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());
        Payment preAuthedPayment = paymentRepository.getOne(savedPayment.getId());
        System.out.println(sm.getState().getId());
        System.out.println(preAuthedPayment);
    }

    @RepeatedTest(10)
    @Transactional
    void authorizePayment() {
        Payment savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());
        if(sm.getState().getId() == PaymentState.PRE_AUTH){
            System.out.println("Payment is authorized");
            StateMachine<PaymentState,PaymentEvent> smAuth = paymentService.authorizePayment(savedPayment.getId());
            System.out.println("Result of Auth: "+smAuth.getState().getId());
        }else{
            System.out.println("Payment failed pre-auth....");
        }
    }
}