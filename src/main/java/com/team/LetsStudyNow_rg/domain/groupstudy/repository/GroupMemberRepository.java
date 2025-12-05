package com.team.LetsStudyNow_rg.domain.groupstudy.repository;

import com.team.LetsStudyNow_rg.domain.groupstudy.domain.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    // 그룹의 모든 멤버 조회
    List<GroupMember> findByGroupId(Long groupId);

    // 특정 멤버가 특정 그룹에 속해있는지 확인
    Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);

    // 그룹의 멤버 수 카운트
    long countByGroupId(Long groupId);

    // 멤버 삭제
    void deleteByGroupIdAndMemberId(Long groupId, Long memberId);
}