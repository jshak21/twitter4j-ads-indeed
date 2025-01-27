package twitter4jads.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import twitter4jads.TwitterAdsClient;
import twitter4jads.api.TwitterAdsMediaUploadApi;
import twitter4jads.internal.http.HttpParameter;
import twitter4jads.internal.http.HttpResponse;
import twitter4jads.internal.models4j.TwitterException;
import twitter4jads.internal.models4j.TwitterInvalidParameterException;
import twitter4jads.internal.models4j.TwitterRuntimeException;
import twitter4jads.models.ads.HttpVerb;
import twitter4jads.models.media.TwitterMediaType;
import twitter4jads.models.video.UploadMediaObjectResponse;
import twitter4jads.util.TwitterAdUtil;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static twitter4jads.TwitterAdsConstants.*;
import static twitter4jads.internal.models4j.TwitterImpl.PARAM_ADDITIONAL_OWNERS;

/**
 * User: abhay
 * Date: 4/5/16
 * Time: 10:41 AM
 */
public class TwitterAdsMediaUploadApiImpl implements TwitterAdsMediaUploadApi {

    private static final Map<Long, Long> VIDEO_SIZE_PROCESSING_WAIT_TIME_MAP;

    private final TwitterAdsClient twitterAdsClient;

    public TwitterAdsMediaUploadApiImpl(TwitterAdsClient twitterAdsClient) {
        this.twitterAdsClient = twitterAdsClient;
    }

    static {
        final Map<Long, Long> videoSizeProcessingWaitTimeMap = Maps.newHashMap();
        videoSizeProcessingWaitTimeMap.put(FIFTY_MIB, MAX_WAIT_INTERVAL_FIFTY_MIB);
        videoSizeProcessingWaitTimeMap.put(ONE_HUNDRED_FIFTY_MIB, MAX_WAIT_INTERVAL_ONE_HUNDRED_FIFTY_MIB);
        videoSizeProcessingWaitTimeMap.put(FIVE_HUNDRED_MIB, MAX_WAIT_INTERVAL_FIVE_HUNDRED_MIB);

        VIDEO_SIZE_PROCESSING_WAIT_TIME_MAP = Collections.unmodifiableMap(videoSizeProcessingWaitTimeMap);
    }

    @Override
    public String uploadMediaAndGetMediaKey(String mediaUrl, Set<String> accountUserIds,
            TwitterMediaType twitterMediaType, String name)
            throws TwitterException {
        final UploadMediaObjectResponse responseFromFinalize = uploadAndGetMediaKey(mediaUrl, accountUserIds,
                twitterMediaType, name);
        String mediaId = responseFromFinalize.getMediaId();
        final String mediaKey = responseFromFinalize.getMediaKey();
        final Long videoSize = responseFromFinalize.getSize();

        //as per documentation if media process info is null then the video is ready
        if (responseFromFinalize.getUploadMediaProcessingInfo() == null) {
            return mediaKey;
        }

        if (responseFromFinalize.getUploadMediaProcessingInfo().getUploadErrorInfo() != null) {
            throw new TwitterException(responseFromFinalize.getUploadMediaProcessingInfo().getUploadErrorInfo().getMessage());
        }

        final String state = responseFromFinalize.getUploadMediaProcessingInfo().getState();
        final Integer progressPercentage = responseFromFinalize.getUploadMediaProcessingInfo().getProgressPercentage();
        if ((TwitterAdUtil.isNotNullOrEmpty(state) && state.equalsIgnoreCase("succeeded")) ||
                (progressPercentage != null && progressPercentage == 100)) {
            return mediaKey;
        }

        return waitForVideoProcessingAndReturnKey(mediaId, mediaKey, responseFromFinalize, videoSize);
    }

    // ------------------------------------------------------------------- PRIVATE METHODS ----------------------------------------------------------

    private UploadMediaObjectResponse uploadAndGetMediaKey(String mediaUrl, Set<String> accountUserIds,
            TwitterMediaType twitterMediaType, String name)
            throws TwitterException {
        try {
            String mediaSizeInBytes = getMediaSizeInBytes(mediaUrl);
            String mediaId = initiateMediaUpload(mediaSizeInBytes, accountUserIds, twitterMediaType, name);
            uploadMedia(mediaUrl, mediaId, mediaSizeInBytes);
            return finalizeMediaUpload(mediaId);
        } catch (Exception e) {
            if (e instanceof TwitterException) {
                throw (TwitterException) e;
            }
            throw new TwitterException("Error Occurred while uploading Media", e);
        }
    }

    private String getMediaSizeInBytes(String mediaUrl) throws TwitterException, IOException {
        try {
            URLConnection urlConnection = new URL(mediaUrl).openConnection();
            return urlConnection.getHeaderField("Content-Length");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid media url");
        }
    }

    private String initiateMediaUpload(String mediaSizeInBytes, Set<String> accountUserIds, TwitterMediaType twitterMediaType, String name)
            throws TwitterException {
        if (StringUtils.isBlank(mediaSizeInBytes)) {
            throw new TwitterInvalidParameterException("Media could not be uploaded as connection could not be established");
        }

        Long mediaSizeInBytesLong;
        try {
            mediaSizeInBytesLong = Long.valueOf(mediaSizeInBytes);
        } catch (NumberFormatException eX) {
            throw new TwitterException("Media could not be uploaded as connection could not be established");
        }

        if (twitterMediaType == TwitterMediaType.IMAGE && mediaSizeInBytesLong > MAX_IMAGE_SIZE_FOR_TWITTER_IN_BYTES) {
            throw new TwitterInvalidParameterException("Image should be less than 5 MB in size");
        }
        if (twitterMediaType == TwitterMediaType.VIDEO && mediaSizeInBytesLong > MAX_VIDEO_SIZE_IN_BYTES) {
            throw new TwitterInvalidParameterException("Video should be less than 500 MB in size");
        }

        final String url = twitterAdsClient.getMediaUploadBaseUrl() + UPLOAD_MEDIA_URL + UPLOAD_JSON;
        final HttpParameter[] parameters = createInitiateMediaUploadParams(mediaSizeInBytes, accountUserIds, twitterMediaType);
        return twitterAdsClient.mediaUploadInitOrFinalize(url, parameters).getMediaIdString();
    }

    private UploadMediaObjectResponse finalizeMediaUpload(String mediaId) throws TwitterException {
        final String url = twitterAdsClient.getMediaUploadBaseUrl() + UPLOAD_MEDIA_URL + UPLOAD_JSON;
        final HttpParameter[] parameters = createFinalizeMediaUploadParams(mediaId);
        final Type type = new TypeToken<UploadMediaObjectResponse>() {
        }.getType();

        return twitterAdsClient.executeRequest(url, parameters, type, HttpVerb.POST);
    }

    private HttpParameter[] createInitiateMediaUploadParams(String mediaSizeInBytes, Set<String> accountUserIds, TwitterMediaType twitterMediaType) {
        if (StringUtils.isBlank(mediaSizeInBytes)) {
            throw new TwitterRuntimeException(null, new TwitterException("mediaSizeInBytes cannot be blank or null."));
        }

        final List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_COMMAND, "INIT"));
        params.add(new HttpParameter(PARAM_TOTAL_BYTES, mediaSizeInBytes));

        switch (twitterMediaType) {
            case VIDEO:
                /*
                Only one attributable user can be specified per video.
                Only the attributable user will be able to use the video in a Tweet.
                If additional owners are also listed, they will be ignored.
                */
                params.add(new HttpParameter(PARAM_MEDIA_TYPE, "video/mp4"));
                params.add(new HttpParameter(PARAM_MEDIA_CATEGORY, "amplify_video"));
                if (TwitterAdUtil.isNotEmpty(accountUserIds) && accountUserIds.size() == 1) {
                    final String accountUserId = accountUserIds.iterator().next();
                    params.add(new HttpParameter(PARAM_ATTRIBUTABLE_USER_ID, accountUserId));
                }
                break;
            case IMAGE:
                params.add(new HttpParameter(PARAM_MEDIA_TYPE, "image/jpeg"));
            params.add(new HttpParameter(PARAM_MEDIA_CATEGORY, "tweet_image"));
                break;
            case DM_IMAGE:
                params.add(new HttpParameter(PARAM_MEDIA_TYPE, "image/jpeg"));
                params.add(new HttpParameter(PARAM_MEDIA_CATEGORY, "dm_image"));
                params.add(new HttpParameter(PARAM_SHARED, true));
                break;
            case DM_VIDEO:
                params.add(new HttpParameter(PARAM_MEDIA_TYPE, "video/mp4"));
                params.add(new HttpParameter(PARAM_MEDIA_CATEGORY, "dm_video"));
                params.add(new HttpParameter(PARAM_SHARED, true));
                break;
            case DM_GIF:
                params.add(new HttpParameter(PARAM_MEDIA_TYPE, "video/gif")); //check this
                params.add(new HttpParameter(PARAM_MEDIA_CATEGORY, "dm_image"));
                params.add(new HttpParameter(PARAM_SHARED, true));
                break;
            default:
                break;
        }

        if (TwitterAdUtil.isNotEmpty(accountUserIds)) {
            params.add(new HttpParameter(PARAM_ADDITIONAL_OWNERS, TwitterAdUtil.getCsv(accountUserIds)));
        }

        return params.toArray(new HttpParameter[params.size()]);
    }

    private void uploadMedia(String mediaUrl, String mediaId, String mediaSizeInBytes) throws TwitterException, IOException {
        int segmentIndex = 0;
        Long bytesLeftToUpload = Long.valueOf(mediaSizeInBytes);
        InputStream inputStream = null;
        BufferedInputStream bis = null;
        try {
            inputStream = new URL(mediaUrl).openStream();
            bis = new BufferedInputStream(inputStream);

            while (bytesLeftToUpload > 0) {
                int totalBytesRead = 0;
                byte[] chunkToUpload = new byte[2 * TWO_MIB];

                while (totalBytesRead < TWO_MIB) {
                    byte[] chunk = new byte[TWO_MIB];
                    int bytesRead = bis.read(chunk);
                    if (bytesRead == -1) {
                        break;
                    } else {
                        chunk = Arrays.copyOf(chunk, bytesRead);
                        for (int i = 0; i < bytesRead; i++) {
                            chunkToUpload[totalBytesRead++] = chunk[i];
                        }
                    }
                }

                if (totalBytesRead > 0) {
                    chunkToUpload = Arrays.copyOf(chunkToUpload, totalBytesRead);
                    String base64Encoding = DatatypeConverter.printBase64Binary(chunkToUpload);
                    appendChunk(mediaId, base64Encoding, segmentIndex);
                    bytesLeftToUpload -= totalBytesRead;
                    segmentIndex += 1;
                } else {
                    break;
                }
            }
        } finally {
            if (inputStream != null) {
                IOUtils.closeQuietly(bis);
            }
        }
    }

    private void appendChunk(String mediaId, String chunk, int segmentIndex) throws TwitterException {
        String url = twitterAdsClient.getMediaUploadBaseUrl() + "media/upload.json";

        List<HttpParameter> params = createAppendChunkParams(mediaId, chunk, segmentIndex);
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);

        HttpResponse response = twitterAdsClient.postRequest(url, parameters);
        int responseCode = response.getStatusCode();
        if (responseCode < SUCCESSFULL_CALL_BEGIN_CODE || responseCode > SUCCESSFULL_CALL_END_CODE) {
            throw new TwitterException(response.asString());
        }

    }

    private List<HttpParameter> createAppendChunkParams(String mediaId, String chunk, int segment_index) {
        List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_COMMAND, "APPEND"));
        params.add(new HttpParameter(PARAM_MEDIA_ID, mediaId));
        params.add(new HttpParameter(PARAM_MEDIA_DATA, chunk));
        params.add(new HttpParameter(PARAM_SEGMENT_INDEX, segment_index));

        return params;
    }

    private HttpParameter[] createFinalizeMediaUploadParams(String mediaId) {
        final List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_COMMAND, "FINALIZE"));
        params.add(new HttpParameter(PARAM_MEDIA_ID, mediaId));

        return params.toArray(new HttpParameter[params.size()]);
    }

    private String waitForVideoProcessingAndReturnKey(String mediaIdString, String mediaKey,
            UploadMediaObjectResponse statusResponse, Long videoSize)
            throws TwitterException {
        if (statusResponse == null) {
            statusResponse = getUploadStatus(mediaIdString);
        }

        Long timeToWait = 0L;
        Long checkAfterSeconds = statusResponse.getUploadMediaProcessingInfo().getCheckAfterSeconds();
        Long maxWaitTime = decideMaxWaitTime(videoSize);
        while (timeToWait < maxWaitTime) {
            TwitterAdUtil.reallySleep(checkAfterSeconds * 1000);
            timeToWait = timeToWait + checkAfterSeconds;

            statusResponse = getUploadStatus(mediaIdString);
            if (statusResponse == null) {
                throw new TwitterException("Could not upload Video successfully, please select a different video");
            }
            //as per documentation if media process info is null then the video is ready
            if (statusResponse.getUploadMediaProcessingInfo() == null) {
                return mediaKey;
            }
            if (statusResponse.getUploadMediaProcessingInfo().getUploadErrorInfo() != null) {
                throw new TwitterException(statusResponse.getUploadMediaProcessingInfo().getUploadErrorInfo().getMessage());
            }

            String state = statusResponse.getUploadMediaProcessingInfo().getState();
            Integer progressPercentage = statusResponse.getUploadMediaProcessingInfo().getProgressPercentage();
            if ((TwitterAdUtil.isNotNullOrEmpty(state) && state.equalsIgnoreCase("succeeded")) ||
                    (progressPercentage != null && progressPercentage == 100)) {
                return mediaKey;
            }
        }

        if (statusResponse.getUploadMediaProcessingInfo().getProgressPercentage() != null &&
                statusResponse.getUploadMediaProcessingInfo().getProgressPercentage() < 100 &&
                statusResponse.getUploadMediaProcessingInfo().getState() != null &&
                statusResponse.getUploadMediaProcessingInfo().getState().equalsIgnoreCase("in_progress")) {
            throw new TwitterException(
                    "Please retry playing the ad, or upload a new video, there is problem at Twitter's end in processing the " + "video");
        }
        throw new TwitterException(statusResponse.getUploadMediaProcessingInfo().getUploadErrorInfo().getMessage());
    }

    private Long decideMaxWaitTime(Long videoSize) {
        Long maxWaitTime = VIDEO_SIZE_PROCESSING_WAIT_TIME_MAP.get(getVideoSizeGroup(videoSize));
        if (maxWaitTime == null) {
            maxWaitTime = MAX_WAIT_INTERVAL_ONE_HUNDRED_FIFTY_MIB;
        }

        return maxWaitTime;
    }

    private Long getVideoSizeGroup(Long videoSize) {
        if (videoSize == null) {
            return ONE_HUNDRED_FIFTY_MIB;
        }

        if (videoSize <= FIFTY_MIB) {
            return FIFTY_MIB;
        } else if (videoSize <= ONE_HUNDRED_FIFTY_MIB) {
            return ONE_HUNDRED_FIFTY_MIB;
        } else {
            return FIVE_HUNDRED_MIB;
        }
    }

    private UploadMediaObjectResponse getUploadStatus(String mediaId) throws TwitterException {
        final String url = twitterAdsClient.getMediaUploadBaseUrl() + "media/upload.json";

        final List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_COMMAND, "STATUS"));
        params.add(new HttpParameter(PARAM_MEDIA_ID, mediaId));

        Type type = new TypeToken<UploadMediaObjectResponse>() {
        }.getType();

        return twitterAdsClient.executeRequest(url, params.toArray(new HttpParameter[params.size()]), type, HttpVerb.GET);
    }
}
