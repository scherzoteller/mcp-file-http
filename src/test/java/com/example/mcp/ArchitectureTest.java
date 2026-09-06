package com.example.mcp;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Tests d'architecture pour vérifier les règles d'accès entre couches
 * selon l'architecture hexagonale définie dans .clinerules/architecture.md
 *
 * Utilise l'API Layers d'ArchUnit pour définir les couches et leurs dépendances autorisées.
 */
class ArchitectureTest {

    // Package bases
    private static final String DOMAIN_PACKAGE = "com.example.mcp.domain..";
    private static final String APPLICATION_PACKAGE = "com.example.mcp.application..";
    private static final String EXPOSITION_PACKAGE = "com.example.mcp.exposition..";
    private static final String INFRASTRUCTURE_PACKAGE = "com.example.mcp.infrastructure..";

    private final JavaClasses classes = new ClassFileImporter().importPackages(
        "com.example.mcp.domain",
        "com.example.mcp.application",
        "com.example.mcp.exposition",
        "com.example.mcp.infrastructure"
    );

    @Test
    void hexagonalArchitectureRules() {
        // Définir les couches avec leurs dépendances autorisées
        Architectures.LayeredArchitecture architecture = layeredArchitecture()
                .consideringOnlyDependenciesInLayers();
        architecture
            .layer("domain").definedBy(DOMAIN_PACKAGE)
            .layer("application").definedBy(APPLICATION_PACKAGE)
            .layer("exposition").definedBy(EXPOSITION_PACKAGE)
            .layer("infrastructure").definedBy(INFRASTRUCTURE_PACKAGE);

        architecture
            // Domain layer - innermost layer, should not depend on other application layers
            .whereLayer("domain").mayNotAccessAnyLayer()

            // Application layer can only depend on domain
            .whereLayer("application").mayOnlyAccessLayers("domain")

            // Exposition layer can depend on domain and application
            .whereLayer("exposition").mayOnlyAccessLayers("domain", "application")

            // Infrastructure layer can depend on domain and application
            .whereLayer("infrastructure").mayOnlyAccessLayers("domain", "application");

        architecture.check(classes);
    }

    @Test
    void domainLayerShouldNotDependOnSpringBoot() {
        noClasses()
            .that().resideInAPackage(DOMAIN_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage("org.springframework.boot")
            .because("Le domaine ne doit pas dépendre de Spring Boot")
            .check(classes);
    }

    @Test
    void domainLayerShouldNotDependOnSpringAI() {
        noClasses()
            .that().resideInAPackage(DOMAIN_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage("org.springframework.ai")
            .because("Le domaine ne doit pas dépendre de Spring AI")
            .check(classes);
    }

    @Test
    void domainLayerShouldNotDependOnLombok() {
        noClasses()
            .that().resideInAPackage(DOMAIN_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage("lombok")
            .because("Le domaine ne doit pas dépendre de Lombok")
            .check(classes);
    }

    @Test
    void domainLayerShouldNotDependOnMapStruct() {
        noClasses()
            .that().resideInAPackage(DOMAIN_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage("org.mapstruct")
            .because("Le domaine ne doit pas dépendre de MapStruct")
            .check(classes);
    }

    @Test
    void domainLayerShouldNotDependOnJPA() {
        noClasses()
            .that().resideInAPackage(DOMAIN_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage("javax.persistence")
            .because("Le domaine ne doit pas dépendre de JPA")
            .check(classes);
    }

    @Test
    void domainPortsInterfacesShouldBeNamedWithPortSuffix() {
        ArchRule rule = classes()
            .that().resideInAPackage(DOMAIN_PACKAGE + "ports..")
            .and().areInterfaces()
            .should().haveNameMatching(".*Port");
        rule.check(classes);
    }

    @Test
    void infrastructureAdaptersShouldBeNamedWithAdapterSuffix() {
        ArchRule rule = classes()
            .that().resideInAPackage(INFRASTRUCTURE_PACKAGE + "adapter..")
            .and().areNotInterfaces()
            .should().haveNameMatching(".*Adapter");
        rule.check(classes);
    }
}