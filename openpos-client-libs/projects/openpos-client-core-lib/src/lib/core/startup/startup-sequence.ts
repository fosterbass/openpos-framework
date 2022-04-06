import { all, oneOf, optional, withSplashMessage } from './startup-task';
import { AudioStartupTask } from './tasks/audio.startup-task';
import { CapacitorHideStatusbarStartupTask } from './tasks/capacitor-hide-statusbar.startup-task';
import { InitializePersonalizationStartupTask } from './tasks/initialize-personalization.startup-task';
import { ManualPersonalizeStartupTask } from './tasks/manual-personalization.startup-task';
import { PlatformDiscoveryStartupTask } from './tasks/platform-discovery.startup-task';
import { PlatformPluginsStartupTask } from './tasks/platform-plugins.startup-task';
import { QueryParamsPersonalization } from './tasks/query-params-personalization.startup-task';
import { SavedSessionPersonalizationStartupTask } from './tasks/saved-session-personalization.startup-task';
import { SessionConnectStartupTask } from './tasks/session-connect.startup-task';
import { EnterpriseConfigStartupTask } from './tasks/enterprise-config-startup-task';
import { EnterpriseConfigPersonalizationStartupTask } from './tasks/enterprise-config-personalization.startup-task';
import { ZeroConfPersonalizationStartupTask } from './tasks/zeroconf/zero-conf-personalization.startup-task';

export let startupSequence = all(
    withSplashMessage('Configuring platform...', PlatformDiscoveryStartupTask),
    optional(CapacitorHideStatusbarStartupTask),
    withSplashMessage('Initializing Enterprise Config Service...', EnterpriseConfigStartupTask),
    InitializePersonalizationStartupTask,
    withSplashMessage(
        'Configuring Client Personalization...',
        oneOf(
            QueryParamsPersonalization,
            EnterpriseConfigPersonalizationStartupTask,
            withSplashMessage('Performing Service Discovery...', ZeroConfPersonalizationStartupTask),
            SavedSessionPersonalizationStartupTask,
            ManualPersonalizeStartupTask
        ),
    ),
    withSplashMessage(
        'Establishing Server Connection...',
        SessionConnectStartupTask
    ),
    withSplashMessage(
        'Configuring Client Plugins...',
        PlatformPluginsStartupTask
    ),
    withSplashMessage(
        'Initializing Audio...',
        AudioStartupTask
    )
);
