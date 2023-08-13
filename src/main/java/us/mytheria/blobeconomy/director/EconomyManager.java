package us.mytheria.blobeconomy.director;

import us.mytheria.blobeconomy.BlobEconomy;
import us.mytheria.bloblib.entities.GenericManager;

public class EconomyManager extends GenericManager<BlobEconomy, EconomyManagerDirector> {

    public EconomyManager(EconomyManagerDirector managerDirector) {
        super(managerDirector);
    }
}