package org.jumpmind.pos.update.provider.google_cloud_storage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.update.provider.NamedFilesSoftwareProvider;
import org.jumpmind.pos.update.versioning.PackageVersioning;
import org.jumpmind.pos.update.versioning.Version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class GoogleCloudStorageSoftwareProvider extends NamedFilesSoftwareProvider {

    Storage storageClient;
    String bucketName;
    
    private String tempDirectory;

    public GoogleCloudStorageSoftwareProvider(PackageVersioning versioning, GoogleCloudStorageSoftwareProviderConfiguration config) {
        super(config.getFileNamePattern(), config.isFileNamePatternIgnoreCase(), versioning);
        bucketName = config.getBucketName();

        try {
            tempDirectory = Files.createTempDirectory("jmcUpdates").toFile().getAbsolutePath();
        } catch (IOException e) {
            log.error("could not create temporary storage for google cloud storage downloads", e);
        }

        storageClient = StorageOptions.getDefaultInstance().getService();
    }
    
    @Override
    public List<Version> getAvailableVersions() {
        List<Version> versions = new ArrayList<>();
        Page<Blob> objects = storageClient.list(bucketName);
        // TODO we need a plan to either clean up the old stale versions or to only fetch the top x cause this will probably get un reasonable to fetch everything.
        objects.getValues().forEach(blob -> {
           Version v = getVersionOfFile(blob.getName());
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

        for(Blob b: objects.iterateAll()) {
            Version versionFromBlob = getVersionOfFile(b.getName());
            if(versionFromBlob != null && versionFromBlob.versionEquals(version)){
                Path p = Paths.get(tempDirectory, b.getName());
                b.downloadTo(p);
                return p;
            }
        }

        return null;
    }
}
