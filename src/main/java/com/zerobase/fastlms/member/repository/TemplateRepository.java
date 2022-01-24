package com.zerobase.fastlms.member.repository;

import com.zerobase.fastlms.member.entity.Member;
import com.zerobase.fastlms.member.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, String> {



}