package us.mytheria.blobeconomy;

import org.jetbrains.annotations.NotNull;
import us.mytheria.blobeconomy.director.EconomyManagerDirector;
import us.mytheria.bloblib.entities.PluginUpdater;
import us.mytheria.bloblib.managers.BlobPlugin;
import us.mytheria.bloblib.managers.IManagerDirector;

public final class BlobEconomy extends BlobPlugin {
    private IManagerDirector proxy;
    private PluginUpdater updater;

    @Override
    public void onEnable() {
        EconomyManagerDirector director = new EconomyManagerDirector(this);
        proxy = director.proxy();
        updater = generateGitHubUpdater("anjoismysign", "BlobEconomy");
    }

    @Override
    public IManagerDirector getManagerDirector() {
        return proxy;
    }

    @Override
    @NotNull
    public PluginUpdater getPluginUpdater() {
        return updater;
    }
}
