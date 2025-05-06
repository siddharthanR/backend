package com.bpaz.backend.config.repository.configPB;

import com.bpaz.backend.config.model.Config;

import java.util.List;

public interface ConfigPBRepository {

    void saveAllConfigsAndFlush(List<Config> appConfigs);

}
