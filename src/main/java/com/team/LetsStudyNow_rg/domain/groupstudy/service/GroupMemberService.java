package com.team.LetsStudyNow_rg.domain.groupstudy.service;

import com.team.LetsStudyNow_rg.domain.groupstudy.domain.Group;
import com.team.LetsStudyNow_rg.domain.groupstudy.domain.GroupMember;
import com.team.LetsStudyNow_rg.domain.groupstudy.dto.AddGroupMemberRequest;
import com.team.LetsStudyNow_rg.domain.groupstudy.dto.GroupMemberResponse;
import com.team.LetsStudyNow_rg.domain.groupstudy.repository.GroupMemberRepository;
import com.team.LetsStudyNow_rg.domain.groupstudy.repository.GroupRepository;
import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import com.team.LetsStudyNow_rg.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    // 생성자 주입
    public GroupMemberService(GroupMemberRepository groupMemberRepository,
                              GroupRepository groupRepository,
                              MemberRepository memberRepository) {
        this.groupMemberRepository = groupMemberRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    // 멤버 추가
    @Transactional
    public GroupMemberResponse addMember(AddGroupMemberRequest request) {
        // 그룹 존재 확인
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다"));

        // 이미 멤버인지 확인
        if (groupMemberRepository.findByGroupIdAndMemberId(
                request.getGroupId(), request.getMemberId()).isPresent()) {
            throw new IllegalArgumentException("이미 그룹 멤버입니다");
        }

        // 멤버 추가
        GroupMember member = new GroupMember(
                request.getGroupId(),
                request.getMemberId(),
                "MEMBER"
        );
        GroupMember saved = groupMemberRepository.save(member);

        return new GroupMemberResponse(saved);
    }

    // 그룹 멤버 목록 조회 (SRS 6.3.6)
    public List<GroupMemberResponse> getGroupMembers(Long groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return members.stream()
                .map(groupMember -> {
                    // Member 정보 조회
                    Member member = memberRepository.findById(groupMember.getMemberId())
                            .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다: " + groupMember.getMemberId()));
                    return new GroupMemberResponse(groupMember, member);
                })
                .collect(Collectors.toList());
    }

    // 멤버 추방 (SRS 6.3.7~6.3.9)
    @Transactional
    public void removeMember(Long groupId, Long memberId, Long requesterId) {
        // 그룹 존재 확인
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다"));

        // 그룹 생성자 권한 확인
        if (!group.getLeaderId().equals(requesterId)) {
            throw new IllegalArgumentException("그룹 생성자만 멤버를 추방할 수 있습니다");
        }

        // 자기 자신은 추방 불가
        if (memberId.equals(requesterId)) {
            throw new IllegalArgumentException("자기 자신은 추방할 수 없습니다");
        }

        // 멤버 존재 확인
        GroupMember member = groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 멤버를 찾을 수 없습니다"));

        // 멤버 삭제
        groupMemberRepository.deleteByGroupIdAndMemberId(groupId, memberId);
    }
}