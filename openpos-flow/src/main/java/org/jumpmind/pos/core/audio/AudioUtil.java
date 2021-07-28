package org.jumpmind.pos.core.audio;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.core.content.ContentProviderService;
import org.jumpmind.pos.core.flow.IStateManager;
import org.jumpmind.pos.util.ResourceUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public final class AudioUtil {
    public static final String AUDIO_LICENSE = "licenses.csv";
    public static final String AUDIO_CONTENT_ROOT = "audio/";
    protected static final int CONTENT_PROVIDER_RETRY_LIMIT = 45;

    public static AudioConfigMessage getInteractionMessageFromConfig(IStateManager stateManager, AudioConfig config) {
        AudioConfig configCopy = (AudioConfig) config.clone();

        if (configCopy.getInteractions() != null && configCopy.getInteractions().getMouse() != null) {
            setProviderUrl(stateManager, configCopy.getInteractions().getMouse().getMouseDown());
            setProviderUrl(stateManager, configCopy.getInteractions().getMouse().getMouseUp());
        }

        if (configCopy.getInteractions() != null && configCopy.getInteractions().getDialog() != null) {
            setProviderUrl(stateManager, configCopy.getInteractions().getDialog().getOpening());
            setProviderUrl(stateManager, configCopy.getInteractions().getDialog().getClosing());
        }

        return new AudioConfigMessage(configCopy);
    }

    public static void setProviderUrl(IStateManager stateManager, AudioRequest request) {
        if (request == null) {
            return;
        }

        ContentProviderService contentProviderService = tryToGetContentProviderService(stateManager);
        
        if (contentProviderService != null) {
            String audioKey = getKey(request.getSound());
            String soundUrl = contentProviderService.resolveContent(stateManager.getDeviceId(), audioKey);
            request.setUrl(soundUrl);
        } else {
            log.info("Unable to get ContentProviderService. Could not get url for audio key \'" + getKey(request.getSound()) + "\'");
            request.setUrl(null);
        }
    }

    public static String getKey(String sound) {
        return AUDIO_CONTENT_ROOT + sound;
    }

    public static List<String> getAllContentKeys(AudioConfig audioConfig) {
        Resource[] resources;
        ArrayList<String> contentKeys = new ArrayList<>();

        try {
            resources = ResourceUtils.getContentResources(AudioUtil.AUDIO_CONTENT_ROOT + "**/*.*");
            Arrays.stream(resources)
                    .filter(resource -> !resource.getFilename().equals(AUDIO_LICENSE))
                    .forEach(resource -> {
                        try {
                            Path resourcePath = Paths.get(resource.getURI());
                            // The parent folder of each file is the content key
                            String parentFolder = resourcePath.getParent().getFileName().toString();
                            if (audioConfig.getSupportedLocales() != null && audioConfig.getSupportedLocales().contains(parentFolder)) {
                                parentFolder = resourcePath.getParent().getParent().getFileName().toString() + "/" + parentFolder;
                            }
                            contentKeys.add(parentFolder);
                        } catch (IOException e) {
                            log.info("Unable to load audio content resources", e);
                        }
                    });
        } catch (IOException e) {
            log.info("Unable to load audio content resources", e);
        }

        return contentKeys;
    }

    public static List<String> getAllContentUrls(IStateManager stateManager, AudioConfig audioConfig) {

        ContentProviderService contentProviderService = tryToGetContentProviderService(stateManager);
        if (contentProviderService != null) {
            return getAllContentKeys(audioConfig).stream().map(contentKey -> contentProviderService
                    .resolveContent(stateManager.getDeviceId(), AUDIO_CONTENT_ROOT + contentKey))
                    .collect(Collectors.toList());
        } else {
            log.info("Unable to get ContentProviderService. Could not get Audio keys.");
            return null;
        }
    }
    
    protected static ContentProviderService tryToGetContentProviderService(IStateManager stateManager) {
        ContentProviderService contentProviderService = stateManager.getApplicationState().getScopeValue("contentProviderService");

        int retry = 0;
        while (contentProviderService == null && retry <= CONTENT_PROVIDER_RETRY_LIMIT) {
            retry++;
            try {
                log.debug("ContentProviderService may not have been created yet. Retrying.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            contentProviderService = stateManager.getApplicationState().getScopeValue("contentProviderService");
        }
        return contentProviderService;
    }
}
