package com.bpaz.backend.config.repository;

import com.bpaz.backend.config.model.Config;
import com.bpaz.backend.config.repository.configPB.ConfigPBRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends JpaRepository<Config, Long>, ConfigPBRepository {

}
