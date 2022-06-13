package org.jumpmind.pos.service.init;

/**
 * An interface for a {@link org.springframework.context.annotation.Bean} type to implement that provides a way for the
 * framework to inquire about the initialization status of a module from the module itself. This is useful in the
 * scenarios where a module perform asynchronous loading of some data.
 *
 * <p>
 * Despite its name suggesting so, there is no limitation to the number of {@link IModuleStatusProvider} beans that are
 * provided by a single module, however this scenario is not common.
 */
public interface IModuleStatusProvider {

    /**
     * Gets a name that can be displayed to a user.
     */
    String getDisplayName();

    /**
     * Gets the current status of the provider.
     */
    ModuleInitStatus getCurrentStatus();
}
