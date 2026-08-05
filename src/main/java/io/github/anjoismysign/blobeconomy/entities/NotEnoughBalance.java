package io.github.anjoismysign.blobeconomy.entities;

import org.jetbrains.annotations.NotNull;

public record NotEnoughBalance(@NotNull BlobDepositor owner,
                               @NotNull String currency,
                               double missing) {

}