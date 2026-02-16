package com.aibe.team2.domain.mypage.dto.response;

import com.aibe.team2.domain.mypage.entity.Member;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MemberResponseDto {
    private final String email;
    private final String nickname;
    private final String profileImageUrl;

    private final JobPreferencesDto  jobPreferences;

    public MemberResponseDto(Member member) {
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.profileImageUrl = member.getProfileImageUrl();

        this.jobPreferences = new JobPreferencesDto(member);
    }

    @Getter
    public static class JobPreferencesDto {
        private final List<String> targetJobRoles;
        private final String preferredLocation;

        public JobPreferencesDto(Member member) {
            this.targetJobRoles = parseJobRoles(member.getDesiredJobRole());
            this.preferredLocation = member.getPreferredLocation();
        }

        private List<String> parseJobRoles(String jobRoles){
            if(jobRoles == null || jobRoles.isBlank()){
                return List.of();
            }

            return Arrays.stream(jobRoles.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }
    }
}
