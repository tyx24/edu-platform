package com.education.platform;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/edu_platform?useUnicode=true&characterEncoding=utf8&allowPublicKeyRetrieval=true&useSSL=false",
                        "root", "123456"
                )
                .globalConfig(builder -> builder
                        .author("tan")
                        .outputDir(System.getProperty("user.dir") + "/src/main/java")
                        .disableOpenDir()
                )
                .packageConfig(builder -> builder
                        .parent("com.education.platform")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .pathInfo(Collections.singletonMap(
                                OutputFile.xml /* 或 OutputFile.mapperXml，看你的枚举值 */,
                                System.getProperty("user.dir") + "/src/main/resources/mapper"
                        ))
                )
                .strategyConfig(builder -> builder
                        .addInclude("user", "course", "chapter", "resource", "homework",
                                "submission", "exam", "question", "exam_record", "enrollment")
                        .entityBuilder()
                        .enableLombok()
                        .naming(NamingStrategy.underline_to_camel)
                        .columnNaming(NamingStrategy.underline_to_camel)
//                        .enableFileOverride()  // 在实体层覆盖已有文件
                        .mapperBuilder()
//                        .enableFileOverride()  // Mapper 覆盖
                        .serviceBuilder()
//                        .enableFileOverride()  // Service 覆盖
                        .controllerBuilder()
//                        .enableFileOverride() // Controller 覆盖
                        .enableRestStyle()
                )
                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }
}
