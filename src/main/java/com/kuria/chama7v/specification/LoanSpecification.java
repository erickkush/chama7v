package com.kuria.chama7v.specification;


import com.kuria.chama7v.entity.Loan;
import com.kuria.chama7v.entity.enums.LoanStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoanSpecification {
    public static Specification<Loan> filter(Long memberId, LoanStatus status,
                                             BigDecimal minAmount, BigDecimal maxAmount,
                                             LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (memberId != null) predicates.add(cb.equal(root.get("member").get("id"), memberId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (minAmount != null) predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            if (maxAmount != null) predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("applicationDate"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("applicationDate"), to));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
