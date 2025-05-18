package br.com.fiap.mspayments.infrastructure.persistence.repository;

import br.com.fiap.mspayments.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

}
