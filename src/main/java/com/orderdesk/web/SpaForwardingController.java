package com.orderdesk.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards client-side routes to the React single-page app so deep links
 * (e.g. /orders/3) work when the browser requests them directly.
 */
@Controller
public class SpaForwardingController {

    @RequestMapping({"/customers", "/customers/{id}", "/orders", "/orders/{id}"})
    public String forward() {
        return "forward:/index.html";
    }
}
