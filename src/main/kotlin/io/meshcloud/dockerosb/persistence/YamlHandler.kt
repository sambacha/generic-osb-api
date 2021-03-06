package io.meshcloud.dockerosb.persistence

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileWriter

@Service
class YamlHandler {

  fun writeObject(objectToWrite: Any, file: File) {
    FileUtils.forceMkdir(file.parentFile)

    val writer = FileWriter(file)
    yamlMapper
        .writerWithDefaultPrettyPrinter()
        .writeValue(writer, objectToWrite)
    writer.close()
  }

  fun <T> readObject(file: File, targetClass: Class<T>): T {
    return yamlMapper.readValue(file, targetClass)
  }

  fun <T> readObject(file: File, typeRef: TypeReference<T>): T {
    return yamlMapper.readValue(file, typeRef)
  }

  companion object {
    private val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
  }
}