package us.mytheria.blobeconomy.director.manager;

import us.mytheria.blobeconomy.director.EconomyManager;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;

public class ListenerManager extends EconomyManager {

    public ListenerManager(EconomyManagerDirector managerDirector) {
        super(managerDirector);
    }

    @Override
    public void loadInConstructor() {
        //logics
    }
}