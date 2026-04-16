package com.zcxt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Security security = new Security();
    private final Crypto crypto = new Crypto();
    private final Qrcode qrcode = new Qrcode();
    private final Ai ai = new Ai();
    private final Bigdata bigdata = new Bigdata();

    public Security getSecurity() {
        return security;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    public Qrcode getQrcode() {
        return qrcode;
    }

    public Ai getAi() {
        return ai;
    }

    public Bigdata getBigdata() {
        return bigdata;
    }

    public static class Security {
        private final Jwt jwt = new Jwt();

        public Jwt getJwt() {
            return jwt;
        }

        public static class Jwt {
            private String issuer;
            private long ttlSeconds;
            private String secret;

            public String getIssuer() {
                return issuer;
            }

            public void setIssuer(String issuer) {
                this.issuer = issuer;
            }

            public long getTtlSeconds() {
                return ttlSeconds;
            }

            public void setTtlSeconds(long ttlSeconds) {
                this.ttlSeconds = ttlSeconds;
            }

            public String getSecret() {
                return secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
            }
        }
    }

    public static class Crypto {
        private String aesKey;

        public String getAesKey() {
            return aesKey;
        }

        public void setAesKey(String aesKey) {
            this.aesKey = aesKey;
        }
    }

    public static class Qrcode {
        private String baseUrl;
        private String storage;
        private String localDir;
        private final Minio minio = new Minio();

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getStorage() {
            return storage;
        }

        public void setStorage(String storage) {
            this.storage = storage;
        }

        public String getLocalDir() {
            return localDir;
        }

        public void setLocalDir(String localDir) {
            this.localDir = localDir;
        }

        public Minio getMinio() {
            return minio;
        }

        public static class Minio {
            private String endpoint;
            private String accessKey;
            private String secretKey;
            private String bucket;
            private String publicEndpoint;

            public String getEndpoint() {
                return endpoint;
            }

            public void setEndpoint(String endpoint) {
                this.endpoint = endpoint;
            }

            public String getAccessKey() {
                return accessKey;
            }

            public void setAccessKey(String accessKey) {
                this.accessKey = accessKey;
            }

            public String getSecretKey() {
                return secretKey;
            }

            public void setSecretKey(String secretKey) {
                this.secretKey = secretKey;
            }

            public String getBucket() {
                return bucket;
            }

            public void setBucket(String bucket) {
                this.bucket = bucket;
            }

            public String getPublicEndpoint() {
                return publicEndpoint;
            }

            public void setPublicEndpoint(String publicEndpoint) {
                this.publicEndpoint = publicEndpoint;
            }
        }
    }

    public static class Ai {
        private boolean enabled;
        private String baseUrl;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Bigdata {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
