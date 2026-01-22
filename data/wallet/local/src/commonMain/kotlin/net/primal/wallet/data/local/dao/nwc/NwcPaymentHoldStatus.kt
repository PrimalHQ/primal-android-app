package net.primal.wallet.data.local.dao.nwc

import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldStatus.COMMITTED
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldStatus.EXPIRED
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldStatus.PENDING
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldStatus.PROCESSING
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldStatus.RELEASED

/**
 * Lifecycle status of an NWC payment hold.
 *
 * When an external app requests a payment via NWC, budget is "held" to prevent
 * concurrent requests from exceeding the daily limit. This enum tracks that hold's lifecycle.
 *
 * ## State Transitions
 * ```
 *                    ┌─────────────────┐
 *                    │                 │
 *          ┌─────────▼──────┐          │
 *   create │    PENDING     │ timeout  │
 *          └───────┬────────┘──────────┼───► EXPIRED
 *                  │                   │
 *          (future)│                   │
 *                  ▼                   │
 *          ┌───────────────┐           │
 *          │  PROCESSING   │───────────┘
 *          └───────┬───────┘
 *                  │
 *       ┌──────────┴──────────┐
 *       │ success             │ failure
 *       ▼                     ▼
 * ┌───────────┐         ┌───────────┐
 * │ COMMITTED │         │ RELEASED  │
 * └───────────┘         └───────────┘
 * ```
 *
 * ## Status Descriptions
 * - [PENDING]: Hold placed, waiting to process payment
 * - [PROCESSING]: Payment actively in flight (reserved for future use)
 * - [COMMITTED]: Payment succeeded; hold converted to confirmed spend
 * - [RELEASED]: Payment failed/cancelled; budget freed
 * - [EXPIRED]: Hold timed out; budget freed automatically
 *
 * @see NwcPaymentHoldData
 * @see NwcDailyBudgetData
 */
enum class NwcPaymentHoldStatus {
    PENDING,
    PROCESSING,
    COMMITTED,
    RELEASED,
    EXPIRED,
}
