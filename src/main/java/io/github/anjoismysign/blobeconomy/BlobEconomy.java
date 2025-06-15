package io.github.anjoismysign.blobeconomy;

import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.blobeconomy.director.EconomyManagerDirector;
import io.github.anjoismysign.bloblib.entities.PluginUpdater;
import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.bloblib.managers.IManagerDirector;

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
