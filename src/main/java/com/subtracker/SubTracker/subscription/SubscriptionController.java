package com.subtracker.SubTracker.subscription;

import com.subtracker.SubTracker.common.PageResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    //Create Subscription for a User
    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(@RequestBody SubscriptionRequest subscriptionRequest) {
        return new ResponseEntity<>(subscriptionService.createSubscription(subscriptionRequest), HttpStatus.OK);
    }

    //Get Subscription by sub-Id
    @GetMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(@PathVariable Long subscriptionId) {
        SubscriptionResponse subscriptionResponse = subscriptionService.getSubscriptionById(subscriptionId);
        if(subscriptionResponse != null){
            return new ResponseEntity<>(subscriptionResponse, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //Get All Subscriptions of a User
    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponseDto<SubscriptionResponse>> getSubscriptionsByUserId(@PathVariable Long userId,
                                                                                          @PageableDefault
                                                                                                  (page = 0,size = 10)
                                                                                          Pageable pageable) {

        return new ResponseEntity<>(subscriptionService.findAllByUserId(userId,pageable),HttpStatus.OK);
    }

    //Get All Subscriptions
    @GetMapping
    public ResponseEntity<PageResponseDto<SubscriptionResponse>> getAllSubscriptions(@PageableDefault(page = 0,size = 10) Pageable pageable) {

        return new ResponseEntity<>(subscriptionService.getAllSubscriptions(pageable),HttpStatus.OK);
    }


    //Update subscription
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> updateSubscription(@PathVariable Long id, @RequestBody UpdateSubscriptionRequest updateSubscriptionRequest) {
        return new ResponseEntity<>(subscriptionService.updateSubscription(id,updateSubscriptionRequest),HttpStatus.OK);
    }


    //Delete Subscription
    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<String> deleteById(@PathVariable Long subscriptionId) {

        boolean flag = subscriptionService.findById(subscriptionId);
        if(flag){
            return new ResponseEntity<>("Subscription deleted",HttpStatus.OK);
        }
        return new ResponseEntity<>("Subscription not found",HttpStatus.NOT_FOUND);
    }

}
