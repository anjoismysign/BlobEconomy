package io.github.anjoismysign.blobeconomy;

import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.bloblib.manager.BlobPlugin;
import io.github.anjoismysign.bloblib.manager.IManagerDirector;
import io.github.anjoismysign.bloblib.updater.PluginUpdater;
import org.jetbrains.annotations.NotNull;

public final class BlobEconomy extends BlobPlugin {
    private IManagerDirector proxy;
    private PluginUpdater updater;
    private BlobEconomyAPI api;

    @Override
    public void onEnable() {
        EconomyManagerDirector director = new EconomyManagerDirector(this);
        api = BlobEconomyAPI.getInstance(director);
        proxy = director.proxy();
        updater = generateGitHubUpdater("anjoismysign", "BlobEconomy");
    }

    @Override
    @NotNull
    public IManagerDirector getManagerDirector() {
        return proxy;
    }

    @Override
    @NotNull
    public PluginUpdater getPluginUpdater() {
        return updater;
    }

    @NotNull
    public BlobEconomyAPI getApi() {
        return api;
    }
}
