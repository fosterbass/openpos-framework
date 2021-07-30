package org.jumpmind.pos.core.audio;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.core.content.ContentProviderService;
import org.jumpmind.pos.core.flow.IStateManager;
import org.jumpmind.pos.util.ResourceUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        //Audio will still work if this fails. We just want to try our best.
        try {
            resources = ResourceUtils.getContentResources(AudioUtil.AUDIO_CONTENT_ROOT + "**/*.*");
            Arrays.stream(resources)
                    .filter(resource -> resource.getFilename() != null && !resource.getFilename().equals(AUDIO_LICENSE))
                    .forEach(resource -> {
                        String key = null;
                        try {
                            key = tryToGetAudioKey(resource, audioConfig);
                        } catch (Exception e) {
                            log.info("Unable to load audio content for resource: " + resource.getFilename(), e);
                        }
                        if (key != null) {
                            contentKeys.add(key);
                        }
                    });
        } catch (Exception e) {
            log.info("Unable to load audio content resources", e);
        }

        return contentKeys;
    }
    
    private static void createFileSystem(URI uri) throws IOException {
        Map<String, String> env = new HashMap<>(); 
        env.put("create", "true");
        FileSystems.newFileSystem(uri, env);
    }
    
    private static String tryToGetAudioKey(Resource resource, AudioConfig config) throws Exception {
        String key = "";
        URL url = resource.getURL();
        String path = url.getPath();
        key = path.substring(path.indexOf(AUDIO_CONTENT_ROOT), path.indexOf(resource.getFilename()));
        key = key.replace(AUDIO_CONTENT_ROOT, "");
        return key;
    }

    public static List<String> getAllContentUrls(IStateManager stateManager, AudioConfig audioConfig) {
        ContentProviderService contentProviderService = tryToGetContentProviderService(stateManager);
        if (contentProviderService != null) {
            return getAllContentKeys(audioConfig).stream().map(contentKey -> contentProviderService
                    .resolveContent(stateManager.getDeviceId(), AUDIO_CONTENT_ROOT + contentKey))
                    .collect(Collectors.toList());
        } else {
            log.info("Unable to get ContentProviderService. Could not get Audio keys.");
            return new ArrayList<>();
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
