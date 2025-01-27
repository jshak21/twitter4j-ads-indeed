package twitter4jads.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import twitter4jads.*;
import twitter4jads.api.TwitterAdsCardsApi;
import twitter4jads.internal.http.HttpParameter;
import twitter4jads.internal.http.HttpResponse;
import twitter4jads.internal.models4j.Media;
import twitter4jads.internal.models4j.MediaUpload;
import twitter4jads.internal.models4j.TwitterException;
import twitter4jads.models.ads.HttpVerb;
import twitter4jads.models.ads.TwitterUUIDResponse;
import twitter4jads.models.ads.cards.*;
import twitter4jads.models.media.TwitterLibraryMedia;
import twitter4jads.util.TwitterAdUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static twitter4jads.TwitterAdsConstants.*;
import static twitter4jads.util.TwitterAdUtil.isNotNullOrEmpty;

/**
 * User: abhay
 * Date: 4/4/16
 * Time: 8:10 PM
 */
public class TwitterAdsCardsApiImpl implements TwitterAdsCardsApi {

    private static final Gson GSON = new Gson();

    private final TwitterAdsClient twitterAdsClient;

    private static final int CONVERSATION_CARD_HASHTAG_LENGTH = 20;
    private static final int CONVERSATION_CARD_TWEET_LENGTH = 256;

    public TwitterAdsCardsApiImpl(TwitterAdsClient twitterAdsClient) {
        this.twitterAdsClient = twitterAdsClient;
    }

    @Override
    public BaseAdsListResponseIterable<Card> getCards(String accountId, List<CardType> cardTypes, List<String> cardIds,
                                                      List<String> cardUris, boolean includeLegacyCards,
                                                      Optional<String> q, Optional<String> sortBy,
                                                      boolean withDeleted, Optional<Integer> count,
                                                      Optional<String> cursor) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");
        List<HttpParameter> params = Lists.newArrayList();

        if (TwitterAdUtil.isNotEmpty(cardTypes)) {
            params.add(new HttpParameter(PARAM_CARD_TYPES, TwitterAdUtil.getCsv(cardTypes)));
        }

        if (TwitterAdUtil.isNotEmpty(cardIds)) {
            params.add(new HttpParameter(PARAM_CARD_IDS, TwitterAdUtil.getCsv(cardIds)));
        }

        if (TwitterAdUtil.isNotEmpty(cardIds)) {
            params.add(new HttpParameter(PARAM_CARD_URIS, TwitterAdUtil.getCsv(cardUris)));
        }

        if (count != null && count.isPresent()) {
            params.add(new HttpParameter(PARAM_COUNT, count.get()));
        }

        if (cursor != null && cursor.isPresent()) {
            params.add(new HttpParameter(PARAM_CURSOR, cursor.get()));
        }

        params.add(new HttpParameter(PARAM_INCLUDE_LEGACY_CARDS, includeLegacyCards));

        if (q != null && q.isPresent()) {
            params.add(new HttpParameter(PARAM_Q, q.get()));
        }

        if (sortBy != null && sortBy.isPresent()) {
            params.add(new HttpParameter(PARAM_SORT_BY, sortBy.get()));
        }

        params.add(new HttpParameter(PARAM_WITH_DELETED, withDeleted));

        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_CARDS;
        Type type = new TypeToken<BaseAdsListResponse<TwitterImageAppDownloadCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpListRequest(url, params, type);
    }

    @Override
    public BaseAdsResponse<Card> createCard(Card card) throws TwitterException {
        TwitterAdUtil.ensureNotNull(card, "Card");
        TwitterAdUtil.ensureNotNull(card.getAccountId(), "AccountId");

        final String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 +
                card.getAccountId() + PATH_CARDS;
        final Type type = new TypeToken<BaseAdsResponse<Card>>() {
        }.getType();

        HttpResponse httpResponse = twitterAdsClient.postRequest(url, GSON.toJson(card));
        return GSON.fromJson(httpResponse.asString(), type);
    }

    @Override
    public BaseAdsResponse<Card> updateCard(Card card) throws TwitterException {
        TwitterAdUtil.ensureNotNull(card, "Card");
        TwitterAdUtil.ensureNotNull(card.getAccountId(), "AccountId");

        final String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 +
                card.getAccountId() + PATH_CARDS;
        final Type type = new TypeToken<BaseAdsResponse<Card>>() {
        }.getType();


        HttpResponse httpResponse = twitterAdsClient.putRequest(url, GSON.toJson(card));
        return GSON.fromJson(httpResponse.asString(), type);
    }

    @Override
    public BaseAdsResponse<Card> deleteCard(String accountId, String cardId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_CARDS + cardId;
        Type type = new TypeToken<BaseAdsResponse<TwitterImageAppDownloadCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, null, type, HttpVerb.DELETE);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsListResponseIterable<TwitterImageAppDownloadCard> getAllImageAppDownloadCards(String accountId, List<String> cardIds,
                                                                                                boolean withDeleted, Optional<Integer> count)
            throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");
        List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_WITH_DELETED, withDeleted));
        if (TwitterAdUtil.isNotEmpty(cardIds)) {
            params.add(new HttpParameter(PARAM_CARD_IDS, TwitterAdUtil.getCsv(cardIds)));
        }
        if (count != null && count.isPresent()) {
            params.add(new HttpParameter(PARAM_COUNT, count.get()));
        }
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_IMAGE_APP_DOWNLOAD_CARDS;
        Type type = new TypeToken<BaseAdsListResponse<TwitterImageAppDownloadCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpListRequest(url, params, type);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsListResponseIterable<TwitterVideoAppDownloadCard> getAllVideoAppDownloadCards(String accountId, List<String> cardIds,
                                                                                                boolean withDeleted, Optional<Integer> count)
            throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");

        final List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_WITH_DELETED, withDeleted));
        if (TwitterAdUtil.isNotEmpty(cardIds)) {
            params.add(new HttpParameter(PARAM_CARD_IDS, TwitterAdUtil.getCsv(cardIds)));
        }
        if (count != null && count.isPresent()) {
            params.add(new HttpParameter(PARAM_COUNT, count.get()));
        }
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_APP_DOWNLOAD_CARDS;
        Type type = new TypeToken<BaseAdsListResponse<TwitterVideoAppDownloadCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpListRequest(url, params, type);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsResponse<TwitterWebsiteCard> deleteWebsiteCard(String accountId, String cardId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_WEBSITE_CARDS + cardId;
        Type type = new TypeToken<BaseAdsResponse<TwitterWebsiteCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, null, type, HttpVerb.DELETE);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsResponse<TwitterWebsiteCard> getWebsiteCard(String accountId, String cardId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");
        TwitterAdUtil.ensureNotNull(cardId, "CardId");
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_WEBSITE_CARDS + cardId;
        Type type = new TypeToken<BaseAdsResponse<TwitterWebsiteCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, null, type, HttpVerb.GET);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsListResponseIterable<TwitterWebsiteCard> getAllWebsiteCards(String accountId, List<String> cardIds, boolean withDeleted,
                                                                              Optional<Integer> count) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");
        List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_WITH_DELETED, withDeleted));
        if (TwitterAdUtil.isNotEmpty(cardIds)) {
            params.add(new HttpParameter(PARAM_CARD_IDS, TwitterAdUtil.getCsv(cardIds)));
        }
        if (count != null && count.isPresent()) {
            params.add(new HttpParameter(PARAM_COUNT, count.get()));
        }

        String url = twitterAdsClient.getBaseAdsAPIUrl() + PREFIX_ACCOUNTS_URI_5 + accountId + PATH_WEBSITE_CARDS;
        Type type = new TypeToken<BaseAdsListResponse<TwitterWebsiteCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpListRequest(url, params, type);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsListResponseIterable<TwitterMobileAppCard> getAllAppDownloadCards(String accountId, List<String> cardIds, boolean withDeleted,
                                                                                    Optional<Integer> count) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");

        final List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_WITH_DELETED, withDeleted));
        if (TwitterAdUtil.isNotEmpty(cardIds)) {
            params.add(new HttpParameter(PARAM_CARD_IDS, TwitterAdUtil.getCsv(cardIds)));
        }
        if (count != null && count.isPresent()) {
            params.add(new HttpParameter(PARAM_COUNT, count.get()));
        }
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_APP_DOWNLOAD_CARDS;
        Type type = new TypeToken<BaseAdsListResponse<TwitterMobileAppCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpListRequest(url, params, type);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsResponse<TwitterMobileAppCard> getAppDownloadCard(String accountId, String cardId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");
        TwitterAdUtil.ensureNotNull(cardId, "CardId");
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_APP_DOWNLOAD_CARDS + cardId;
        Type type = new TypeToken<BaseAdsResponse<TwitterMobileAppCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, null, type, HttpVerb.GET);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsResponse<TwitterVideoAppDownloadCard> deleteVideoAppDownloadCard(String accountId, String cardId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_APP_DOWNLOAD_CARDS + cardId;
        Type type = new TypeToken<BaseAdsResponse<TwitterVideoAppDownloadCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, null, type, HttpVerb.DELETE);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsResponse<TwitterMobileAppCard> deleteAppDownloadCard(String accountId, String cardId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_APP_DOWNLOAD_CARDS + cardId;
        Type type = new TypeToken<BaseAdsResponse<TwitterMobileAppCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, null, type, HttpVerb.DELETE);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsResponse<TwitterImageAppDownloadCard> deleteImageAppDownloadCard(String accountId, String cardId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_IMAGE_APP_DOWNLOAD_CARDS + cardId;
        Type type = new TypeToken<BaseAdsResponse<TwitterImageAppDownloadCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, null, type, HttpVerb.DELETE);
    }

    @Override
    public BaseAdsResponse<TwitterLeadGenerationStat> getTwitterLeadGenerationStat(String accountId, String cardId, String startTime, Optional<String> endTime,
                                                                                   Optional<String> granularity, Optional<String> metric, Optional<Boolean> withDeleted)
            throws TwitterException {

        final List<HttpParameter> params =
                validateAndCreateParamsForLeadGenerationCardStat(accountId, cardId, startTime, endTime, granularity, metric, withDeleted);
        String url = twitterAdsClient.getBaseAdsAPIUrl() + PREFIX_STATS_ACCOUNTS_URI + accountId + PATH_LEAD_GENERATION_CARDS + cardId;
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type typeToken = new TypeToken<BaseAdsResponse<TwitterLeadGenerationStat>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, parameters, typeToken, HttpVerb.GET);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsListResponseIterable<TwitterImageConversationCard> getAllImageConversationCards(String accountId, List<String> cardIds, boolean withDeleted, Integer count) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");

        final List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_WITH_DELETED, withDeleted));
        if (TwitterAdUtil.isNotEmpty(cardIds)) {
            params.add(new HttpParameter(PARAM_CARD_IDS, TwitterAdUtil.getCsv(cardIds)));
        }

        if (TwitterAdUtil.isNotNull(count)) {
            params.add(new HttpParameter(PARAM_COUNT, count));
        }
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_IMAGE_CONVERSATION_CARDS;
        Type type = new TypeToken<BaseAdsListResponse<TwitterImageConversationCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpListRequest(url, params, type);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsListResponseIterable<TwitterVideoConversationCard> getAllVideoConversationCards(String accountId, List<String> cardIds, boolean withDeleted, Integer count) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");

        final List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_WITH_DELETED, withDeleted));
        if (TwitterAdUtil.isNotEmpty(cardIds)) {
            params.add(new HttpParameter(PARAM_CARD_IDS, TwitterAdUtil.getCsv(cardIds)));
        }

        if (TwitterAdUtil.isNotNull(count)) {
            params.add(new HttpParameter(PARAM_COUNT, count));
        }
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_CONVERSATION_CARDS;
        Type type = new TypeToken<BaseAdsListResponse<TwitterVideoConversationCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpListRequest(url, params, type);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsListResponseIterable<TwitterVideoWebsiteCard> getAllVideoWebsiteCards(String accountId, List<String> cardIds, boolean withDeleted,
                                                                                        Integer count) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");

        final List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_WITH_DELETED, withDeleted));
        if (TwitterAdUtil.isNotEmpty(cardIds)) {
            params.add(new HttpParameter(PARAM_CARD_IDS, TwitterAdUtil.getCsv(cardIds)));
        }

        if (TwitterAdUtil.isNotNull(count)) {
            params.add(new HttpParameter(PARAM_COUNT, count));
        }

        final String url = twitterAdsClient.getBaseAdsAPIUrl() + PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_WEBSITE_CARDS;
        Type type = new TypeToken<BaseAdsListResponse<TwitterVideoWebsiteCard>>() {
        }.getType();

        return twitterAdsClient.executeHttpListRequest(url, params, type);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsResponse<TwitterWebsiteCard> updateWebsiteCard(String accountId, String name, String cardId,
            String websiteTitle, String websiteUrl, String imageMediaKey) throws TwitterException {
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");

        List<HttpParameter> params = validateAndCreateParamsForCreateWebsiteCard(accountId, name, websiteTitle,
                websiteUrl, imageMediaKey);
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_WEBSITE_CARDS + cardId;
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterWebsiteCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.PUT);
    }

    @Override
    public BaseAdsResponse<TwitterWebsiteCard> createWebsiteCard(String accountId, String name, String websiteTitle,
            String websiteUrl, String imageMediaKey) throws TwitterException {
        final List<HttpParameter> params = validateAndCreateParamsForCreateWebsiteCard(accountId, name, websiteTitle,
                websiteUrl, imageMediaKey);
        final String url = twitterAdsClient.getBaseAdsAPIUrl() + PREFIX_ACCOUNTS_URI_5 + accountId + PATH_WEBSITE_CARDS;
        final HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterWebsiteCard>>() {
        }.getType();

        return twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.POST);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsResponse<TwitterMobileAppCard> createAppDownloadCard(String accountId, String name,
            String countryCode, String iphoneAppId, String ipadAppId, String googlePlayAppId, String iphoneDeepLink,
            String ipadDeepLink, String googlePlayDeepLink, String imageMediaKey, String customAppDescription,
            String callToAction) throws TwitterException {
        List<HttpParameter> params =
                validateAndCreateParamsForCreateAppDownloadCard(accountId, name, countryCode, iphoneAppId, ipadAppId, googlePlayAppId,
                        iphoneDeepLink, ipadDeepLink, googlePlayDeepLink, customAppDescription, imageMediaKey,
                        callToAction);
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_APP_DOWNLOAD_CARDS;
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterMobileAppCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.POST);
    }

    @Override
    public BaseAdsResponse<TwitterMobileAppCard> updateAppDownloadCard(String accountId, String name, String cardId,
            String countryCode, String iphoneAppId, String ipadAppId, String googlePlayAppId, String iphoneDeepLink,
            String ipadDeepLink, String googlePlayDeepLink, String imageMediaKey, String customAppDescription,
            String callToAction) throws TwitterException {
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");

        List<HttpParameter> params =
                validateAndCreateParamsForUpdateAppDownloadCard(accountId, name, countryCode, iphoneAppId, ipadAppId, googlePlayAppId,
                        iphoneDeepLink, ipadDeepLink, googlePlayDeepLink, customAppDescription, imageMediaKey,
                        callToAction);
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_APP_DOWNLOAD_CARDS + cardId;
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterMobileAppCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.PUT);
    }

    @Override
    public BaseAdsResponse<TwitterImageAppDownloadCard> createImageAppDownloadCard(String accountId, String name,
            String countryCode, String iphoneAppId, String ipadAppId, String googlePlayAppId, String iphoneDeepLink,
            String ipadDeepLink, String googlePlayDeepLink, String imageMediaKey, String callToAction)
            throws TwitterException {
        List<HttpParameter> params =
                validateAndCreateParamsForCreateImageAppDownloadCard(accountId, name, countryCode, iphoneAppId, ipadAppId, googlePlayAppId,
                        iphoneDeepLink, ipadDeepLink, googlePlayDeepLink, imageMediaKey, callToAction);
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_IMAGE_APP_DOWNLOAD_CARDS;
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterImageAppDownloadCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.POST);
    }

    @Override
    public BaseAdsResponse<TwitterImageAppDownloadCard> updateImageAppDownloadCard(String accountId, String name,
            String cardId, String countryCode, String iphoneAppId, String ipadAppId, String googlePlayAppId,
            String iphoneDeepLink, String ipadDeepLink, String googlePlayDeepLink, String imageMediaKey,
            String callToAction) throws TwitterException {
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");

        List<HttpParameter> params =
                validateAndCreateParamsForUpdateImageAppDownloadCard(accountId, name, countryCode, iphoneAppId, ipadAppId, googlePlayAppId,
                        iphoneDeepLink, ipadDeepLink, googlePlayDeepLink, imageMediaKey, callToAction);
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_IMAGE_APP_DOWNLOAD_CARDS + cardId;
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterImageAppDownloadCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.PUT);
    }

    @Override
    public BaseAdsResponse<TwitterVideoAppDownloadCard> createVideoAppDownloadCard(String accountId, String name,
            String countryCode, String iphoneAppId, String ipadAppId, String googlePlayAppId, String iphoneDeepLink,
            String ipadDeepLink, String googlePlayDeepLink, String imageMediaKey, String callToAction,
            TwitterLibraryMedia video) throws TwitterException, IOException, InterruptedException {
        List<HttpParameter> params =
                validateAndCreateParamsForCreateVideoAppDownloadCard(accountId, name, countryCode, iphoneAppId, ipadAppId, googlePlayAppId,
                        iphoneDeepLink, ipadDeepLink, googlePlayDeepLink, imageMediaKey, video.getMediaKey(),
                        callToAction);
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_APP_DOWNLOAD_CARDS;
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterVideoAppDownloadCard>>() {
        }.getType();

        final BaseAdsResponse<TwitterVideoAppDownloadCard> twitterVideoAppDownloadResponse =
                twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.POST);

        final TwitterVideoAppDownloadCard videoAppDownloadCard = twitterVideoAppDownloadResponse.getData();
        if (video.getMediaKey() != null) {
            videoAppDownloadCard.setMediaKey(video.getMediaKey());
        }

        if (video.getMediaUrl() != null) {
            videoAppDownloadCard.setMediaUrl(video.getMediaUrl());
        }

        if (video.getDuration() != null) {
            videoAppDownloadCard.setChannelVideoLength(video.getDuration().toString());
        }
        videoAppDownloadCard.setChannelImageId(imageMediaKey);

        return twitterVideoAppDownloadResponse;
    }

    @Override
    public BaseAdsResponse<TwitterVideoAppDownloadCard> updateVideoAppDownloadCard(String accountId, String name,
            String cardId, String countryCode, String iphoneAppId, String ipadAppId, String googlePlayAppId,
            String iphoneDeepLink, String ipadDeepLink, String googlePlayDeepLink, String imageMediaKey,
            String callToActionValue, TwitterLibraryMedia video)
            throws TwitterException, IOException, InterruptedException {
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");

        String channelVideoKey = null;
        if (video != null) {
            channelVideoKey = video.getMediaKey();
        }

        List<HttpParameter> params =
                validateAndCreateParamsForUpdateVideoAppDownloadCard(accountId, name, countryCode, iphoneAppId, ipadAppId, googlePlayAppId,
                        iphoneDeepLink, ipadDeepLink, googlePlayDeepLink, imageMediaKey, channelVideoKey,
                        callToActionValue);
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_APP_DOWNLOAD_CARDS + cardId;
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterVideoAppDownloadCard>>() {
        }.getType();

        final BaseAdsResponse<TwitterVideoAppDownloadCard> twitterVideoAppDownloadResponse =
                twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.PUT);

        final TwitterVideoAppDownloadCard videoAppDownloadCard = twitterVideoAppDownloadResponse.getData();
        if (video != null) {
            videoAppDownloadCard.setMediaKey(video.getMediaKey());
        }

        if (video != null) {
            videoAppDownloadCard.setMediaUrl(video.getMediaUrl());
        }

        if (video != null) {
            videoAppDownloadCard.setChannelVideoLength(video.getDuration().toString());
        }
        videoAppDownloadCard.setChannelImageId(imageMediaKey);

        return twitterVideoAppDownloadResponse;
    }

    @Override
    public BaseAdsResponse<TwitterImageConversationCard> createImageConversationCard(String accountId, String name, String title, String firstHashtag,
                                                                                     String firstTweet, String secondHashtag, String secondTweet,
                                                                                     String thirdHashtag, String thirdTweet, String fourthHashtag,
                                                                                     String fourthTweet, String thanksText, String thanksUrl,
            String imageUrl, String imageMediaKey) throws TwitterException {

        final List<HttpParameter> params =
                validateAndCreateParamsForCreateImageConversationCard(accountId, name, title, firstHashtag, firstTweet, secondHashtag, secondTweet,
                        thirdHashtag, thirdTweet, fourthHashtag, fourthTweet, thanksText, thanksUrl,
                        imageUrl);

        params.add(new HttpParameter(PARAM_MEDIA_KEY, imageMediaKey));

        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_IMAGE_CONVERSATION_CARDS;
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterImageConversationCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.POST);
    }

    @Override
    public BaseAdsResponse<TwitterImageConversationCard> updateImageConversationCard(String accountId, String cardId, String name, String title, String firstHashtag, String firstTweet, String secondHashtag,
                                                                                     String secondTweet, String thirdHashtag, String thirdTweet,
                                                                                     String fourthHashtag, String fourthTweet, String thanksText,
            String thanksUrl, String imageUrl, String imageMediaKey)
            throws TwitterException {
        final BaseAdsResponse<TwitterImageConversationCard> response = getImageConversationCard(accountId, cardId);
        final TwitterImageConversationCard existingCard = response.getData();

        final List<HttpParameter> params =
                validateAndCreateParamsForUpdateImageConversationCard(accountId, name, title, firstHashtag, firstTweet, secondHashtag, secondTweet,
                        thirdHashtag, thirdTweet, fourthHashtag, fourthTweet, thanksText, thanksUrl,
                        imageUrl, existingCard);

        params.add(new HttpParameter(PARAM_MEDIA_KEY, imageMediaKey));

        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_IMAGE_CONVERSATION_CARDS + cardId;
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterImageConversationCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.PUT);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsResponse<TwitterImageConversationCard> deleteImageConversationCard(String accountId, String cardId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_IMAGE_CONVERSATION_CARDS + cardId;
        Type type = new TypeToken<BaseAdsResponse<TwitterImageConversationCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, null, type, HttpVerb.DELETE);
    }

    @Override
    public BaseAdsResponse<TwitterVideoConversationCard> createVideoConversationCard(String accountId, String name, String title, String firstHashtag,
                                                                                     String firstTweet, String secondHashtag, String secondTweet,
                                                                                     String thirdHashtag, String thirdTweet, String fourthHashtag,
                                                                                     String fourthTweet, String thanksText, String thanksUrl,
            String imageMediaKey, TwitterLibraryMedia twitterVideo)
            throws TwitterException {
        TwitterAdUtil.ensureNotNull(twitterVideo.getMediaKey(), "Video Key");
        final List<HttpParameter> params =
                validateAndCreateParamsForCreateVideoConversationCard(accountId, name, title, firstHashtag, firstTweet, secondHashtag, secondTweet,
                        thirdHashtag, thirdTweet, fourthHashtag, fourthTweet, thanksText, thanksUrl);
        if (isNotNullOrEmpty(imageMediaKey)) {
            params.add(new HttpParameter("poster_media_key", imageMediaKey));
        }
        params.add(new HttpParameter(PARAM_MEDIA_KEY, twitterVideo.getMediaKey()));

        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_CONVERSATION_CARDS;
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterVideoConversationCard>>() {
        }.getType();

        return twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.POST);
    }

    @Override
    public BaseAdsResponse<TwitterVideoConversationCard> updateVideoConversationCard(String accountId, String cardId, String name, String title, String firstHashtag, String firstTweet, String secondHashtag,
                                                                                     String secondTweet, String thirdHashtag, String thirdTweet,
                                                                                     String fourthHashtag, String fourthTweet, String thanksText,
            String thanksUrl, String imageMediaKey,
                                                                                     TwitterLibraryMedia twitterVideo) throws TwitterException {
        final BaseAdsResponse<TwitterVideoConversationCard> response = getVideoConversationCard(accountId, cardId);
        final TwitterVideoConversationCard existingCard = response.getData();

        final List<HttpParameter> params =
                validateAndCreateParamsForUpdateVideoConversationCard(accountId, name, title, firstHashtag, firstTweet, secondHashtag, secondTweet,
                        thirdHashtag, thirdTweet, fourthHashtag, fourthTweet, thanksText, thanksUrl,
                        existingCard);


        if (isNotNullOrEmpty(imageMediaKey)) {
            params.add(new HttpParameter("poster_media_key", imageMediaKey));
        }

        if (twitterVideo != null) {
            params.add(new HttpParameter(PARAM_MEDIA_KEY, twitterVideo.getMediaKey()));
        }

        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_CONVERSATION_CARDS + cardId;
        HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterVideoConversationCard>>() {
        }.getType();

        return twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.PUT);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsResponse<TwitterVideoConversationCard> deleteVideoConversationCard(String accountId, String cardId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_CONVERSATION_CARDS + cardId;
        Type type = new TypeToken<BaseAdsResponse<TwitterVideoConversationCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, null, type, HttpVerb.DELETE);
    }

    @Override
    public BaseAdsResponse<TwitterVideoWebsiteCard> createVideoWebsiteCard(String accountId, String name, String title, String videoKey, String websiteUrl) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        TwitterAdUtil.ensureNotNull(name, "Name");
        TwitterAdUtil.ensureNotNull(title, "Title");
        TwitterAdUtil.ensureNotNull(videoKey, "Video Key");
        TwitterAdUtil.ensureNotNull(websiteUrl, "Website url");

        verifyLength(name, "Name", MAX_VIDEO_WEBSITE_CARD_NAME_LENGTH);
        verifyLength(title, "Title", MAX_VIDEO_WEBSITE_CARD_TITLE_LENGTH);

        final List<HttpParameter> params = validateAndCreateParamsForCreateVideoWebsiteCard(accountId, name, title, videoKey, websiteUrl);
        final String url = twitterAdsClient.getBaseAdsAPIUrl() + PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_WEBSITE_CARDS;
        final HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterVideoWebsiteCard>>() {
        }.getType();

        return twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.POST);

    }

    @Override
    public BaseAdsResponse<TwitterVideoWebsiteCard> updateVideoWebsiteCard(String accountId, String cardId, String name, String title, String videoKey, String websiteUrl) throws TwitterException {

        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");
        final List<HttpParameter> params =
                validateAndCreateParamsForUpdateVideoWebsiteCard(name, title, videoKey, websiteUrl);

        final String url = twitterAdsClient.getBaseAdsAPIUrl() + PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_WEBSITE_CARDS + cardId;
        final HttpParameter[] parameters = params.toArray(new HttpParameter[params.size()]);
        Type type = new TypeToken<BaseAdsResponse<TwitterVideoWebsiteCard>>() {
        }.getType();

        return twitterAdsClient.executeHttpRequest(url, parameters, type, HttpVerb.PUT);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public BaseAdsResponse<TwitterVideoWebsiteCard> deleteVideoWebsiteCard(String accountId, String cardId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        TwitterAdUtil.ensureNotNull(cardId, "Card Id");

        final String url = twitterAdsClient.getBaseAdsAPIUrl() + PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_WEBSITE_CARDS + cardId;
        Type type = new TypeToken<BaseAdsResponse<TwitterVideoWebsiteCard>>() {
        }.getType();

        return twitterAdsClient.executeHttpRequest(url, null, type, HttpVerb.DELETE);
    }

    @Override
    public String postVideoCardImage(String imageTonLocation) throws TwitterException {
        TwitterAdUtil.ensureNotNull(imageTonLocation, "imageTonLocation");
        List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter("location", imageTonLocation));
        TwitterUUIDResponse twitterUUIDResponse = twitterAdsClient
                .executeRequest(UPLOAD_VIDEO_CARD_IMAGE_URL, params.toArray(new HttpParameter[params.size()]), TwitterUUIDResponse.class,
                        HttpVerb.POST);
        return twitterUUIDResponse.getuUID();
    }

// --------------------------------------------------------------------- PRIVATE METHODS  ---------------------------------------------

    private String getMediaId(String image) {
        Media media;
        try {
            TwitterAdUtil.ensureNotNull(image, "image");
            InputStream fileBody = new URL(image).openStream();
            media = twitterAdsClient.upload(new MediaUpload(fileBody));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (media != null) {
            return media.getMediaIdString();
        }
        return null;
    }

    private BaseAdsResponse<TwitterVideoConversationCard> getVideoConversationCard(String accountId, String cardId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_VIDEO_CONVERSATION_CARDS + cardId;
        Type type = new TypeToken<BaseAdsResponse<TwitterVideoConversationCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, null, type, HttpVerb.GET);
    }

    private List<HttpParameter> validateAndCreateParamsForCreateWebsiteCard(String accountId, String name, String websiteTitle, String websiteUrl,
            String mediaKey) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");
        TwitterAdUtil.ensureNotNull(name, "Name");
        TwitterAdUtil.ensureNotNull(websiteTitle, "WebsiteTitle");
        TwitterAdUtil.ensureNotNull(websiteUrl, "WebsiteUrl");

        final List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_NAME, name));
        params.add(new HttpParameter(PARAM_WEBSITE_TITLE, websiteTitle));
        params.add(new HttpParameter(PARAM_WEBSITE_URL, websiteUrl));

        if (isNotNullOrEmpty(mediaKey)) {
            params.add(new HttpParameter(PARAM_MEDIA_KEY, mediaKey));
        }

        return params;
    }

    private void verifyHashtagLength(String hashtag, String label) throws TwitterException {
        if (hashtag.length() > CONVERSATION_CARD_HASHTAG_LENGTH) {
            throw new TwitterException(
                    new UnsupportedOperationException(label + " cannot be more than " + CONVERSATION_CARD_HASHTAG_LENGTH + " characters"));
        }
    }

    private void verifyTweetLength(String tweet, String label) throws TwitterException {
        if (tweet.length() > CONVERSATION_CARD_TWEET_LENGTH) {
            throw new TwitterException(
                    new UnsupportedOperationException(label + " cannot be more than " + CONVERSATION_CARD_TWEET_LENGTH + " characters"));
        }
    }

    private List<HttpParameter> validateAndCreateParamsForUpdateConversationCard(String accountId, String name, String title, String firstHashtag,
                                                                                 String firstTweet, String secondHashtag, String secondTweet,
                                                                                 String thirdHashtag, String thirdTweet, String fourthHashtag,
                                                                                 String fourthTweet,
                                                                                 String thanksText, String thanksUrl,
                                                                                 AbstractConversationCard existingCard) throws TwitterException {
        final List<HttpParameter> params =
                validateAndCreateParamsForCreateConversationCard(accountId, name, title, firstHashtag, firstTweet, secondHashtag, secondTweet,
                        thirdHashtag, thirdTweet, fourthHashtag, fourthTweet, thanksText, thanksUrl);

        if (!isNotNullOrEmpty(title) && isNotNullOrEmpty(existingCard.getTitle())) {
            params.add(new HttpParameter(PARAM_TITLE, ""));
        }

        if (!isNotNullOrEmpty(secondHashtag) && isNotNullOrEmpty(existingCard.getSecondHashtag())) {
            params.add(new HttpParameter(PARAM_SECOND_CTA, ""));
        }

        if (!isNotNullOrEmpty(secondTweet) && isNotNullOrEmpty(existingCard.getSecondTweet())) {
            params.add(new HttpParameter(PARAM_SECOND_CTA_TWEET, ""));
        }

        if (!isNotNullOrEmpty(thirdHashtag) && isNotNullOrEmpty(existingCard.getThirdHashtag())) {
            params.add(new HttpParameter(PARAM_THIRD_CTA, ""));
        }

        if (!isNotNullOrEmpty(thirdTweet) && isNotNullOrEmpty(existingCard.getThirdTweet())) {
            params.add(new HttpParameter(PARAM_THIRD_CTA_TWEET, ""));
        }

        if (!isNotNullOrEmpty(fourthHashtag) && isNotNullOrEmpty(existingCard.getFourthHashtag())) {
            params.add(new HttpParameter(PARAM_FOURTH_CTA, ""));
        }

        if (!isNotNullOrEmpty(fourthTweet) && isNotNullOrEmpty(existingCard.getFourthTweet())) {
            params.add(new HttpParameter(PARAM_FOURTH_CTA_TWEET, ""));
        }

        if (!isNotNullOrEmpty(thanksUrl) && isNotNullOrEmpty(existingCard.getThankUrl())) {
            params.add(new HttpParameter(PARAM_THANK_YOU_URL, ""));
        }

        return params;
    }

    @SuppressWarnings("Duplicates")
    private List<HttpParameter> validateAndCreateParamsForCreateAppDownloadCard(String accountId, String name, String countryCode,
                                                                                String iphoneAppId, String ipadAppId, String googlePlayAppId,
                                                                                String iphoneDeepLink, String ipadDeepLink, String googlePlayDeepLink,
            String customAppDescription, String mediaKey, String callToAction)
            throws TwitterException {
        List<HttpParameter> params =
                getCardHttpParameters(accountId, name, countryCode, iphoneAppId, ipadAppId, googlePlayAppId, iphoneDeepLink, ipadDeepLink,
                        googlePlayDeepLink);

        if (isNotNullOrEmpty(mediaKey)) {
            params.add(new HttpParameter(PARAM_MEDIA_KEY, mediaKey));
        }

        if (isNotNullOrEmpty(callToAction)) {
            params.add(new HttpParameter(APP_CTA, callToAction));
        }

        if (isNotNullOrEmpty(customAppDescription)) {
            params.add(new HttpParameter(PARAM_CUSTOM_APP_DESCRIPTION, customAppDescription));
        }

        return params;
    }

    private List<HttpParameter> validateAndCreateParamsForUpdateVideoConversationCard(String accountId, String name, String title,
                                                                                      String firstHashtag, String firstTweet, String secondHashtag,
                                                                                      String secondTweet, String thirdHashtag, String thirdTweet,
                                                                                      String fourthHashtag, String fourthTweet, String thanksText,
                                                                                      String thanksUrl, TwitterVideoConversationCard existingCard)
            throws TwitterException {
        return validateAndCreateParamsForUpdateConversationCard(accountId, name, title, firstHashtag, firstTweet, secondHashtag, secondTweet,
                thirdHashtag, thirdTweet, fourthHashtag, fourthTweet, thanksText, thanksUrl,
                existingCard);
    }

    private List<HttpParameter> getCardHttpParameters(String accountId, String name, String countryCode, String iphoneAppId, String ipadAppId,
                                                      String googlePlayAppId, String iphoneDeepLink, String ipadDeepLink, String googlePlayDeepLink)
            throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");
        TwitterAdUtil.ensureNotNull(countryCode, "App Country Code");
        TwitterAdUtil.ensureNotNull(name, "Name");

        final List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_NAME, name));
        params.add(new HttpParameter(PARAM_COUNTRY_CODE, countryCode));

        // This is done in order to satisfy the condition: atleast one of the app ids is provided
        if (!(TwitterAdUtil.isNotNullOrEmpty(googlePlayAppId) || TwitterAdUtil.isNotNullOrEmpty(ipadAppId) ||
                TwitterAdUtil.isNotNullOrEmpty(iphoneAppId))) {
            throw new TwitterException(
                    new UnsupportedOperationException("Please provide atleast one valid store app id to create an app download card"));
        }

        if (isNotNullOrEmpty(iphoneAppId)) {
            params.add(new HttpParameter(PARAM_IPHONE_APP_ID, iphoneAppId));
            if (isNotNullOrEmpty(iphoneDeepLink)) {
                params.add(new HttpParameter(PARAM_IPHONE_DEEP_LINK, iphoneDeepLink));
            }
        }

        if (isNotNullOrEmpty(ipadAppId)) {
            params.add(new HttpParameter(PARAM_IPAD_APP_ID, ipadAppId));
            if (isNotNullOrEmpty(ipadDeepLink)) {
                params.add(new HttpParameter(PARAM_IPAD_DEEP_LINK, ipadDeepLink));
            }
        }

        if (isNotNullOrEmpty(googlePlayAppId)) {
            params.add(new HttpParameter(PARAM_GOOGLEPLAY_APP_ID, googlePlayAppId));
            if (isNotNullOrEmpty(googlePlayDeepLink)) {
                params.add(new HttpParameter(PARAM_GOOGLEPLAY_DEEP_LINK, googlePlayDeepLink));
            }
        }

        return params;
    }

    private List<HttpParameter> validateAndCreateParamsForLeadGenerationCardStat(String accountId, String cardId, String startTime, Optional<String> endTime,
                                                                                 Optional<String> granularity, Optional<String> metric, Optional<Boolean> withDeleted) {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");
        TwitterAdUtil.ensureNotNull(cardId, "CardId");
        TwitterAdUtil.ensureNotNull(startTime, "StartTime");

        List<HttpParameter> params = new ArrayList<>();
        params.add(new HttpParameter(PARAM_START_TIME, startTime));

        if (endTime != null && endTime.isPresent()) {
            params.add(new HttpParameter(PARAM_END_TIME, endTime.get()));
        }
        if (granularity != null && granularity.isPresent()) {
            params.add(new HttpParameter(PARAM_GRANULARITY, granularity.get()));
        }
        if (metric != null && metric.isPresent()) {
            params.add(new HttpParameter(PARAM_METRICS, metric.get()));
        }
        if (withDeleted != null && withDeleted.isPresent()) {
            params.add(new HttpParameter(PARAM_WITH_DELETED, withDeleted.get()));
        }

        return params;
    }

    private BaseAdsResponse<TwitterImageConversationCard> getImageConversationCard(String accountId, String cardId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        String url = twitterAdsClient.getBaseAdsAPIUrl() + TwitterAdsConstants.PREFIX_ACCOUNTS_URI_5 + accountId
                + PATH_IMAGE_CONVERSATION_CARDS + cardId;
        Type type = new TypeToken<BaseAdsResponse<TwitterImageConversationCard>>() {
        }.getType();
        return twitterAdsClient.executeHttpRequest(url, null, type, HttpVerb.GET);
    }

    private List<HttpParameter> validateAndCreateParamsForUpdateAppDownloadCard(String accountId, String name, String countryCode,
                                                                                String iphoneAppId, String ipadAppId, String googlePlayAppId,
                                                                                String iphoneDeepLink, String ipadDeepLink, String googlePlayDeepLink,
            String customAppDescription, String mediaKey, String callToAction)
            throws TwitterException {
        List<HttpParameter> params =
                getCardHttpParametersForUpdate(accountId, name, countryCode, iphoneAppId, ipadAppId, googlePlayAppId, iphoneDeepLink, ipadDeepLink,
                        googlePlayDeepLink);

        if (isNotNullOrEmpty(mediaKey)) {
            params.add(new HttpParameter(PARAM_MEDIA_KEY, mediaKey));
        }

        if (isNotNullOrEmpty(callToAction)) {
            params.add(new HttpParameter(APP_CTA, callToAction));
        }

        if (isNotNullOrEmpty(customAppDescription)) {
            params.add(new HttpParameter(PARAM_CUSTOM_APP_DESCRIPTION, customAppDescription));
        }

        return params;
    }

    private List<HttpParameter> validateAndCreateParamsForCreateVideoConversationCard(String accountId, String name, String title, String firstHashtag,
                                                                                      String firstTweet, String secondHashtag, String secondTweet,
                                                                                      String thirdHashtag, String thirdTweet, String fourthHashtag,
                                                                                      String fourthTweet, String thanksText, String thanksUrl)
            throws TwitterException {
        return validateAndCreateParamsForCreateConversationCard(accountId, name, title, firstHashtag, firstTweet, secondHashtag, secondTweet,
                thirdHashtag, thirdTweet, fourthHashtag, fourthTweet, thanksText, thanksUrl);
    }

    private List<HttpParameter> getCardHttpParametersForUpdate(String accountId, String name, String countryCode, String iphoneAppId,
                                                               String ipadAppId, String googlePlayAppId, String iphoneDeepLink, String ipadDeepLink,
                                                               String googlePlayDeepLink) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");
        TwitterAdUtil.ensureNotNull(countryCode, "App Country Code");
        TwitterAdUtil.ensureNotNull(name, "Name");

        final List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_NAME, name));
        params.add(new HttpParameter(PARAM_COUNTRY_CODE, countryCode));

        // This is done in order to satisfy the condition: atleast one of the app ids is provided
        if (!(TwitterAdUtil.isNotNullOrEmpty(googlePlayAppId) || TwitterAdUtil.isNotNullOrEmpty(ipadAppId) ||
                TwitterAdUtil.isNotNullOrEmpty(iphoneAppId))) {
            throw new TwitterException(
                    new UnsupportedOperationException("Please provide atleast one valid store app id to create an app download card"));
        }

        iphoneAppId = iphoneAppId == null ? "" : iphoneAppId;
        iphoneDeepLink = iphoneDeepLink == null ? "" : iphoneDeepLink;
        ipadAppId = ipadAppId == null ? "" : ipadAppId;
        ipadDeepLink = ipadDeepLink == null ? "" : ipadDeepLink;
        googlePlayAppId = googlePlayAppId == null ? "" : googlePlayAppId;
        googlePlayDeepLink = googlePlayDeepLink == null ? "" : googlePlayDeepLink;

        params.add(new HttpParameter(PARAM_IPHONE_APP_ID, iphoneAppId));
        params.add(new HttpParameter(PARAM_IPHONE_DEEP_LINK, iphoneDeepLink));
        params.add(new HttpParameter(PARAM_IPAD_APP_ID, ipadAppId));
        params.add(new HttpParameter(PARAM_IPAD_DEEP_LINK, ipadDeepLink));
        params.add(new HttpParameter(PARAM_GOOGLEPLAY_APP_ID, googlePlayAppId));
        params.add(new HttpParameter(PARAM_GOOGLEPLAY_DEEP_LINK, googlePlayDeepLink));

        return params;
    }

    private List<HttpParameter> validateAndCreateParamsForCreateImageAppDownloadCard(String accountId, String name, String countryCode,
                                                                                     String iphoneAppId, String ipadAppId, String googlePlayAppId,
                                                                                     String iphoneDeepLink, String ipadDeepLink,
            String googlePlayDeepLink, String mediaKey, String callToAction)
            throws TwitterException {
        List<HttpParameter> params =
                getCardHttpParameters(accountId, name, countryCode, iphoneAppId, ipadAppId, googlePlayAppId, iphoneDeepLink, ipadDeepLink,
                        googlePlayDeepLink);

        if (isNotNullOrEmpty(mediaKey)) {
            params.add(new HttpParameter(PARAM_MEDIA_KEY, mediaKey));
        }

        if (isNotNullOrEmpty(callToAction)) {
            params.add(new HttpParameter(APP_CTA, callToAction));
        }

        return params;
    }

    private List<HttpParameter> validateAndCreateParamsForUpdateImageAppDownloadCard(String accountId, String name, String countryCode,
                                                                                     String iphoneAppId, String ipadAppId, String googlePlayAppId,
                                                                                     String iphoneDeepLink, String ipadDeepLink,
            String googlePlayDeepLink, String mediaKey, String callToAction)
            throws TwitterException {
        List<HttpParameter> params =
                getCardHttpParametersForUpdate(accountId, name, countryCode, iphoneAppId, ipadAppId, googlePlayAppId, iphoneDeepLink, ipadDeepLink,
                        googlePlayDeepLink);

        if (isNotNullOrEmpty(mediaKey)) {
            params.add(new HttpParameter(PARAM_MEDIA_KEY, mediaKey));
        }

        if (isNotNullOrEmpty(callToAction)) {
            params.add(new HttpParameter(APP_CTA, callToAction));
        }

        return params;

    }

    private List<HttpParameter> validateAndCreateParamsForCreateImageConversationCard(String accountId, String name, String title,
                                                                                      String firstHashtag, String firstTweet, String secondHashtag,
                                                                                      String secondTweet, String thirdHashtag, String thirdTweet,
                                                                                      String fourthHashtag, String fourthTweet, String thanksText,
                                                                                      String thanksUrl, String imageUrl) throws TwitterException {
        TwitterAdUtil.ensureNotNull(imageUrl, "Image");
        return validateAndCreateParamsForCreateConversationCard(accountId, name, title, firstHashtag, firstTweet, secondHashtag, secondTweet,
                thirdHashtag, thirdTweet, fourthHashtag, fourthTweet, thanksText, thanksUrl);
    }

    private List<HttpParameter> validateAndCreateParamsForCreateConversationCard(String accountId, String name, String title, String firstHashtag,
                                                                                 String firstTweet, String secondHashtag, String secondTweet,
                                                                                 String thirdHashtag, String thirdTweet, String fourthHashtag,
                                                                                 String fourthTweet, String thanksText, String thanksUrl)
            throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        TwitterAdUtil.ensureNotNull(name, "Name");
        TwitterAdUtil.ensureNotNull(firstHashtag, "First Cta Hashtag");
        TwitterAdUtil.ensureNotNull(firstTweet, "First Cta Tweet");
        TwitterAdUtil.ensureNotNull(thanksText, "Thanks tweet text");

        verifyHashtagLength(firstHashtag, "First hashtag");
        verifyTweetLength(firstTweet, "First Tweet");

        if (isNotNullOrEmpty(title) && isNotNullOrEmpty(secondHashtag)) {
            throw new TwitterException(new UnsupportedOperationException("Card Title cannot be used with second hashtag"));
        }

        if (!isNotNullOrEmpty(title) && !isNotNullOrEmpty(secondHashtag)) {
            throw new TwitterException(new UnsupportedOperationException("Atleast one of card title or second hashtag is compulsory"));
        }

        if (isNotNullOrEmpty(secondHashtag) && !isNotNullOrEmpty(secondTweet)) {
            throw new TwitterException(new UnsupportedOperationException("Please provide tweet along with second hashtag"));
        }

        final List<HttpParameter> params = new ArrayList<>();
        params.add(new HttpParameter(PARAM_NAME, name));
        params.add(new HttpParameter(PARAM_FIRST_CTA, firstHashtag));
        params.add(new HttpParameter(PARAM_FIRST_CTA_TWEET, firstTweet));
        params.add(new HttpParameter(PARAM_THANK_YOU_TEXT, thanksText));

        if (TwitterAdUtil.isNotNull(title)) {
            params.add(new HttpParameter(PARAM_TITLE, title));
        }

        if (TwitterAdUtil.isNotNull(secondHashtag)) {
            verifyHashtagLength(secondHashtag, "Second hashtag");
            params.add(new HttpParameter(PARAM_SECOND_CTA, secondHashtag));
        }

        if (TwitterAdUtil.isNotNull(secondTweet)) {
            verifyTweetLength(secondTweet, "Second Tweet");
            params.add(new HttpParameter(PARAM_SECOND_CTA_TWEET, secondTweet));
        }

        if (TwitterAdUtil.isNotNull(thirdHashtag)) {
            verifyHashtagLength(thirdHashtag, "Third hashtag");
            params.add(new HttpParameter(PARAM_THIRD_CTA, thirdHashtag));
        }

        if (TwitterAdUtil.isNotNull(thirdTweet)) {
            verifyTweetLength(thirdTweet, "Third Tweet");
            params.add(new HttpParameter(PARAM_THIRD_CTA_TWEET, thirdTweet));
        }

        if (TwitterAdUtil.isNotNull(fourthHashtag)) {
            verifyHashtagLength(fourthHashtag, "Fourth hashtag");
            params.add(new HttpParameter(PARAM_FOURTH_CTA, fourthHashtag));
        }

        if (TwitterAdUtil.isNotNull(fourthTweet)) {
            verifyTweetLength(fourthTweet, "Fourth Tweet");
            params.add(new HttpParameter(PARAM_FOURTH_CTA_TWEET, fourthTweet));
        }

        if (TwitterAdUtil.isNotNull(thanksUrl)) {
            params.add(new HttpParameter(PARAM_THANK_YOU_URL, thanksUrl));
        }

        return params;
    }

    @SuppressWarnings("Duplicates")
    private List<HttpParameter> validateAndCreateParamsForCreateVideoAppDownloadCard(String accountId, String name, String countryCode,
                                                                                     String iphoneAppId, String ipadAppId, String googlePlayAppId,
                                                                                     String iphoneDeepLink, String ipadDeepLink,
            String googlePlayDeepLink, String posterMediaKey,
            String videoMediaKey, String callToAction)
            throws TwitterException {

        List<HttpParameter> params =
                getCardHttpParameters(accountId, name, countryCode, iphoneAppId, ipadAppId, googlePlayAppId, iphoneDeepLink, ipadDeepLink,
                        googlePlayDeepLink);

        if (isNotNullOrEmpty(videoMediaKey)) {
            params.add(new HttpParameter(PARAM_MEDIA_KEY, videoMediaKey));
        }

        if (isNotNullOrEmpty(posterMediaKey)) {
            params.add(new HttpParameter("poster_media_key", posterMediaKey));
        }

        if (isNotNullOrEmpty(callToAction)) {
            params.add(new HttpParameter(APP_CTA, callToAction));
        }

        return params;
    }


    private List<HttpParameter> validateAndCreateParamsForUpdateVideoAppDownloadCard(String accountId, String name, String countryCode,
                                                                                     String iphoneAppId, String ipadAppId, String googlePlayAppId,
                                                                                     String iphoneDeepLink, String ipadDeepLink,
            String googlePlayDeepLink, String imageMediaKey,
            String videoMediaKey, String callToAction)
            throws TwitterException {

        List<HttpParameter> params =
                getVideoCardHttpParametersForUpdate(accountId, name, countryCode, iphoneAppId, ipadAppId, googlePlayAppId, iphoneDeepLink,
                        ipadDeepLink, googlePlayDeepLink);

        if (isNotNullOrEmpty(imageMediaKey)) {
            params.add(new HttpParameter("poster_media_key", imageMediaKey));
        }

        if (isNotNullOrEmpty(videoMediaKey)) {
            params.add(new HttpParameter(PARAM_MEDIA_KEY, videoMediaKey));
        }

        if (isNotNullOrEmpty(callToAction)) {
            params.add(new HttpParameter(APP_CTA, callToAction));
        }

        return params;
    }

    private List<HttpParameter> getVideoCardHttpParametersForUpdate(String accountId, String name, String countryCode, String iphoneAppId,
                                                                    String ipadAppId, String googlePlayAppId, String iphoneDeepLink,
                                                                    String ipadDeepLink, String googlePlayDeepLink) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");
        TwitterAdUtil.ensureNotNull(countryCode, "App Country Code");
        TwitterAdUtil.ensureNotNull(name, "Name");

        final List<HttpParameter> params = Lists.newArrayList();
        params.add(new HttpParameter(PARAM_NAME, name));
        params.add(new HttpParameter(PARAM_COUNTRY_CODE, countryCode));

        // This is done in order to satisfy the condition: atleast one of the app ids is provided
        if (!(TwitterAdUtil.isNotNullOrEmpty(googlePlayAppId) || TwitterAdUtil.isNotNullOrEmpty(ipadAppId) ||
                TwitterAdUtil.isNotNullOrEmpty(iphoneAppId))) {
            throw new TwitterException(
                    new UnsupportedOperationException("Please provide atleast one valid store app id to create an app download card"));
        }

        if (TwitterAdUtil.isNotNull(iphoneAppId)) {
            params.add(new HttpParameter(PARAM_IPHONE_APP_ID, iphoneAppId));
            if (TwitterAdUtil.isNotNull(iphoneDeepLink)) {
                params.add(new HttpParameter(PARAM_IPHONE_DEEP_LINK, iphoneDeepLink));
            }
        }

        if (TwitterAdUtil.isNotNull(ipadAppId)) {
            params.add(new HttpParameter(PARAM_IPAD_APP_ID, ipadAppId));
            if (TwitterAdUtil.isNotNull(ipadDeepLink)) {
                params.add(new HttpParameter(PARAM_IPAD_DEEP_LINK, ipadDeepLink));
            }
        }

        if (TwitterAdUtil.isNotNull(googlePlayAppId)) {
            params.add(new HttpParameter(PARAM_GOOGLEPLAY_APP_ID, googlePlayAppId));
            if (TwitterAdUtil.isNotNull(googlePlayDeepLink)) {
                params.add(new HttpParameter(PARAM_GOOGLEPLAY_DEEP_LINK, googlePlayDeepLink));
            }
        }
        return params;
    }

    private List<HttpParameter> validateAndCreateParamsForUpdateImageConversationCard(String accountId, String name, String title,
                                                                                      String firstHashtag, String firstTweet, String secondHashtag,
                                                                                      String secondTweet, String thirdHashtag, String thirdTweet,
                                                                                      String fourthHashtag, String fourthTweet, String thanksText,
                                                                                      String thanksUrl, String imageUrl,
                                                                                      TwitterImageConversationCard existingCard)
            throws TwitterException {

        TwitterAdUtil.ensureNotNull(imageUrl, "Image");
        return validateAndCreateParamsForUpdateConversationCard(accountId, name, title, firstHashtag, firstTweet, secondHashtag, secondTweet,
                thirdHashtag, thirdTweet, fourthHashtag, fourthTweet, thanksText, thanksUrl,
                existingCard);
    }

    private List<HttpParameter> validateAndCreateParamsForCreateVideoWebsiteCard(String accountId, String name,
            String title, String videoKey,
                                                                                 String websiteUrl) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "Account Id");
        TwitterAdUtil.ensureNotNull(name, "Name");
        TwitterAdUtil.ensureNotNull(title, "Title");
        TwitterAdUtil.ensureNotNull(videoKey, "Video Id");
        TwitterAdUtil.ensureNotNull(websiteUrl, "Website url");

        verifyLength(name, "Name", MAX_VIDEO_WEBSITE_CARD_NAME_LENGTH);
        verifyLength(title, "Title", MAX_VIDEO_WEBSITE_CARD_TITLE_LENGTH);

        final List<HttpParameter> params = new ArrayList<>();
        params.add(new HttpParameter(PARAM_NAME, name));
        params.add(new HttpParameter(PARAM_TITLE, title));
        params.add(new HttpParameter(PARAM_MEDIA_KEY, videoKey));
        params.add(new HttpParameter(PARAM_WEBSITE_URL, websiteUrl));

        return params;
    }

    private List<HttpParameter> validateAndCreateParamsForUpdateVideoWebsiteCard(String name, String title,
            String videoKey, String websiteUrl)
            throws TwitterException {
        final List<HttpParameter> params = new ArrayList<>();
        if (isNotNullOrEmpty(name)) {
            verifyLength(name, "Name", MAX_VIDEO_WEBSITE_CARD_NAME_LENGTH);
            params.add(new HttpParameter(PARAM_NAME, name));
        }

        if (isNotNullOrEmpty(title)) {
            verifyLength(title, "Title", MAX_VIDEO_WEBSITE_CARD_TITLE_LENGTH);
            params.add(new HttpParameter(PARAM_TITLE, title));
        }

        if (isNotNullOrEmpty(videoKey)) {
            params.add(new HttpParameter(PARAM_MEDIA_KEY, videoKey));
        }

        if (isNotNullOrEmpty(websiteUrl)) {
            params.add(new HttpParameter(PARAM_WEBSITE_URL, websiteUrl));
        }

        return params;
    }

    private void verifyLength(String field, String label, long length) throws TwitterException {
        if (field.length() > length) {
            throw new TwitterException(new UnsupportedOperationException(label + " cannot be more than " + length + " characters"));
        }
    }

    private List<HttpParameter> validateAndCreateParamsForCreateImageDMCard(String accountId, String name, String firstCta,
                                                                            Long firstWelcomeMessageId,
                                                                            String secondCta, Long secondWelcomeMessageId, String thirdCta,
                                                                            Long thirdWelcomeMessageId, String fourthCta, Long fourthWelcomeMessageId,
                                                                            Long recipientAccountId, String imageUrl) throws TwitterException {
        TwitterAdUtil.ensureNotNull(imageUrl, "Image Url");
        return validateAndCreateParamsForCreateDMCard(accountId, name, firstCta, firstWelcomeMessageId, secondCta, secondWelcomeMessageId, thirdCta,
                thirdWelcomeMessageId, fourthCta, fourthWelcomeMessageId, recipientAccountId);
    }

    private List<HttpParameter> validateAndCreateParamsForCreateVideoDMCard(String accountId, String name, String firstCta,
                                                                            Long firstWelcomeMessageId,
                                                                            String secondCta, Long secondWelcomeMessageId, String thirdCta,
                                                                            Long thirdWelcomeMessageId, String fourthCta, Long fourthWelcomeMessageId,
                                                                            Long recipientAccountId, String videoUrl) throws TwitterException {

        TwitterAdUtil.ensureNotNull(videoUrl, "Video Url");
        return validateAndCreateParamsForCreateDMCard(accountId, name, firstCta, firstWelcomeMessageId, secondCta, secondWelcomeMessageId, thirdCta,
                thirdWelcomeMessageId, fourthCta, fourthWelcomeMessageId, recipientAccountId);
    }

    private List<HttpParameter> validateAndCreateParamsForUpdateDMCard(String accountId, String name, String firstCta,
                                                                       Long firstWelcomeMessageId,
                                                                       String secondCta, Long secondWelcomeMessageId, String thirdCta,
                                                                       Long thirdWelcomeMessageId, String fourthCta, Long fourthWelcomeMessageId,
                                                                       String channelId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");
        TwitterAdUtil.ensureNotNull(channelId, "Card Channel Id");

        final List<HttpParameter> params = new ArrayList<>();
        if (StringUtils.isNotBlank(name)) {
            params.add(new HttpParameter(PARAM_NAME, name));
        }

        if (TwitterAdUtil.isNotNull(firstCta) && TwitterAdUtil.isNotNull(firstWelcomeMessageId)) {
            params.add(new HttpParameter(PARAM_FIRST_CTA, firstCta));
            params.add(new HttpParameter(PARAM_FIRST_CTA_WELCOME_MESSAGE_ID, firstWelcomeMessageId));
        }

        if (TwitterAdUtil.isNotNull(secondCta) && TwitterAdUtil.isNotNull(secondWelcomeMessageId)) {
            params.add(new HttpParameter(PARAM_SECOND_CTA, secondCta));
            params.add(new HttpParameter(PARAM_SECOND_CTA_WELCOME_MESSAGE_ID, secondWelcomeMessageId));
        }

        if (TwitterAdUtil.isNotNull(thirdCta) && TwitterAdUtil.isNotNull(thirdWelcomeMessageId)) {
            params.add(new HttpParameter(PARAM_THIRD_CTA, thirdCta));
            params.add(new HttpParameter(PARAM_THIRD_CTA_WELCOME_MESSAGE_ID, thirdWelcomeMessageId));
        }

        if (TwitterAdUtil.isNotNull(fourthCta) && TwitterAdUtil.isNotNull(fourthWelcomeMessageId)) {
            params.add(new HttpParameter(PARAM_FOURTH_CTA, fourthCta));
            params.add(new HttpParameter(PARAM_FOURTH_CTA_WELCOME_MESSAGE_ID, fourthWelcomeMessageId));
        }

        return params;
    }

    private List<HttpParameter> validateAndCreateParamsForCreateDMCard(String accountId, String name, String firstCta, Long firstWelcomeMessageId,
                                                                       String secondCta, Long secondWelcomeMessageId, String thirdCta,
                                                                       Long thirdWelcomeMessageId, String fourthCta, Long fourthWelcomeMessageId,
                                                                       Long recipientAccountId) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "AccountId");
        TwitterAdUtil.ensureNotNull(name, "Card Name");
        TwitterAdUtil.ensureNotNull(firstCta, "First Cta");
        TwitterAdUtil.ensureNotNull(firstWelcomeMessageId, "First Welcome Message ID");
        TwitterAdUtil.ensureNotNull(recipientAccountId, "Promotable Account ID");

        final List<HttpParameter> params = new ArrayList<>();
        params.add(new HttpParameter(PARAM_NAME, name));
        params.add(new HttpParameter(PARAM_RECIPIENT_USER_ID, recipientAccountId));
        params.add(new HttpParameter(PARAM_FIRST_CTA, firstCta));
        params.add(new HttpParameter(PARAM_FIRST_CTA_WELCOME_MESSAGE_ID, firstWelcomeMessageId));

        if (TwitterAdUtil.isNotNull(secondCta) && TwitterAdUtil.isNotNull(secondWelcomeMessageId)) {
            params.add(new HttpParameter(PARAM_SECOND_CTA, secondCta));
            params.add(new HttpParameter(PARAM_SECOND_CTA_WELCOME_MESSAGE_ID, secondWelcomeMessageId));
        }

        if (TwitterAdUtil.isNotNull(thirdCta) && TwitterAdUtil.isNotNull(thirdWelcomeMessageId)) {
            params.add(new HttpParameter(PARAM_THIRD_CTA, thirdCta));
            params.add(new HttpParameter(PARAM_THIRD_CTA_WELCOME_MESSAGE_ID, thirdWelcomeMessageId));
        }

        if (TwitterAdUtil.isNotNull(fourthCta) && TwitterAdUtil.isNotNull(fourthWelcomeMessageId)) {
            params.add(new HttpParameter(PARAM_FOURTH_CTA, fourthCta));
            params.add(new HttpParameter(PARAM_FOURTH_CTA_WELCOME_MESSAGE_ID, fourthWelcomeMessageId));
        }

        return params;
    }
}
