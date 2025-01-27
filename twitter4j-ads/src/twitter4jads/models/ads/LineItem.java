package twitter4jads.models.ads;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * User: poly
 * Date: 29/01/14
 * Time: 11:54 AM
 */
public class LineItem extends TwitterEntity {

    @SerializedName("account_id")
    private String accountId;

    @SerializedName("name")
    private String name;

    @SerializedName("bid_amount_local_micro")
    private Long bidAmtInMicro;

    @SerializedName("campaign_id")
    private String campaignId;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("currency")
    private String currency;

    @SerializedName("goal_settings")
    private String goalSettings;

    @SerializedName("match_relevant_popular_queries")
    private Boolean matchRelevantPopularQueries;

    @SerializedName("objective")
    private String objective;

    @SerializedName("goal")
    private String goal;

    @SerializedName("deleted")
    private Boolean deleted;

    @SerializedName("placements")
    private List<Placement> placements;

    @SerializedName("product_type")
    private ProductType productType;

    @SerializedName("include_sentiment")
    private Sentiments sentiment;

    @SerializedName("primary_web_event_tag")
    private String webEventTag;

    @SerializedName("suggested_high_cpe_bid_local_micro")
    private Long suggestedHighCpeBidInMicro;

    @SerializedName("suggested_low_cpe_bid_local_micro")
    private Long suggestedLowCpeBidInMicro;

    @SerializedName("target_cpa_local_micro")
    private Long targetCpaLocalMicro;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("bid_strategy")
    private BidStrategy bidStrategy;

    @SerializedName("pay_by")
    private String payBy;

    @SerializedName("advertiser_domain")
    private String advertiserDomain;

    @SerializedName("advertiser_user_id ")
    private String advertiserUserId;

    @SerializedName("categories")
    private String[] categories;

    @SerializedName("creative_source")
    private String creativeSource;

    @SerializedName("start_time")
    private Date startTimeInUTC;

    @SerializedName("end_time")
    private Date endTimeInUTC;

    @SerializedName("total_budget_amount_local_micro")
    private Long totalBudget;

    @SerializedName("daily_budget_amount_local_micro")
    private Long dailyBudget;

    @SerializedName("entity_status")
    private EntityStatus status;

    @SerializedName("frequency_cap")
    private Integer frequencyCap;

    @SerializedName("duration_in_days")
    private Integer durationInDays;

    @SerializedName("audience_expansion")
    private AudienceExpansion audienceExpansion;

    @SerializedName("standard_delivery")
    private Boolean standardDelivery;

    public String getCreativeSource() {
        return creativeSource;
    }

    public void setCreativeSource(String creativeSource) {
        this.creativeSource = creativeSource;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Long getBidAmtInMicro() {
        return bidAmtInMicro;
    }

    public void setBidAmtInMicro(Long bidAmtInMicro) {
        this.bidAmtInMicro = bidAmtInMicro;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getGoalSettings() {
        return goalSettings;
    }

    public void setGoalSettings(String goalSettings) {
        this.goalSettings = goalSettings;
    }

    public Boolean getMatchRelevantPopularQueries() {
        return matchRelevantPopularQueries;
    }

    public void setMatchRelevantPopularQueries(Boolean matchRelevantPopularQueries) {
        this.matchRelevantPopularQueries = matchRelevantPopularQueries;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Sentiments getSentiment() {
        return sentiment;
    }

    public void setSentiment(Sentiments sentiment) {
        this.sentiment = sentiment;
    }

    public Long getSuggestedHighCpeBidInMicro() {
        return suggestedHighCpeBidInMicro;
    }

    public void setSuggestedHighCpeBidInMicro(Long suggestedHighCpeBidInMicro) {
        this.suggestedHighCpeBidInMicro = suggestedHighCpeBidInMicro;
    }

    public Long getSuggestedLowCpeBidInMicro() {
        return suggestedLowCpeBidInMicro;
    }

    public void setSuggestedLowCpeBidInMicro(Long suggestedLowCpeBidInMicro) {
        this.suggestedLowCpeBidInMicro = suggestedLowCpeBidInMicro;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public List<Placement> getPlacements() {
        return placements;
    }

    public void setPlacements(List<Placement> placements) {
        this.placements = placements;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }

    public BidStrategy getBidStrategy() {
        return bidStrategy;
    }

    public void setBidStrategy(BidStrategy bidStrategy) {
        this.bidStrategy = bidStrategy;
    }

    public String getPayBy() {
        return payBy;
    }

    public void setPayBy(String payBy) {
        this.payBy = payBy;
    }

    public String getWebEventTag() {
        return webEventTag;
    }

    public void setWebEventTag(String webEventTag) {
        this.webEventTag = webEventTag;
    }

    public String getAdvertiserDomain() {
        return advertiserDomain;
    }

    public void setAdvertiserDomain(String advertiserDomain) {
        this.advertiserDomain = advertiserDomain;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdvertiserUserId() {
        return advertiserUserId;
    }

    public void setAdvertiserUserId(String advertiserUserId) {
        this.advertiserUserId = advertiserUserId;
    }

    public Date getStartTime() {
        return startTimeInUTC;
    }

    public void setStartTime(Date startTimeInUTC) {
        this.startTimeInUTC = startTimeInUTC;
    }

    public Date getEndTime() {
        return endTimeInUTC;
    }

    public void setEndTime(Date endTimeInUTC) {
        this.endTimeInUTC = endTimeInUTC;
    }

    public Long getTargetCpaLocalMicro() {
        return targetCpaLocalMicro;
    }

    public void setTargetCpaLocalMicro(Long targetCpaLocalMicro) {
        this.targetCpaLocalMicro = targetCpaLocalMicro;
    }

    public Long getTotalBudget() {
        return totalBudget;
    }

    public Long getDailyBudget() {
        return dailyBudget;
    }

    public void setTotalBudget(Long budget) {
        this.totalBudget = budget;
    }

    public void setDailyBudget(Long budget) {
        this.dailyBudget = budget;
    }

    public EntityStatus getStatus() {
        return status;
    }

    public void setStatus(EntityStatus status) {
        this.status = status;
    }

    public Integer getFrequencyCap() {
        return frequencyCap;
    }

    public void setFrequencyCap(Integer frequencyCap) {
        this.frequencyCap = frequencyCap;
    }

    public Integer getDurationInDays() {
        return durationInDays;
    }

    public void setDurationInDays(Integer durationInDays) {
        this.durationInDays = durationInDays;
    }

    public AudienceExpansion getAudienceExpansion() {
        return audienceExpansion;
    }

    public void setAudienceExpansion(AudienceExpansion audienceExpansion) {
        this.audienceExpansion = audienceExpansion;
    }

    public Boolean getStandardDelivery() {
        return standardDelivery;
    }

    public void setStandardDelivery(Boolean standardDelivery) {
        this.standardDelivery = standardDelivery;
    }

    @Override
    public String toString() {
        return "LineItem{" +
               "accountId='" + accountId + '\'' +
               ", name='" + name + '\'' +
               ", bidAmtInMicro=" + bidAmtInMicro +
               ", campaignId='" + campaignId + '\'' +
               ", createdAt=" + createdAt +
               ", currency='" + currency + '\'' +
               ", goalSettings='" + goalSettings + '\'' +
               ", matchRelevantPopularQueries=" + matchRelevantPopularQueries +
               ", objective='" + objective + '\'' +
               ", deleted=" + deleted +
               ", placements=" + placements +
               ", productType=" + productType +
               ", sentiment=" + sentiment +
               ", status=" + status +
               ", webEventTag='" + webEventTag + '\'' +
               ", suggestedHighCpeBidInMicro=" + suggestedHighCpeBidInMicro +
               ", suggestedLowCpeBidInMicro=" + suggestedLowCpeBidInMicro +
               ", targetCpaLocalMicro=" + targetCpaLocalMicro +
               ", updatedAt=" + updatedAt +
               ", bidStrategy=" + bidStrategy +
               ", payBy='" + payBy + '\'' +
               ", advertiserDomain='" + advertiserDomain + '\'' +
               ", advertiserUserId='" + advertiserUserId + '\'' +
               ", categories=" + Arrays.toString(categories) +
               ", startTime=" + startTimeInUTC +
               ", endTime=" + endTimeInUTC +
               ", goal='" + goal + '\'' +
               ", creativeSource='" + creativeSource + '\'' +
               ", totalBudget='" + totalBudget + '\'' +
               ", dailyBudget='" + dailyBudget + '\'' +
               ", frequencyCap='" + frequencyCap + '\'' +
               ", durationInDays='" + durationInDays + '\'' +
               ", audienceExpansion='" + audienceExpansion + '\'' +
               ", standardDelivery='" + standardDelivery + '\'' +
               '}';
    }
}
