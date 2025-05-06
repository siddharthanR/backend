package com.bpaz.backend.config.repository.configPB;

import com.bpaz.backend.config.model.Config;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class ConfigPBRepositoryImpl implements ConfigPBRepository {

    @Override
    @Transactional
    public void saveAllConfigsAndFlush(List<Config> appConfigs) {

    }
}
