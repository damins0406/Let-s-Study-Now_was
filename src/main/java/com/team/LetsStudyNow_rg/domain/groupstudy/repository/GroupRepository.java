package com.team.LetsStudyNow_rg.domain.groupstudy.repository;

import com.team.LetsStudyNow_rg.domain.groupstudy.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    // 그룹 생성자 ID로 그룹 찾기
    List<Group> findByLeaderId(Long leaderId);

    // 그룹 이름으로 찾기
    Optional<Group> findByGroupName(String groupName);
}