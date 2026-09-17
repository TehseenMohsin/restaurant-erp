package com.devmasters.restaurant_erp.config;

import com.devmasters.restaurant_erp.auth.domain.User;
import com.devmasters.restaurant_erp.auth.respository.UserRepository;
import com.devmasters.restaurant_erp.branch.domain.Branch;
import com.devmasters.restaurant_erp.branch.respository.BranchRepository;
import com.devmasters.restaurant_erp.common.enums.BillingCycle;
import com.devmasters.restaurant_erp.common.enums.PermissionAction;
import com.devmasters.restaurant_erp.organization.domain.Organization;
import com.devmasters.restaurant_erp.organization.respository.OrganizationRepository;
import com.devmasters.restaurant_erp.permission.domain.Permission;
import com.devmasters.restaurant_erp.permission.respository.PermissionRepository;

import com.devmasters.restaurant_erp.role.domain.Role;
import com.devmasters.restaurant_erp.role.respository.RoleRepository;
import com.devmasters.restaurant_erp.rolepermission.domain.RolePermission;
import com.devmasters.restaurant_erp.rolepermission.respository.RolePermissionRepository;
import com.devmasters.restaurant_erp.subscriptionplan.domain.SubscriptionPlan;
import com.devmasters.restaurant_erp.subscriptionplan.respository.SubscriptionPlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApplicationInitializer implements CommandLineRunner {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final OrganizationRepository organizationRepository;
    private final BranchRepository branchRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String developerEmail = "tehseen7735@gmail.com";

        if (userRepository.existsByEmail(developerEmail)) {
            return;
        }

        System.out.println("=================================");
        System.out.println("INITIALIZING Restaurant APPLICATION");
        System.out.println("=================================");

        /*
         * STEP 1
         * Create Subscription Plan
         */
        SubscriptionPlan subscriptionPlan =
                subscriptionPlanRepository.save(
                        SubscriptionPlan.builder()
                                .id(UUID.randomUUID())
                                .name("Enterprise")
                                .branchesLimit(999)
                                .usersLimit(999)
                                .menuItemsLimit(99999)
                                .ordersPerMonth(99999)
                                .monthlyPrice(0.0)
                                .yearlyPrice(0.0)
                                .isActive(true)
                                .build()
                );

        /*
         * STEP 2
         * Create Organization
         */
        Organization organization =
                organizationRepository.save(
                        Organization.builder()
                                .id(UUID.randomUUID())
                                .organizationName("TM DEVOP")
                                .ownerName("Tehseen Mohsin")
                                .contactNumber("+92 300-4654714")
                                .email("tehseen7735@gmail.com")
                                .address("DHA Lahore")
                                .city("Lahore")
                                .country("Pakistan")
                                .isActive(true)
                                .subscriptionPlan(subscriptionPlan)
                                .billingCycle(BillingCycle.YEARLY)
                                .subscriptionStartDate(LocalDate.now())
                                .subscriptionEndDate(LocalDate.now().plusYears(1))
                                .build()
                );

        /*
         * STEP 3
         * Create Branch
         */
        Branch branch =
                branchRepository.save(
                        Branch.builder()
                                .id(UUID.randomUUID())
                                .branchName("Head Office")
                                .branchCode("HO-001")
                                .address("Main Office")
                                .city("DHA Lahore")
                                .phone("+92 300 4654714")
                                .organization(organization)
                                .isActive(true)
                                .build()
                );

        /*
         * STEP 4
         * Create Developer Role
         */
        Role developerRole =
                roleRepository.save(
                        Role.builder()
                                .id(UUID.randomUUID())
                                .roleName("DEVELOPER")
                                .description("System Developer With Full Access")
                                .organization(organization)
                                .isActive(true)
                                .build()
                );
        List<String> permissionModules = List.of(
                "Plan",
                "Organization",
                "Branch",
                "Employee",
                "Role",
                "Permission",
                "Customer",
                "Supplier",
                "Category",
                "Floor",
                "Modifier Group",
                "Modifier",
                "Expense",
                "Table",
                "Menu",
                "Settings"
        );

        List<Permission> permissions = new ArrayList<>();
        for(String permissionModule : permissionModules){
            create(permissionModule,permissions);
        }
        permissionRepository.saveAll(permissions);

        List<RolePermission> rolePermissions = new ArrayList<>();
        for(Permission permission : permissions){
            rolePermissions.add(RolePermission.builder().id(UUID.randomUUID())
                    .role(developerRole).permission(permission).build());
        }

        rolePermissionRepository.saveAll(rolePermissions);

        /*
         * STEP 5
         * Create Developer User
         */
        User developer =
                User.builder()
                        .id(UUID.randomUUID())
                        .username("developer")
                        .password(passwordEncoder.encode("Developer123"))
                        .fullName("Tehseen Mohsin")
                        .email(developerEmail)
                        .phone("+92 300 4654714")
                        .isActive(true)
                        .organization(organization)
                        .branch(branch)
                        .role(developerRole)
                        .build();

        userRepository.save(developer);

        System.out.println("=================================");
        System.out.println("RESTAURANT ERP INITIALIZATION COMPLETED");
        System.out.println("=================================");
        System.out.println("Username : developer");
        System.out.println("Email    : TEHSEEN7735@GMAIL.COM");
        System.out.println("Password : Developer123");
        System.out.println("Organization : " + organization.getOrganizationName());
        System.out.println("Branch       : " + branch.getBranchName());
        System.out.println("=================================");
    }

    private Permission permission(String code, String name, String module) {

        return Permission.builder()
                .id(UUID.randomUUID())
                .code(code)
                .name(name)
                .module(module)
                .build();
    }


    public void create(String module, List<Permission> permissions) {

        String moduleCode = module.trim().toUpperCase();
        String moduleName = toSentenceCase(module);

        for (PermissionAction permissionValue : PermissionAction.values()){
            permissions.add(Permission.builder()
                    .id(UUID.randomUUID())
                    .module(moduleCode)
                    .code(moduleCode + "_" + permissionValue.name())
                    .name(moduleName + " " + permissionValue.getDisplayName())
                    .isActive(true)
                    .build());
        }
    }

    private String toSentenceCase(String text) {

        if (text == null || text.isBlank())
            return text;
        text = text.trim();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
    }
}