package io.github.anjoismysign.blobeconomy.director;

import io.github.anjoismysign.blobeconomy.BlobEconomy;
import io.github.anjoismysign.bloblib.entities.GenericManager;

public class EconomyManager extends GenericManager<BlobEconomy, EconomyManagerDirector> {

    public EconomyManager(EconomyManagerDirector managerDirector) {
        super(managerDirector);
    }
}