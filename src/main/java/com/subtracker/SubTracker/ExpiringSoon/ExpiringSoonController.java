package com.subtracker.SubTracker.ExpiringSoon;


import com.subtracker.SubTracker.subscription.SubscriptionEntity;
import com.subtracker.SubTracker.subscription.SubscriptionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
public class ExpiringSoonController {

    @Autowired
    private ExpiringSoonService expiringSoonService;

    @GetMapping("/expiring-soon")
    public ResponseEntity<List<SubscriptionResponse>> findExpiringSoon() {

        return new ResponseEntity<>(expiringSoonService.findExpiringSoon(), HttpStatus.OK);
    }
}
