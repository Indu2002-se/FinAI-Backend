package com.finai.backend.service.interfaces;

import com.finai.backend.dto.response.FeatureValidationResult;

import java.util.List;
import java.util.Map;

public interface FeatureValidationService {

    List<String> EXPECTED_FEATURE_NAMES = List.of(
            "age",
            "gender",
            "education",
            "marital_status",
            "household_size_f",
            "employment_income",
            "other_income",
            "windfall_income",
            "agri_income",
            "non_agri_income",
            "transfer_income",
            "total_income",
            "food_expenditure",
            "nonfood_expenditure",
            "total_expenditure",
            "expense_to_income_ratio",
            "financial_surplus",
            "savings_ratio",
            "per_capita_income",
            "employment_capacity",
            "debt_amount",
            "debt_records",
            "debt_sources",
            "debt_to_income_ratio",
            "credit_card_debt",
            "has_credit_card_debt",
            "has_creditmix_match",
            "credit_score",
            "credit_defaulted",
            "credit_clv",
            "credit_fraud_txn",
            "cc_utilization_ratio",
            "cc_late_payments",
            "cc_credit_lines",
            "cc_debt_to_income_ratio",
            "cc_total_spend_last_year",
            "cc_avg_txn_amount",
            "cc_total_txns",
            "cc_tenure_years",
            "vehicle_ownership",
            "instalment_goods_flag",
            "instalment_amount"
    );

    FeatureValidationResult validate(Map<String, Object> featureVector);
}
