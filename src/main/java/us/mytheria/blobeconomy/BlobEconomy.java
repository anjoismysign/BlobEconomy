package us.mytheria.blobeconomy;

import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.bloblib.managers.BlobPlugin;
import us.mytheria.bloblib.managers.IManagerDirector;

public final class BlobEconomy extends BlobPlugin {
    private IManagerDirector proxy;

    @Override
    public void onEnable() {
        EconomyManagerDirector director = new EconomyManagerDirector(this);
        proxy = director.proxy();
    }

    @Override
    public IManagerDirector getManagerDirector() {
        return proxy;
    }
}
