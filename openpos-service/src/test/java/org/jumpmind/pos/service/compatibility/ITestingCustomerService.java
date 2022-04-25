package org.jumpmind.pos.service.compatibility;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Testing Customer Service", description = "This service exposes endpoints to retrieve customer information")
@RestController("testingCustomer")
@RequestMapping("/testingCustomer")
public interface ITestingCustomerService {

    @PostMapping("/getCustomer")
    @ResponseBody
    TestingGetCustomerResponse getCustomer(@RequestBody String customerId);

    @PostMapping("/saveCustomer")
    @ResponseBody
    TestingSaveCustomerResponse saveCustomer(@RequestBody TestingSaveCustomerRequest saveCustomerRequest);

    @PostMapping("/unlinkCustomer")
    @ResponseBody
    TestingSaveCustomerResponse unlinkCustomer(@RequestBody TestingSaveCustomerRequest saveCustomerRequest);

    @PostMapping("/getLoyaltyPromotions")
    @ResponseBody
    TestingGetLoyaltyPromotionsResponse getLoyaltyPromotions(@RequestBody String customerId);
}
