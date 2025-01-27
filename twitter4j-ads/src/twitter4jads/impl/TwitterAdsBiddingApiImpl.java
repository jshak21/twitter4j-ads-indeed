package twitter4jads.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import twitter4jads.TwitterAdsClient;
import twitter4jads.api.TwitterAdsBiddingApi;
import twitter4jads.internal.http.HttpParameter;
import twitter4jads.internal.models4j.TwitterException;
import twitter4jads.models.ads.HttpVerb;
import twitter4jads.models.ads.TwitterBidInfo;
import twitter4jads.util.TwitterAdUtil;

import java.lang.reflect.Type;
import java.util.List;


/**
 * User: prashant
 * Date: 22/04/16.
 * Time: 2:50 PM
 */
public class TwitterAdsBiddingApiImpl implements TwitterAdsBiddingApi {
    private final TwitterAdsClient twitterAdsClient;

    public TwitterAdsBiddingApiImpl(TwitterAdsClient twitterAdsClient) {
        this.twitterAdsClient = twitterAdsClient;
    }

    /*s
    This call does not hit any version of twitter ads api, it hits the same end point as is hit on native
    * */
    @Override
    public TwitterBidInfo getBidInfo(String accountId, String campaignType, Optional<String> currency, Optional<String> objectiveForBidding) throws TwitterException {
        TwitterAdUtil.ensureNotNull(accountId, "accountId");
        TwitterAdUtil.ensureNotNull(campaignType, "campaignType");
        List<HttpParameter> params = Lists.newArrayList();
        String baseUrl = "https://ads.twitter.com/" + "accounts/" + accountId + "/campaigns/bid_guidance";
        //noinspection ConstantConditions
        params.add(new HttpParameter("account", accountId));
        params.add(new HttpParameter("campaign_type", campaignType));
        if (currency != null &&currency.isPresent()) {
            params.add(new HttpParameter("currency", currency.get()));
        }
        if (objectiveForBidding != null && objectiveForBidding.isPresent()) {
            params.add(new HttpParameter("objective", objectiveForBidding.get()));
        }
        Type type = new TypeToken<TwitterBidInfo>() {}.getType();

        return twitterAdsClient.executeRequest(baseUrl, params.toArray(new HttpParameter[params.size()]), type, HttpVerb.GET);
    }
}
