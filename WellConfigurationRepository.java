package com.zeptoh.benchmarking.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.zeptoh.benchmarking.model.WellConfiguration;

public interface WellConfigurationRepository extends MongoRepository<WellConfiguration, Long>{
	WellConfiguration findById(String id);
	WellConfiguration findByWellIdAndLevel(String wellId,String level);
	@Query("{'wellId' : ?0 , 'level' : ?1, 'configNo' : ?2}")
	WellConfiguration findByWellIdLevelAndConfigNo(String wellId,String level,String config);

	//WellConfiguration findByWellId(String wellId);
	List<WellConfiguration> findByWellId(String wellId);
	
}
