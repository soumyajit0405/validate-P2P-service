package com.energytrade;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


import com.energytrade.app.model.GeneralConfig;


@Component
public class AppStartupRunner implements ApplicationRunner {
    
	
	public static HashMap<String,String> configValues = new HashMap<>();
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("Your application started with option names : {}"+ args.getOptionNames());
    
    }
}
