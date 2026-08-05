package io.github.anjoismysign.blobeconomy.domain;

import org.jetbrains.annotations.NotNull;

/**
 * Describes a withdrawal that could not be fully covered by a player's wallet.
 * <p>
 * Passed to the handler registered through
 * {@link io.github.anjoismysign.blobeconomy.director.manager.BlobDepositorManager#setNotEnoughEvent},
 * which decides whether the {@code missing} amount can be covered (for example from the
 * bank wallet) and whether the withdrawal should proceed.
 *
 * @param owner    the depositor that lacks the funds
 * @param currency the currency key of the withdrawal
 * @param missing  the amount still needed on top of the wallet balance
 */
public record NotEnoughBalance(@NotNull BlobDepositor owner,
                               @NotNull String currency,
                               double missing) {

}