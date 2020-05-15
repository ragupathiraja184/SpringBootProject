package com.zeptoh.benchmarking;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
//import org.springframework.mail.MailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.zeptoh.benchmarking.model.Login;
import com.zeptoh.benchmarking.model.Privilege;
import com.zeptoh.benchmarking.model.Role;
import com.zeptoh.benchmarking.model.Template;
import com.zeptoh.benchmarking.model.Well;
import com.zeptoh.benchmarking.repository.LoginRepository;
import com.zeptoh.benchmarking.repository.PrivilegeRepository;
import com.zeptoh.benchmarking.repository.RoleRepository;
import com.zeptoh.benchmarking.repository.TemplateRepository;
import com.zeptoh.benchmarking.repository.WellRepository;
import com.zeptoh.benchmarking.services.UserServiceImpl;
//import com.zeptoh.benchmarking.utils.Utils;

@SpringBootApplication
@Controller
@EnableScheduling
public class SpringbootApplication {

	@Autowired
	RoleRepository roleRepository;  

	@Autowired
	PrivilegeRepository privilegeRepository;

	@Autowired
	TemplateRepository templateRepository;

	@Autowired
	WellRepository wellRepository;

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SpringbootApplication.class, args);
		System.out.println("Press 'Enter' to terminate");
		new Scanner(System.in).nextLine();
		clearLogoutEntry();
		System.out.println("Exiting");
		System.exit(1);
		
	}

	@Bean
	public MongoTemplate mongoTemplate(MongoDbFactory mongoDbFactory, MongoMappingContext context) {

		MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory), context);
		converter.setTypeMapper(new DefaultMongoTypeMapper(null));

		MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory, converter);

		return mongoTemplate;

	}

	public static void clearLogoutEntry() {
		MongoClient mongoConnection = new MongoClient("localhost");
		try {

			DB database = mongoConnection.getDB("zeptoh");
			// release Login lock
			CommandResult result = database.doEval("db.Login.update({}, {$set: { isLoggedIn: false} });");

			// release well Input lock
			// To be done
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mongoConnection.close();
		}
	}

}
