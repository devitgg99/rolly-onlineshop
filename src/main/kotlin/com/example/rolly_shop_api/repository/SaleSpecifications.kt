package com.example.rolly_shop_api.repository

import com.example.rolly_shop_api.model.entity.PaymentMethod
import com.example.rolly_shop_api.model.entity.Sale
import com.example.rolly_shop_api.model.entity.SaleItem
import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal
import java.time.Instant
import java.util.*

object SaleSpecifications {

    fun withFilters(
        startDate: Instant?,
        endDate: Instant?,
        paymentMethod: PaymentMethod?,
        minAmount: BigDecimal?,
        maxAmount: BigDecimal?,
        customerName: String?,
        productId: UUID?
    ): Specification<Sale> {
        return Specification { root, query, criteriaBuilder ->
            val predicates = mutableListOf<Predicate>()

            // Date range filter
            startDate?.let {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), it))
            }
            endDate?.let {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), it))
            }

            // Payment method filter
            paymentMethod?.let {
                predicates.add(criteriaBuilder.equal(root.get<PaymentMethod>("paymentMethod"), it))
            }

            // Amount range filter
            minAmount?.let {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), it))
            }
            maxAmount?.let {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), it))
            }

            // Customer name filter (case-insensitive partial match)
            if (!customerName.isNullOrBlank()) {
                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("customerName")),
                        "%${customerName.lowercase()}%"
                    )
                )
            }

            // Product filter (sales containing specific product)
            productId?.let {
                val itemsJoin: Join<Sale, SaleItem> = root.join("items", JoinType.LEFT)
                predicates.add(criteriaBuilder.equal(itemsJoin.get<UUID>("product").get<UUID>("id"), it))
            }

            // Use DISTINCT to avoid duplicates from joins
            query?.distinct(true)

            criteriaBuilder.and(*predicates.toTypedArray())
        }
    }
}
