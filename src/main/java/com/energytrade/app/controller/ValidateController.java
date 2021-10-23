package com.energytrade.app.controller;

import java.util.Hashtable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.web.bind.annotation.RequestBody;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.energytrade.app.services.ValidateService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;

@CrossOrigin
@RestController
public class ValidateController extends AbstractBaseController
{
    @Autowired
    private ValidateService validateService;
    
    @RequestMapping(value ="health" , method =  RequestMethod.GET , headers =  "Accept=application/json" )
    public String health() {
        
    	return "Running";
    	
    }
    
    
    @RequestMapping(value ="validate" , method =  RequestMethod.POST , headers =  "Accept=application/json" )
    public HashMap<String, Object> validate(@RequestBody HashMap<String,String> orderDetails) {
        
    	return validateService.validate(orderDetails.get("orderId"));
    	
    }
    
       }
