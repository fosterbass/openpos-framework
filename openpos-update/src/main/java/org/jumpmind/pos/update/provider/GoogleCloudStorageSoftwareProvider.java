package org.jumpmind.pos.update.provider;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.update.UpdateModule;
import org.jumpmind.pos.update.versioning.Version;
import org.jumpmind.pos.update.versioning.Versioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Profile(UpdateModule.NAME)
@Component
@Slf4j
public class GoogleCloudStorageSoftwareProvider implements ISoftwareProvider {

    Storage storageClient;
    
    @Value("${openpos.update.googleCloudStorageSoftwareProvider.bucketName}")
    String bucketName;

    @Value("${openpos.update.googleCloudStorageSoftwareProvider.namePattern}")
    String fileNamePattern;

    @Value("${openpos.update.googleCloudStorageSoftwareProvider.namePatternIgnoreCase:false}")
    boolean fileNamePatternIgnoresCase;

    @Autowired
    Versioning versionFactory;
    
    private String tempDirectory;

    @PostConstruct
    public void init(){
        try {
            tempDirectory = Files.createTempDirectory("jmcUpdates").toFile().getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        storageClient = StorageOptions.getDefaultInstance().getService();
    }
    
    private Pattern compiledFileNamePattern;
    
    @Override
    public List<Version> getAvailableVersions() {
        List<Version> versions = new ArrayList<>();
        Page<Blob> objects = storageClient.list(bucketName);
        // TODO we need a plan to either clean up the old stale versions or to only fetch the top x cause this will probably get un reasonable to fetch everything.
        objects.getValues().forEach(blob -> {
           Version v = getVersionFromBlob(blob);
           if(v != null){
               versions.add(v);
           }
        });

        return versions;
    }

    @Override
    public Version getLatestVersion() {
        return getAvailableVersions().stream().max(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public Path getSoftwareVersion(Version version) {
        Page<Blob> objects = storageClient.list(bucketName);
        for(Blob b: objects.iterateAll()){
            if( getVersionFromBlob(b).equals(version)){
                Path p = Paths.get(tempDirectory, b.getName());
                b.downloadTo(p);
                return p;
            }
        }

        return null;
    }
    
    private Version getVersionFromBlob(Blob blob){
        Version resultingVersion = null;

        if (StringUtils.isNotEmpty(fileNamePattern)) {
            if (compiledFileNamePattern == null) {
                int flags = 0;

                if (fileNamePatternIgnoresCase) {
                    flags |= Pattern.CASE_INSENSITIVE;
                }

                compiledFileNamePattern = Pattern.compile(fileNamePattern, flags);
            }

            final Matcher matcher = compiledFileNamePattern.matcher(blob.getName());
            if (matcher.find()) {
                final String versionString = matcher.group("version");

                if (versionString != null) {
                    try {
                        resultingVersion = versionFactory.fromString(versionString);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } else {
            // assume the file name is named after the version number.
            resultingVersion = versionFactory.fromString(blob.getName()));
        }
        
        return resultingVersion;
    }
}
