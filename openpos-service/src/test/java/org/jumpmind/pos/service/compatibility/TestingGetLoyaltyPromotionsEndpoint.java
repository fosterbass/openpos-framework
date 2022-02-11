package org.jumpmind.pos.service.compatibility;

import org.jumpmind.pos.service.Endpoint;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Endpoint(path = "/testingCustomer/getLoyaltyPromotions")
public class TestingGetLoyaltyPromotionsEndpoint {

    @ResponseBody
    public TestingGetLoyaltyPromotionsResponse getLoyaltyPromotions(@RequestBody String customerId) {
        TestingGetLoyaltyPromotionsResponse response = new TestingGetLoyaltyPromotionsResponse();

        List<TestingLoyaltyPromotionModel> loyaltyPromotions = new ArrayList<>();

        TestingPromotionModel promotionModel = new TestingPromotionModel();
        List<TestingRewardModel> rewardModels = new ArrayList<>();
        TestingRewardModel rewardModel = new TestingRewardModel();
        rewardModel.setRewardAmount(new BigDecimal(10.0));
        rewardModels.add(rewardModel);
        promotionModel.setRewards(rewardModels);

        {
            TestingLoyaltyPromotionModel loyaltyPromotionModel = new TestingLoyaltyPromotionModel();
            loyaltyPromotionModel.setPromotionModel(promotionModel);
            loyaltyPromotions.add(loyaltyPromotionModel);
        }
        {
            TestingPromotionModel promotionModel2 = new TestingPromotionModel();
            List<TestingRewardModel> rewardModels2 = new ArrayList<>();
            TestingRewardModel rewardModel2 = new TestingRewardModel();
            rewardModel2.setRewardAmount(new BigDecimal(10.0));
            rewardModels2.add(rewardModel2);
            promotionModel2.setRewards(rewardModels2);

            TestingLoyaltyPromotionModel loyaltyPromotionModel = new TestingLoyaltyPromotionModel();
            loyaltyPromotionModel.setPromotionModel(promotionModel2);
            loyaltyPromotions.add(loyaltyPromotionModel);
        }

        response.setLoyaltyPromotions(loyaltyPromotions);

        return response;
    }

}
