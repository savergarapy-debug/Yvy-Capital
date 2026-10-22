package com.example.data.model

enum class PlanOrigin {
    STANDARD,  // Usuario regular / cliente
    PURCHASED, // Membresía adquirida de asesor (Inicial, Intermedio, Senior)
    STAFF      // Asignación directa por pertenecer al personal activo de Yvy Capital
}

/**
 * Representa el plan efectivo de un usuario en toda la aplicación.
 * Resuelve de forma centralizada:
 * 1. Si el usuario es personal activo -> PLAN_SENIOR (STAFF)
 * 2. Si memberships/{uid} tiene status == 'active' y expiresAt es null o futuro -> ese planTier (PURCHASED)
 * 3. En cualquier otro caso -> FREE (STANDARD)
 */
data class EffectivePlan(
    val tier: String = AdvisorMemberships.TIER_FREE,
    val origin: PlanOrigin = PlanOrigin.STANDARD,
    val isStaff: Boolean = (origin == PlanOrigin.STAFF)
) {
    val isSenior: Boolean get() = isStaff || tier == AdvisorMemberships.TIER_SENIOR
    val canPublish: Boolean get() = isStaff || tier != AdvisorMemberships.TIER_FREE
    val maxActiveListings: Int get() = if (isStaff) 10 else AdvisorMemberships.maxListingsForTier(tier)
    val canFeatureListings: Boolean get() = if (isStaff) true else AdvisorMemberships.canFeatureForTier(tier)
    val canGenerateVideos: Boolean get() = isStaff || AdvisorMemberships.isSeniorTier(tier)
    val hasDirectCollaboration: Boolean get() = isStaff || AdvisorMemberships.isSeniorTier(tier)
    val supportsExternalAgentMode: Boolean get() = if (isStaff) false else (AdvisorMemberships.getSpec(tier)?.supportsExternalAgentMode ?: false)
    val participationPercent: String get() = if (isStaff) "4,5 %" else (AdvisorMemberships.getSpec(tier)?.advisorParticipationPercent ?: "—")

    val badgeLabel: String get() = when {
        isStaff -> "Plan Senior · Personal Yvy Capital"
        tier == AdvisorMemberships.TIER_SENIOR -> "Plan Senior"
        tier == AdvisorMemberships.TIER_INTERMEDIO -> "Plan Intermedio"
        tier == AdvisorMemberships.TIER_INICIAL -> "Plan Inicial"
        else -> "Cuenta Estándar"
    }

    val displayPrice: String get() = when {
        isStaff -> "Bonificado (Personal Yvy Capital)"
        tier == AdvisorMemberships.TIER_SENIOR -> "G. 249.000 mensuales"
        tier == AdvisorMemberships.TIER_INTERMEDIO -> "G. 189.000 mensuales"
        tier == AdvisorMemberships.TIER_INICIAL -> "G. 99.000 mensuales"
        else -> "Gratis"
    }

    companion object {
        fun resolveEffectivePlan(
            isActiveStaff: Boolean,
            membership: UserMembership?
        ): EffectivePlan {
            if (isActiveStaff) {
                return EffectivePlan(
                    tier = AdvisorMemberships.TIER_SENIOR,
                    origin = PlanOrigin.STAFF,
                    isStaff = true
                )
            }
            if (membership != null && membership.isActive && membership.planTier != AdvisorMemberships.TIER_FREE) {
                return EffectivePlan(
                    tier = membership.planTier,
                    origin = PlanOrigin.PURCHASED,
                    isStaff = false
                )
            }
            return EffectivePlan(
                tier = AdvisorMemberships.TIER_FREE,
                origin = PlanOrigin.STANDARD,
                isStaff = false
            )
        }

        fun resolveEffectivePlan(
            isActiveStaff: Boolean,
            userProfile: UserProfile?,
            membership: UserMembership? = null
        ): EffectivePlan {
            return resolveEffectivePlan(isActiveStaff, membership)
        }
    }
}
