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

import java.util.Date;

/**
 * A data interface representing one single status of a user.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public interface Status extends Comparable<Status>, TwitterResponse, EntitySupport, java.io.Serializable, JSONResponse {

    /**
     * Return the created_at
     *
     * @return created_at
     * @since Twitter4J 1.1.0
     */
    Date getCreatedAt();

    /**
     * Returns the id of the status
     *
     * @return the id
     */
    long getId();

    /**
     * Returns the text of the status
     *
     * @return the text
     */
    String getText();

    /**
     * Restricts tweets to the given language, given by an ISO 639-1 code. Language detection is best-effort.
     *
     * @return language if set
     */
    String getLanguage();


    /**
     * Returns the number of times this tweet has been marked favorited.
     *
     * @return the favorite count
     */
    String getUnEscapedText();

    /**
     * Returns the source
     *
     * @return the source
     * @since Twitter4J 1.0.4
     */
    String getSource();


    /**
     * Test if the status is truncated
     *
     * @return true if truncated
     * @since Twitter4J 1.0.4
     */
    boolean isTruncated();

    /**
     * Returns the in_reply_to_status_id
     *
     * @return the in_reply_to_status_id
     * @since Twitter4J 1.0.4
     */
    String getInReplyToStatusId();

    /**
     * Returns the in_reply_to_user_id
     *
     * @return the in_reply_to_user_id
     * @since Twitter4J 1.0.4
     */
    String getInReplyToUserId();

    /**
     * Returns the in_reply_to_screen_name
     *
     * @return the in_in_reply_to_screen_name
     * @since Twitter4J 2.0.4
     */
    String getInReplyToScreenName();

    /**
     * Returns The location that this tweet refers to if available.
     *
     * @return returns The location that this tweet refers to if available (can be null)
     * @since Twitter4J 2.1.0
     */
    GeoLocation getGeoLocation();

    /**
     * Returns the place attached to this status
     *
     * @return The place attached to this status
     * @since Twitter4J 2.1.1
     */
    Place getPlace();

    /**
     * Test if the status is favorited
     *
     * @return true if favorited
     * @since Twitter4J 1.0.4
     */
    boolean isFavorited();

    String getIdStr();

    String getInReplyToUserIdStr();

    String getInReplyToStatusIdStr();

    TweetScope getScopes();

    /**
     * Return the user associated with the status.<br>
     * This can be null if the instance if from User.getStatus().
     *
     * @return the user
     */
    User getUser();

    /**
     * @since Twitter4J 2.0.10
     */
    boolean isRetweet();

    /**
     * @since Twitter4J 2.1.0
     */
    Status getRetweetedStatus();

    Status getQuotedStatus();

    String getQuotedStatusId();

    String getQuotedStatusIdStr();

    String getFullText();

    String[] getDisplayTextRange();

    Status getExtendedTweet();

    /**
     * Returns an array of contributors, or null if no contributor is associated with this status.
     *
     * @since Twitter4J 2.2.3
     */
    long[] getContributors();

    /**
     * Returns the number of times this tweet has been retweeted, or -1 when the tweet was
     * created before this feature was enabled.
     *
     * @return the retweet count.
     */
    long getRetweetCount();

    /**
     * Returns the number of times this tweet has been marked favorited.
     *
     * @return the favorite count
     */
    long getFavoriteCount();

    /**
     * Returns true if the authenticating user has retweeted this tweet, or false when the tweet was
     * created before this feature was enabled.
     *
     * @return whether the authenticating user has retweeted this tweet.
     * @since Twitter4J 2.1.4
     */
    boolean isRetweetedByMe();

    /**
     * Returns the authenticating user's retweet's id of this tweet, or -1L when the tweet was created
     * before this feature was enabled.
     *
     * @return the authenticating user's retweet's id of this tweet
     * @since Twitter4J 3.0.1
     */
    long getCurrentUserRetweetId();

    /**
     * Returns true if the status contains a link that is identified as sensitive.
     *
     * @return whether the status contains sensitive links
     * @since Twitter4J 3.0.0
     */
    boolean isPossiblySensitive();

    /**
     * Refer: https://dev.twitter.com/docs/api/advertising/tweet-delivery-by-country for more details.
     * Place ids of countries, where tweet is targeted to.
     *
     * @return if status is narrow casted to certain countries.
     */
    String[] getPlaceIds();


    void setHierarchicalMessage(Boolean hierarchicalMessage);

    Boolean getHierarchicalMessage();

    String getHierarchicalMessageId();

    void setHierarchicalMessageId(String statusId);

    /**
     * @return card uri of the tweet
     */
    String getCardUri();
}
