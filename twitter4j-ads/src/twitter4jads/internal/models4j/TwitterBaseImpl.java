/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4jads.internal.models4j;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import twitter4jads.auth.*;
import twitter4jads.conf.Configuration;
import twitter4jads.internal.http.*;
import twitter4jads.internal.json.z_T4JInternalFactory;
import twitter4jads.internal.json.z_T4JInternalJSONImplFactory;
import twitter4jads.models.ads.HttpVerb;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static twitter4jads.internal.http.HttpResponseCode.ENHANCE_YOUR_CLAIM;
import static twitter4jads.internal.http.HttpResponseCode.SERVICE_UNAVAILABLE;

/**
 * Base class of Twitter / AsyncTwitter / TwitterStream supports OAuth.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
abstract class TwitterBaseImpl implements TwitterBase, Serializable, OAuthSupport, HttpResponseListener {
    protected Configuration conf;
    protected transient String screenName = null;
    protected transient long id = 0;

    protected transient HttpClientWrapper http;
    private List<RateLimitStatusListener> rateLimitStatusListeners = new ArrayList<RateLimitStatusListener>(0);

    protected z_T4JInternalFactory factory;

    protected Authorization auth;
    private static final long serialVersionUID = -3812176145960812140L;

    /*package*/ TwitterBaseImpl(Configuration conf, Authorization auth) {
        this.conf = conf;
        this.auth = auth;
        init();
    }

    private void init() {
        if (null == auth) {
            // try to populate OAuthAuthorization if available in the configuration
            String consumerKey = conf.getOAuthConsumerKey();
            String consumerSecret = conf.getOAuthConsumerSecret();
            // try to find oauth tokens in the configuration
            if (consumerKey != null && consumerSecret != null) {
                OAuthAuthorization oauth = new OAuthAuthorization(conf);
                String accessToken = conf.getOAuthAccessToken();
                String accessTokenSecret = conf.getOAuthAccessTokenSecret();
                if (accessToken != null && accessTokenSecret != null) {
                    oauth.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret));
                }
                this.auth = oauth;
            } else {
                this.auth = NullAuthorization.getInstance();
            }
        }
        http = new HttpClientWrapper(conf);
        http.setHttpResponseListener(this);
        setFactory();
    }

    protected void setFactory() {
        factory = new z_T4JInternalJSONImplFactory(conf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScreenName() throws TwitterException, IllegalStateException {
        if (!auth.isEnabled()) {
            throw new IllegalStateException("Neither user ID/password combination nor OAuth consumer key/secret combination supplied");
        }
        if (null == screenName) {
            if (auth instanceof BasicAuthorization) {
                screenName = ((BasicAuthorization) auth).getUserId();
                if (-1 != screenName.indexOf("@")) {
                    screenName = null;
                }
            }
            if (null == screenName) {
                // retrieve the screen name if this instance is authenticated with OAuth or email address
                fillInIDAndScreenName();
            }
        }
        return screenName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getId() throws TwitterException, IllegalStateException {
        if (!auth.isEnabled()) {
            throw new IllegalStateException("Neither user ID/password combination nor OAuth consumer key/secret combination supplied");
        }
        if (0 == id) {
            fillInIDAndScreenName();
        }
        // retrieve the screen name if this instance is authenticated with OAuth or email address
        return id;
    }

    protected User fillInIDAndScreenName() throws TwitterException {
        ensureAuthorizationEnabled();
        User user = factory.createUser(http.get(conf.getRestBaseURL() + "account/verify_credentials.json", auth));
        this.screenName = user.getScreenName();
        this.id = user.getId();
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRateLimitStatusListener(RateLimitStatusListener listener) {
        rateLimitStatusListeners.add(listener);
    }

    @Override
    public void httpResponseReceived(HttpResponseEvent event) {
        if (rateLimitStatusListeners.size() != 0) {
            HttpResponse res = event.getResponse();
            TwitterException te = event.getTwitterException();
            RateLimitStatus rateLimitStatus;
            int statusCode;
            if (te != null) {
                rateLimitStatus = te.getRateLimitStatus();
                statusCode = te.getStatusCode();
            } else {
                rateLimitStatus = z_T4JInternalJSONImplFactory.createRateLimitStatusFromResponseHeader(res);
                statusCode = res.getStatusCode();
            }
            if (rateLimitStatus != null) {
                RateLimitStatusEvent statusEvent = new RateLimitStatusEvent(this, rateLimitStatus, event.isAuthenticated());
                if (statusCode == ENHANCE_YOUR_CLAIM || statusCode == SERVICE_UNAVAILABLE) {
                    // EXCEEDED_RATE_LIMIT_QUOTA is returned by Rest API
                    // SERVICE_UNAVAILABLE is returned by Search API
                    for (RateLimitStatusListener listener : rateLimitStatusListeners) {
                        listener.onRateLimitStatus(statusEvent);
                        listener.onRateLimitReached(statusEvent);
                    }
                } else {
                    for (RateLimitStatusListener listener : rateLimitStatusListeners) {
                        listener.onRateLimitStatus(statusEvent);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Authorization getAuthorization() {
        return auth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        return this.conf;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        if (http != null) {
            http.shutdown();
        }
    }

    protected final void ensureAuthorizationEnabled() {
        if (!auth.isEnabled()) {
            throw new IllegalStateException("Authentication credentials are missing. See http://twitter4jads.org/configuration.html for the detail.");
        }
    }

    protected final void ensureOAuthEnabled() {
        if (!(auth instanceof OAuthAuthorization)) {
            throw new IllegalStateException(
                    "OAuth required. Authentication credentials are missing. See http://twitter4jads.org/configuration.html for the detail.");
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        // http://docs.oracle.com/javase/6/docs/platform/serialization/spec/output.html#861
        out.putFields();
        out.writeFields();

        out.writeObject(conf);
        out.writeObject(auth);
        List<RateLimitStatusListener> serializableRateLimitStatusListeners = new ArrayList<RateLimitStatusListener>(0);
        for (RateLimitStatusListener listener : rateLimitStatusListeners) {
            if (listener instanceof Serializable) {
                serializableRateLimitStatusListeners.add(listener);
            }
        }
        out.writeObject(serializableRateLimitStatusListeners);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        // http://docs.oracle.com/javase/6/docs/platform/serialization/spec/input.html#2971
        stream.readFields();

        conf = (Configuration) stream.readObject();
        auth = (Authorization) stream.readObject();
        rateLimitStatusListeners = (List<RateLimitStatusListener>) stream.readObject();
        http = new HttpClientWrapper(conf);
        http.setHttpResponseListener(this);
        setFactory();
    }


    // methods declared in OAuthSupport interface

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setOAuthConsumer(String consumerKey, String consumerSecret) {
        if (null == consumerKey) {
            throw new NullPointerException("consumer key is null");
        }
        if (null == consumerSecret) {
            throw new NullPointerException("consumer secret is null");
        }
        if (auth instanceof NullAuthorization) {
            OAuthAuthorization oauth = new OAuthAuthorization(conf);
            oauth.setOAuthConsumer(consumerKey, consumerSecret);
            auth = oauth;
        } else if (auth instanceof BasicAuthorization) {
            XAuthAuthorization xauth = new XAuthAuthorization((BasicAuthorization) auth);
            xauth.setOAuthConsumer(consumerKey, consumerSecret);
            auth = xauth;
        } else if (auth instanceof OAuthAuthorization) {
            throw new IllegalStateException("consumer key/secret pair already set.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestToken getOAuthRequestToken() throws TwitterException {
        return getOAuthRequestToken(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestToken getOAuthRequestToken(String callbackUrl) throws TwitterException {
        return getOAuth().getOAuthRequestToken(callbackUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestToken getOAuthRequestToken(String callbackUrl, String xAuthAccessType) throws TwitterException {
        return getOAuth().getOAuthRequestToken(callbackUrl, xAuthAccessType);
    }

    /**
     * {@inheritDoc}
     * Basic authenticated instance of this class will try acquiring an AccessToken using xAuth.<br>
     * In order to get access acquire AccessToken using xAuth, you must apply by sending an email to <a href="mailto:api@twitter.com">api@twitter.com</a> all other applications will receive an HTTP 401 error.  Web-based applications will not be granted access, except on a temporary basis for when they are converting from basic-authentication support to full OAuth support.<br>
     * Storage of Twitter usernames and passwords is forbidden. By using xAuth, you are required to store only access tokens and access token secrets. If the access token expires or is expunged by a user, you must ask for their login and password again before exchanging the credentials for an access token.
     *
     * @throws TwitterException When Twitter service or network is unavailable, when the user has not authorized, or when the client application is not permitted to use xAuth
     * @see <a href="https://dev.twitter.com/docs/oauth/xauth">xAuth | Twitter Developers</a>
     */
    @Override
    public synchronized AccessToken getOAuthAccessToken() throws TwitterException {
        Authorization auth = getAuthorization();
        AccessToken oauthAccessToken;
        if (auth instanceof BasicAuthorization) {
            BasicAuthorization basicAuth = (BasicAuthorization) auth;
            auth = AuthorizationFactory.getInstance(conf);
            if (auth instanceof OAuthAuthorization) {
                this.auth = auth;
                OAuthAuthorization oauthAuth = (OAuthAuthorization) auth;
                oauthAccessToken = oauthAuth.getOAuthAccessToken(basicAuth.getUserId(), basicAuth.getPassword());
            } else {
                throw new IllegalStateException("consumer key / secret combination not supplied.");
            }
        } else {
            if (auth instanceof XAuthAuthorization) {
                XAuthAuthorization xauth = (XAuthAuthorization) auth;
                this.auth = xauth;
                OAuthAuthorization oauthAuth = new OAuthAuthorization(conf);
                oauthAuth.setOAuthConsumer(xauth.getConsumerKey(), xauth.getConsumerSecret());
                oauthAccessToken = oauthAuth.getOAuthAccessToken(xauth.getUserId(), xauth.getPassword());
            } else {
                oauthAccessToken = getOAuth().getOAuthAccessToken();
            }
        }
        screenName = oauthAccessToken.getScreenName();
        id = oauthAccessToken.getUserId();
        return oauthAccessToken;
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException when AccessToken has already been retrieved or set
     */
    @Override
    public synchronized AccessToken getOAuthAccessToken(String oauthVerifier) throws TwitterException {
        AccessToken oauthAccessToken = getOAuth().getOAuthAccessToken(oauthVerifier);
        screenName = oauthAccessToken.getScreenName();
        return oauthAccessToken;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException when AccessToken has already been retrieved or set
     */
    @Override
    public synchronized AccessToken getOAuthAccessToken(RequestToken requestToken) throws TwitterException {
        OAuthSupport oauth = getOAuth();
        AccessToken oauthAccessToken = oauth.getOAuthAccessToken(requestToken);
        screenName = oauthAccessToken.getScreenName();
        return oauthAccessToken;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException when AccessToken has already been retrieved or set
     */
    @Override
    public synchronized AccessToken getOAuthAccessToken(RequestToken requestToken, String oauthVerifier) throws TwitterException {
        return getOAuth().getOAuthAccessToken(requestToken, oauthVerifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setOAuthAccessToken(AccessToken accessToken) {
        getOAuth().setOAuthAccessToken(accessToken);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized AccessToken getOAuthAccessToken(String screenName, String password) throws TwitterException {
        return getOAuth().getOAuthAccessToken(screenName, password);
    }

    public Media upload(MediaUpload mediaUpload) throws TwitterException {
        return factory.createMediaUpload(post(conf.getMediaUploadBaseUrl() + "media/upload.json", mediaUpload.asHttpParameterArray()));
    }

    public Media mediaUploadInitOrFinalize(String url, HttpParameter[] parameters) throws TwitterException {
        return factory.createMediaUpload(post(url, parameters));
    }

    public Media checkVideoUploadStatus(String url, HttpParameter[] parameters) throws TwitterException {
        return factory.createMediaUpload(get(url, parameters));
    }

    public String createVideo(String tonVideoLocation, String title, String description) throws TwitterException {
        List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter("location", tonVideoLocation));
        if (StringUtils.isNotBlank(description)) {
            params.add(new HttpParameter("description", description));
        }
        if (StringUtils.isNotBlank(title)) {
            params.add(new HttpParameter("title", title));
        }
        return constructResponse(post(conf.getVideoBaseUrl(), params.toArray(new HttpParameter[params.size()])).asString(), TwitterUUIDResponse.class)
                .getuUID();
    }

    public String createVideoImage(String tonImageLocation, String title, String description) throws TwitterException {
        List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter("location", tonImageLocation));
        if (StringUtils.isNotBlank(description)) {
            params.add(new HttpParameter("description", description));
        }
        if (StringUtils.isNotBlank(title)) {
            params.add(new HttpParameter("title", title));
        }
        return constructResponse(post(conf.getVideoImageBaseUrl(), params.toArray(new HttpParameter[params.size()])).asString(),
                                 TwitterUUIDResponse.class).getuUID();
    }

    public VideoTweetResponse createVideoTweet(VideoTweetRequest request) throws TwitterException {
        return constructResponse(post(conf.getVideoTweetBaseUrl(), request.asHttpParamArray()).asString(), VideoTweetResponse.class);
    }


    public String uploadToTon(TonUpload tonUpload) throws TwitterException {
        setContentTypeAndTotalContentLength(tonUpload);
        if (tonUpload.getTotalContentLength() < 64000000) {
            return uploadSingleChunkToTon(tonUpload);
        } else {
            return uploadInChunks(tonUpload);
        }
    }

    private String uploadSingleChunkToTon(TonUpload tonUpload) throws TwitterException {
        byte[] data = getAllBytesToUpload(tonUpload);
        String bucketName = getBucketNameForTon(tonUpload.getMediaType());
        String endpoint = conf.getTwitterTonBaseUrl() + bucketName;
        Map<String, String> customHeaders = Maps.newHashMap();
        customHeaders.put("Content-Type", tonUpload.getContentType());
        customHeaders.put("Content-Length", String.valueOf(data.length));
        HttpParameter[] params = new HttpParameter[1];
        params[0] = new HttpParameter("file", data, true);
        HttpResponse response = postWithCustomHeaders(endpoint, params, customHeaders, true);
        return response.getResponseHeader("location");
    }


    private String uploadInChunks(TonUpload tonUpload) throws TwitterException {
        HttpResponse httpResponse = initiateResumableUpload(tonUpload.getMediaType(), tonUpload.getTotalContentLength(), tonUpload.getContentType());
        String location = httpResponse.getResponseHeader("Location");
        Long maxChunkSize = Long.valueOf(httpResponse.getResponseHeader("X-TON-Max-Chunk-Size"));
        Long minChunkSize = Long.valueOf(httpResponse.getResponseHeader("X-TON-Min-Chunk-Size"));
        Long chunkSize = computeChunkSize(minChunkSize, maxChunkSize);
        chunkSize = minChunkSize;
        String filePath = downloadContentIntoFileReturningPath(tonUpload.getMediaUrl());
        InputStream inputStream = null;
        BufferedInputStream dataInputStream = null;
        HttpResponse response = null;
        try {
            inputStream = new FileInputStream(filePath);
            dataInputStream = new BufferedInputStream(inputStream);
            byte[] chunk = new byte[chunkSize.intValue()];
            int length;
            Long bytesUploaded = 0l;

            while ((length = dataInputStream.read(chunk)) != -1) {
                if (length < chunkSize.intValue()) {
                    chunk = Arrays.copyOf(chunk, length);
                }
                response = uploadChunk(location, tonUpload.getContentType(), chunk, bytesUploaded, tonUpload.getTotalContentLength());
                bytesUploaded += length;
            }
        } catch (IOException e) {
            throw new TwitterException(e);
        } finally {
            IOUtils.closeQuietly(dataInputStream);
            IOUtils.closeQuietly(inputStream);
            deleteFile(filePath);
        }
        String responseLocation = response.getResponseHeader("Location");
        if (StringUtils.isNotBlank(responseLocation)) {
            return responseLocation;
        }
        return location;

    }


    private Long computeChunkSize(Long min, Long max) {
        Long chunkSize = min;
        while (chunkSize < max) {
            chunkSize += min;
        }
        return chunkSize;
    }

    private HttpResponse initiateResumableUpload(TonUpload.MediaType mediaType, Long totalContentLength, String contentType) throws TwitterException {
        String bucketName = getBucketNameForTon(mediaType);
        String endpoint = conf.getTwitterTonBaseUrl() + bucketName + "?resumable=true";
        Map<String, String> customHeaders = Maps.newHashMap();
        customHeaders.put("Content-Type", contentType);
        customHeaders.put("Content-Length", "0");
        customHeaders.put("X-TON-Content-Type", contentType);
        customHeaders.put("X-TON-Content-Length", String.valueOf(totalContentLength));
        HttpResponse httpResponse = postWithCustomHeaders(endpoint, null, customHeaders, false);
        return httpResponse;
    }

    private HttpResponse uploadChunk(String location, String contentType, byte[] data, Long uploadedBytes, Long totalContentLength)
            throws TwitterException {
        String endpoint = "https://ton.twitter.com" + location;
        Map<String, String> customHeaders = Maps.newHashMap();
        customHeaders.put("Content-Type", contentType);
        customHeaders.put("Content-Length", String.valueOf(data.length));
        customHeaders.put("Content-Range", createContentRange(uploadedBytes, data.length, totalContentLength));
        HttpParameter[] params = new HttpParameter[1];
        params[0] = new HttpParameter("file", data, true);
        return putWithCustomHeaders(endpoint, params, customHeaders, true);

    }

    private String createContentRange(Long uploadedBytes, int contentLength, Long totalContentLength) {
        return "bytes " + String.valueOf(uploadedBytes) + "-" + String.valueOf(uploadedBytes + contentLength - 1) +
               "/" + String.valueOf(totalContentLength);
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private <T> T constructResponse(String response, Class<T> clazz) throws TwitterException {
        try {
            return OBJECT_MAPPER.readValue(response, clazz);
        } catch (IOException e) {
            throw new TwitterException("Error while parsing response", e);
        }

    }

    private String getBucketNameForTon(TonUpload.MediaType mediaType) throws TwitterException {
        switch (mediaType) {
            case IMAGE:
                return "pro_video_api";
            case VIDEO:
                return "pro_video_api";
            case AUDIENCE:
                return "ta_partner";
        }
        throw new TwitterException("Unknown Ton Media Type");
    }

    /* OAuth support methods */

    private OAuthSupport getOAuth() {
        if (!(auth instanceof OAuthSupport)) {
            throw new IllegalStateException("OAuth consumer key/secret combination not supplied");
        }
        return (OAuthSupport) auth;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TwitterBaseImpl)) {
            return false;
        }

        TwitterBaseImpl that = (TwitterBaseImpl) o;

        if (auth != null ? !auth.equals(that.auth) : that.auth != null) {
            return false;
        }
        if (!conf.equals(that.conf)) {
            return false;
        }
        if (http != null ? !http.equals(that.http) : that.http != null) {
            return false;
        }
        if (!rateLimitStatusListeners.equals(that.rateLimitStatusListeners)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = conf.hashCode();
        result = 31 * result + (http != null ? http.hashCode() : 0);
        result = 31 * result + rateLimitStatusListeners.hashCode();
        result = 31 * result + (auth != null ? auth.hashCode() : 0);
        return result;
    }


    protected HttpResponse executeRequest(String url, HttpVerb httpVerb) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            switch (httpVerb) {
                case GET:
                    return http.get(url, auth);
                case POST:
                    return http.post(url, auth);
                case PUT:
                    return http.put(url, auth);
                case DELETE:
                    return http.delete(url, auth);
            }
        }
        // intercept HTTP call for monitoring purposes
        HttpResponse response = null;
        long start = System.currentTimeMillis();
        try {
            switch (httpVerb) {
                case GET:
                    return http.get(url, auth);
                case POST:
                    return http.post(url, auth);
                case PUT:
                    return http.put(url, auth);
                case DELETE:
                    return http.delete(url, auth);
            }
        } finally {
            long elapsedTime = System.currentTimeMillis() - start;
            TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
        }
        return response;
    }


    protected HttpResponse executeRequest(String url, HttpParameter[] params, HttpVerb httpVerb) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            switch (httpVerb) {
                case GET:
                return http.get(url, params, auth);
                case POST:
                return http.post(url, params, auth);
                case PUT:
                return http.put(url, params, auth);
                case DELETE:
                return http.delete(url, params, auth);
            }
        }
        // intercept HTTP call for monitoring purposes
        HttpResponse response = null;
        long start = System.currentTimeMillis();
        try {
            switch (httpVerb) {
                case GET:
                return http.get(url, params, auth);
                case POST:
                return http.post(url, params, auth);
                case PUT:
                return http.put(url, params, auth);
                case DELETE:
                return http.delete(url, params, auth);
            }
        } finally {
            long elapsedTime = System.currentTimeMillis() - start;
            TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
        }
        return response;
    }

    protected HttpResponse get(String url) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.get(url, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.get(url, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpResponse getWithoutMergingImplicitParams(String url, HttpParameter[] params) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.get(url, params, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.get(url, params, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpResponse get(String url, HttpParameter[] params) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.get(url, params, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.get(url, params, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpResponse post(String url) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.post(url, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.post(url, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpResponse post(String url, HttpParameter[] params) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.post(url, params, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.post(url, params, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpResponse post(String url, String requestBody) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.post(url, requestBody, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.post(url, requestBody, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpResponse put(String url, String requestBody) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.putRequest(url, requestBody, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.putRequest(url, requestBody, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpResponse postWithCustomHeaders(String url, HttpParameter[] params, Map<String, String> customHeaders, boolean isTonUpload)
            throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.postWithCustomHeaders(url, params, auth, customHeaders, isTonUpload);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.postWithCustomHeaders(url, params, auth, customHeaders, isTonUpload);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpResponse putWithCustomHeaders(String url, HttpParameter[] params, Map<String, String> customHeaders, boolean isTonUpload)
            throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.putWithCustomHeaders(url, params, auth, customHeaders, isTonUpload);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.putWithCustomHeaders(url, params, auth, customHeaders, isTonUpload);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpResponse put(String url) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.put(url, null, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.put(url, null, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    public HttpResponse postBatchRequest(String url, String requestBody) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.postBatchRequest(url, null, auth, requestBody);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.put(url, null, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpResponse put(String url, HttpParameter[] params) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.put(url, params, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.put(url, params, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpResponse delete(String url) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.delete(url, null, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.delete(url, null, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpResponse delete(String url, HttpParameter[] params) throws TwitterException {
        ensureAuthorizationEnabled();
        if (!conf.isMBeanEnabled()) {
            return http.delete(url, params, auth);
        } else {
            // intercept HTTP call for monitoring purposes
            HttpResponse response = null;
            long start = System.currentTimeMillis();
            try {
                response = http.delete(url, params, auth);
            } finally {
                long elapsedTime = System.currentTimeMillis() - start;
                TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
            }
            return response;
        }
    }

    protected HttpParameter[] mergeParameters(HttpParameter[] params1, HttpParameter[] params2) {
        if (params1 != null && params2 != null) {
            HttpParameter[] params = new HttpParameter[params1.length + params2.length];
            System.arraycopy(params1, 0, params, 0, params1.length);
            System.arraycopy(params2, 0, params, params1.length, params2.length);
            return params;
        }
        if (null == params1 && null == params2) {
            return new HttpParameter[0];
        }
        if (params1 != null) {
            return params1;
        } else {
            return params2;
        }
    }

    protected HttpParameter[] mergeParameters(HttpParameter[] params1, HttpParameter params2) {
        if (params1 != null && params2 != null) {
            HttpParameter[] params = new HttpParameter[params1.length + 1];
            System.arraycopy(params1, 0, params, 0, params1.length);
            params[params.length - 1] = params2;
            return params;
        }
        if (null == params1 && null == params2) {
            return new HttpParameter[0];
        }
        if (params1 != null) {
            return params1;
        } else {
            return new HttpParameter[]{params2};
        }
    }

    private boolean isOk(HttpResponse response) {
        return response != null && response.getStatusCode() < 300;
    }

    private byte[] getAllBytesToUpload(TonUpload tonUpload) throws TwitterException {
        try {
            return IOUtils.toByteArray(new URL(tonUpload.getMediaUrl()).openStream());
        } catch (IOException e) {
            throw new TwitterException(e);
        }
    }

    private void setContentTypeAndTotalContentLength(TonUpload tonUpload) throws TwitterException {
        if (tonUpload.getTotalContentLength() != null && tonUpload.getTotalContentLength() > 0l &&
            StringUtils.isNotBlank(tonUpload.getContentType())) {
            return;
        }
        if (StringUtils.isNotBlank(tonUpload.getMediaUrl())) {

            URLConnection urlConnection = null;
            try {
                URL localURL = new URL(tonUpload.getMediaUrl());
                urlConnection = localURL.openConnection();
                String contentLength = urlConnection.getHeaderField("Content-Length");
                String contentType = urlConnection.getHeaderField("Content-Type");
                if (StringUtils.isNotBlank(contentLength)) {
                    tonUpload.setTotalContentLength(Long.valueOf(contentLength));
                }
                //check valid content type else add video/mp4 or image default or file default
                tonUpload.setContentType(contentType);
            } catch (IOException e) {
                throw new TwitterException("Failed to determine content type and length", e);
            } finally {

            }

        }
    }

    private String downloadContentIntoFileReturningPath(String url) {
        InputStream urlInputStream = null;
        FileOutputStream temporaryFileStream = null;
        try {
            urlInputStream = new URL(url).openStream();
            File tempMedia = File.createTempFile("tw-upload-temp-" + System.currentTimeMillis(), ".tmp");
            tempMedia.deleteOnExit();
            temporaryFileStream = new FileOutputStream(tempMedia);
            IOUtils.copyLarge(urlInputStream, temporaryFileStream);
            temporaryFileStream.flush();
            return tempMedia.getAbsolutePath();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(temporaryFileStream);
            IOUtils.closeQuietly(urlInputStream);
        }
    }

    private void deleteFile(String filePath) {
        try {
            new File(filePath).delete();
        } catch (Exception e) {
        }
    }

}
