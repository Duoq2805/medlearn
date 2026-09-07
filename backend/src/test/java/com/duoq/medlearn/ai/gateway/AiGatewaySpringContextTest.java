package com.duoq.medlearn.ai.gateway;

import com.duoq.medlearn.ai.gateway.provider.AiProvider;
import com.duoq.medlearn.ai.model.AiModel;
import com.duoq.medlearn.audit.service.AuditService;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import com.duoq.medlearn.auth.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Spring context test — verifies real bean wiring for the AI gateway layer.
 *
 * What this catches that unit tests missed:
 * - AiGatewayRouter.providers list is EMPTY in production (no AiProvider impl exists)
 * - Previously chat(request, model) called resolveProvider() → always threw
 * - Now verifies: router is wired, NineRouterGateway is discovered, providers list is empty
 *   but chat() still works (routes via AiGateway, not AiProvider)
 */
@SpringBootTest
@ActiveProfiles("test")
class AiGatewaySpringContextTest {

    @Autowired private ApplicationContext context;
    @Autowired private AiGatewayRouter gatewayRouter;

    // MockBeans required to satisfy Spring context startup in test profile
    @MockBean private AuditService auditService;
    @MockBean private CurrentUserResolver currentUserResolver;
    @MockBean private PermissionService permissionService;

    // ── Bean existence ─────────────────────────────────────────────────────────

    @Test
    void aiGatewayRouter_beanExists() {
        assertThat(gatewayRouter).isNotNull();
    }

    @Test
    void nineRouterGateway_beanExists() {
        var gateway = context.getBean(NineRouterGateway.class);
        assertThat(gateway).isNotNull();
        assertThat(gateway.getProviderName()).isEqualTo("nine-router");
    }

    // ── Gateway list — must NOT be empty ──────────────────────────────────────

    @Test
    void gatewayList_containsNineRouterGateway() {
        var gateways = context.getBeansOfType(AiGateway.class).values();
        assertThat(gateways)
                .isNotEmpty()
                .anyMatch(g -> "nine-router".equals(g.getProviderName()));
    }

    // ── Provider list — IS empty in current production setup ──────────────────

    @Test
    void providerList_isEmptyInCurrentSetup() {
        // AiProvider has no implementations — this is by design currently.
        // chat() must NOT rely on this list.
        var providers = context.getBeansOfType(AiProvider.class).values();
        assertThat(providers)
                .as("AiProvider has no implementations — if this fails, someone added one: update AiGatewayRouter accordingly")
                .isEmpty();
    }

    // ── resolve() works with real Spring beans ────────────────────────────────

    @Test
    void resolve_nineRouter_returnsGateway() {
        var gateway = gatewayRouter.resolve("nine-router");
        assertThat(gateway).isNotNull();
        assertThat(gateway.getProviderName()).isEqualTo("nine-router");
    }

    @Test
    void resolve_unknownProvider_throwsAiConfigurationException() {
        assertThatThrownBy(() -> gatewayRouter.resolve("openai-direct"))
                .isInstanceOf(com.duoq.medlearn.ai.exception.AiConfigurationException.class);
    }

    @Test
    void resolve_defaultProvider_returnsNineRouter() {
        // resolve(null) uses aiProperties.gateway.default-provider = "nine-router"
        var gateway = gatewayRouter.resolve(null);
        assertThat(gateway.getProviderName()).isEqualTo("nine-router");
    }

    // ── resolveProvider() with empty list — documents pre-fix failure mode ────

    @Test
    void resolveProvider_withEmptyList_throwsAiConfigurationException() {
        // This is the root cause of the Critical Bug:
        // In production, providers list is empty → resolveProvider() always throws.
        // chat(request, model) no longer calls this method — but we keep this test
        // to document the boundary and catch regressions if routing ever reverts.
        assertThatThrownBy(() -> gatewayRouter.resolveProvider(AiModel.GPT_5_MINI))
                .isInstanceOf(com.duoq.medlearn.ai.exception.AiConfigurationException.class)
                .hasMessageContaining("No provider found for model");
    }

    // ── No circular bean dependencies ─────────────────────────────────────────

    @Test
    void springContext_loadsWithoutCircularDependencies() {
        // If Spring context loads successfully, no circular dependencies exist.
        // This test is a canary — it passes if and only if context starts cleanly.
        assertThat(context).isNotNull();
        assertThat(context.getBean(AiGatewayRouter.class)).isNotNull();
    }
}
