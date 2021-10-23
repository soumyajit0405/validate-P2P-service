package com.energytrade.app.services;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.energytrade.app.dao.ValidateDao;

import com.energytrade.app.model.DevicePl;

@Service("validateService")
public class ValidateService extends AbstractBaseService
{
    @Autowired
    private ValidateDao validateDao;
    
    public HashMap<String,Object> validate(String blockChainOrderId){
    	return validateDao.validateTrade(blockChainOrderId);
    }
    
}