package crio.vicara;

public class StorageServiceDetails {
    private String name;
    private String websiteUrl;
    private String logoUrl;
    private String hyphenatedName;


    private StorageServiceDetails() {
    }

    public String getName() {
        return name;
    }

    public String getHyphenatedName() {
        return hyphenatedName;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public static StorageServiceDetails with() {
        return new StorageServiceDetails();
    }

    public StorageServiceDetails name(String name) {
        this.name = name;
        return this;
    }

    public StorageServiceDetails websiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
        return this;
    }

    public StorageServiceDetails logoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
        return this;
    }

    public StorageServiceDetails hyphenatedName(String hyphenatedName) {
        this.hyphenatedName = hyphenatedName;
        return this;
    }
}
