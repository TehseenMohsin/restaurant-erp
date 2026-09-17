package com.devmasters.restaurant_erp.common.service.Sequence;

import com.devmasters.restaurant_erp.common.enums.MenuItemType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CodeGeneratorService {

    private final SequenceGeneratorService sequenceGeneratorService;

    public String generateCategoryCode() {

        long sequence = sequenceGeneratorService.generateSequence("CATEGORY");
        return "CAT" + String.format("%05d", sequence);
    }


    public String generateMenuItemCode(MenuItemType type) {

        long sequence = sequenceGeneratorService.generateSequence("MENU_ITEM");

        return getPrefix(type) + String.format("%05d", sequence);
    }

    private String getPrefix(MenuItemType type) {

        return switch (type) {

            case FOOD -> "FOD";

            case DRINK -> "DRK";

            case DESSERT -> "DES";

            case APPETIZER -> "APP";

            case SALAD -> "SAL";

            case SOUP -> "SOU";

            case COMBO -> "COM";

            case SIDE -> "SID";

            case ADDON -> "ADD";
        };
    }

    public String generateEmployeeCode(UUID organizationId) {

        String sequenceName = "EMPLOYEE_" + organizationId;

        long sequence = sequenceGeneratorService.generateSequence(sequenceName);

        return "EMP" + String.format("%05d", sequence);
    }

    public String generateMenuVariantCode(UUID branchId) {

        String sequenceName = "MENU_VARIANT_" + branchId;

        long sequence = sequenceGeneratorService.generateSequence(sequenceName);

        return "VAR" + String.format("%05d", sequence);
    }

    public String generateModifierGroupCode(UUID branchId) {

        String sequenceName = "MODIFIER_GROUP_" + branchId;

        long sequence = sequenceGeneratorService.generateSequence(sequenceName);

        return "MGP" + String.format("%05d", sequence);
    }

    public String generateModifierCode(UUID branchId) {

        String sequenceName = "MODIFIER_" + branchId;

        long sequence =
                sequenceGeneratorService.generateSequence(sequenceName);

        return "MOD" + String.format("%05d", sequence);
    }

    public String generateExpenseCategoryCode(UUID organizationId) {

        String sequenceName = "EXPENSE_CATEGORY_" + organizationId;

        long sequence = sequenceGeneratorService.generateSequence(sequenceName);

        return "EXPCAT" + String.format("%05d", sequence);
    }

    public String generateExpenseTypeCode(UUID organizationId) {

        String sequenceName = "EXPENSE_TYPE_" + organizationId;

        long sequence =
                sequenceGeneratorService.generateSequence(sequenceName);

        return "EXPTYP" + String.format("%05d", sequence);
    }

    public String generatePaymentMethodCode(UUID organizationId) {

        String sequenceName = "PAYMENT_METHOD_" + organizationId;

        long sequence =
                sequenceGeneratorService.generateSequence(sequenceName);

        return "PAY" + String.format("%05d", sequence);
    }

    public String generateExpenseVendorCode(UUID organizationId) {

        String sequenceName = "EXPENSE_VENDOR_" + organizationId;

        long sequence =
                sequenceGeneratorService.generateSequence(sequenceName);

        return "VEN" + String.format("%05d", sequence);
    }

    public String generateExpenseStatusCode(UUID organizationId) {

        String sequenceName = "EXPENSE_STATUS_" + organizationId;

        long sequence =
                sequenceGeneratorService.generateSequence(sequenceName);

        return "EST" + String.format("%05d", sequence);
    }

    public String generateExpenseCode(UUID organizationId) {

        String sequenceName = "EXPENSE_" + organizationId;

        long sequence =
                sequenceGeneratorService.generateSequence(sequenceName);

        return "EXP" + String.format("%06d", sequence);
    }

    public String generateExpenseAttachmentCode(UUID organizationId) {
        String sequenceName = "EXPENSE_ATTACHMENT_" + organizationId;
        long sequence = sequenceGeneratorService.generateSequence(sequenceName);
        return "ATT" + String.format("%05d", sequence);
    }
//Order Code generator
    public String generateOrderNumber(UUID branchId) {
        String sequenceName = "ORDER_" + branchId;
        long sequence = sequenceGeneratorService.generateSequence(sequenceName);
        return "ORD" + String.format("%06d", sequence);
    }

    public String generatePaymentCode(UUID organizationId) {
        String sequenceName = "ORDER_PAYMENT_" + organizationId;
        long sequence = sequenceGeneratorService.generateSequence(sequenceName);
        return "OPAY" + String.format("%06d", sequence);
    }

    public String generateOrderDiscountCode(UUID organizationId) {

        String sequenceName = "ORDER_DISCOUNT_" + organizationId;

        long sequence = sequenceGeneratorService.generateSequence(sequenceName);

        return "ODIS" + String.format("%06d", sequence);
    }

    public String generateTaxCode(UUID organizationId) {
        String sequenceName = "TAX_" + organizationId;
        long sequence = sequenceGeneratorService.generateSequence(sequenceName);
        return "TAX" + String.format("%05d", sequence);
    }

    public String generateKitchenTicketCode(UUID branchId) {
        long sequence = sequenceGeneratorService.generateSequence("KITCHEN_TICKET_" + branchId);
        return "KIT" + String.format("%06d", sequence);
    }

    public String generateSplitNumber() {
        return "SPLIT-" + System.currentTimeMillis();
    }

    public String generateCombinationNumber() {
        return "COMB-" + System.currentTimeMillis();
    }
}
