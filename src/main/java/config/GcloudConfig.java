/**
 * This class holds configuration settings for Google Cloud services.
 * It includes properties like bucket name, object path, duration, and file URLs.
 *
 * Provides getter and setter methods for managing these properties.
 */
package config;

public class GcloudConfig {

    private String bucket_name;
    private String private_object_path;
    private String duration;
    private String public_file_url;
    private String junk_file_url;

    public String getBucket_name() {
        return bucket_name;
    }

    public void setBucket_name(String bucket_name) {
        this.bucket_name = bucket_name;
    }

    public String getPrivate_object_path() {
        return private_object_path;
    }

    public void setPrivate_object_path(String private_object_path) {
        this.private_object_path = private_object_path;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPublic_file_url() {
        return public_file_url;
    }

    public void setPublic_file_url(String public_file_url) {
        this.public_file_url = public_file_url;
    }

    public String getJunk_file_url() {
        return junk_file_url;
    }

    public void setJunk_file_url(String junk_file_url) {
        this.junk_file_url = junk_file_url;
    }
}
