package com.team.LetsStudyNow_rg.domain.groupstudy.service;

import com.team.LetsStudyNow_rg.domain.groupstudy.domain.Group;
import com.team.LetsStudyNow_rg.domain.groupstudy.domain.GroupMember;
import com.team.LetsStudyNow_rg.domain.groupstudy.dto.CreateGroupRequest;
import com.team.LetsStudyNow_rg.domain.groupstudy.dto.GroupMemberResponse;
import com.team.LetsStudyNow_rg.domain.groupstudy.dto.GroupResponse;
import com.team.LetsStudyNow_rg.domain.groupstudy.repository.GroupMemberRepository;
import com.team.LetsStudyNow_rg.domain.groupstudy.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)  // 읽기 전용 (기본)
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    // 생성자 주입
    public GroupService(GroupRepository groupRepository,
                        GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    // 그룹 생성
    @Transactional
    public GroupResponse createGroup(String groupName, Long leaderId) {
        // 1. 그룹 이름 입력 (SRS 6.2.2)
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("그룹 이름을 입력해주세요");
        }

        // 2. 그룹 생성
        Group group = new Group(groupName, leaderId);

        // 3. 저장
        Group savedGroup = groupRepository.save(group);

        // 4. 그룹 생성자를 자동으로 그룹 멤버로 추가
        GroupMember leaderMember = new GroupMember(savedGroup.getId(), leaderId, "LEADER");
        groupMemberRepository.save(leaderMember);

        // 5. 응답 반환 (생성 직후에는 참여자 수 1 - 생성자)
        return new GroupResponse(savedGroup, 1L);
    }

    // 그룹 조회 (참여자 수 포함)
    public GroupResponse getGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다"));
        Long memberCount = groupMemberRepository.countByGroupId(groupId);
        return new GroupResponse(group, memberCount);
    }

    // 내가 만든 그룹 목록 (참여자 수 포함)
    public List<GroupResponse> getMyGroups(Long leaderId) {
        List<Group> groups = groupRepository.findByLeaderId(leaderId);
        return groups.stream()
                .map(group -> {
                    Long memberCount = groupMemberRepository.countByGroupId(group.getId());
                    return new GroupResponse(group, memberCount);
                })
                .collect(Collectors.toList());
    }

    // 전체 그룹 목록 (참여자 수 포함)
    public List<GroupResponse> getAllGroups() {
        List<Group> groups = groupRepository.findAll();
        return groups.stream()
                .map(group -> {
                    Long memberCount = groupMemberRepository.countByGroupId(group.getId());
                    return new GroupResponse(group, memberCount);
                })
                .collect(Collectors.toList());
    }

    // 그룹 삭제 (SRS 6.2.4, 6.2.6)
    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다"));

        // 그룹 생성자 확인
        if (!group.getLeaderId().equals(userId)) {
            throw new IllegalArgumentException("그룹 생성자만 그룹을 삭제할 수 있습니다");
        }

        // 멤버 수 확인 (SRS 6.2.6 그룹 생성자 외 다른 멤버 없어야 함)
        long memberCount = groupMemberRepository.countByGroupId(groupId);
        if (memberCount > 1) {
            throw new IllegalArgumentException("그룹에 다른 멤버가 있으면 삭제할 수 없습니다");
        }

        // 삭제
        groupRepository.deleteById(groupId);
    }
}
