package com.redisson.cache.common;

import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

public class YamlPropertySourceFactory implements PropertySourceFactory {

  @Override
  public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) throws IOException {
    YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
    Resource resource = encodedResource.getResource();
    yamlPropertiesFactoryBean.setResources(resource);
    Properties properties = yamlPropertiesFactoryBean.getObject();
    return new PropertiesPropertySource(resource.getFilename(), properties);
  }
}
