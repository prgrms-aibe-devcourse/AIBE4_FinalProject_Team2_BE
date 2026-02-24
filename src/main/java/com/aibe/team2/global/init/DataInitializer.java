package com.aibe.team2.global.init;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.repository.InterviewRepository;
import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.MemberRepository;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import com.aibe.team2.domain.statistics.entity.InterviewResultStatistics;
import com.aibe.team2.domain.statistics.repository.InterviewRecordRepository;
import com.aibe.team2.domain.statistics.repository.InterviewResultStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@Profile("!prod") // 로컬이나 개발 환경에서만 이 빈이 활성화됨
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewRecordRepository interviewRecordRepository;
    private final InterviewResultStatisticsRepository statisticsRepository;

    private final ResumeRepository resumeRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

   @Override
    @Transactional
    public void run(String... args) throws Exception {

       if(memberRepository.count() > 0){
           log.info("더미 데이터가 이미 존재하여 생성을 건너뜁니다.");
           return;
       }

       log.info("---전체 도메인 더미 데이터 생성을 시작합니다.---");
       Random random = new Random();

       // 1. 멤버 5명 생성
       List<Member> members = createMembers();

       for(Member member : members) {
           Long userId = member.getId();

           // 2. 이력서 및 채용공고 생성(부모 데이터)
           Resume resume = createResume(userId);
           JobPosting jobPosting = createJobPosting(userId);

           // 3. 이력서 분석 결과 생성(자식 데이터)
           createResumeAnalysisReport(resume, jobPosting);

           // 4. 회우너별 면접 세션 및 통계 데이터 생성(5~10건)
           int sessionCount = random.nextInt(6) + 5;
           for (int i = 0; i < sessionCount; i++){

               int daysAgo = random.nextInt(60) + 1;
               LocalDateTime pastDate = LocalDateTime.now().minusDays(daysAgo);

               // 4-1. InterviewSession
               InterviewSession session = InterviewSession.builder()
                       .memberId(userId)
                       .interviewType(i % 2 == 0 ? "TEXT" : "VOICE") // 변경된 필드명에 맞게 수정
                       .aiProvider(i % 2 == 0 ? "OPEN_AI" : "RETELL") // aiProvider 값도 함께 세팅 (Null 방지)
                       .build();

               setCreatedAt(session, pastDate);
               interviewRepository.save(session);

               // 4-2. InterviewRecord
               Map<String, Object> emotionDummy = new HashMap<>();
               emotionDummy.put("confidence", random.nextDouble());
               emotionDummy.put("anxiety", random.nextDouble());

               InterviewRecord record = InterviewRecord.builder()
                       .interviewSession(session)
                       .turnSequence(1)
                       .questionText("Spring Boot의 장점은 무엇인가요?")
                       .answerText("의존성 관리가 편리하고 내장 톰캣을 제공하여...")
                       .emotionAnalysis(emotionDummy)
                       .evaluationScore((float) (random.nextInt(41)+60)) // 60~100점 사이로 분산
                       .build();

               setCreatedAt(record, pastDate);
               interviewRecordRepository.save(record);

               // 4-3. InterviewResultStatistics
               Map<String, Object> habitDummy = new HashMap<>();
               habitDummy.put("filler_words", random.nextInt(5));

               InterviewResultStatistics stats = InterviewResultStatistics.builder()
                       .interviewSession(session)
                       .avgClarity(random.nextDouble() * 100)
                       .avgPersuasiveness(random.nextDouble() * 100)
                       .avgConsistency(random.nextDouble() * 100)
                       .jobRelevanceScore(random.nextDouble() * 100)
                       .logicalStructureScore(random.nextDouble() * 100)
                       .attitudeConfidenceScore(random.nextDouble() * 100)
                       .speechHabits(habitDummy)
                       .build();
               setCreatedAt(stats, pastDate);
               statisticsRepository.save(stats);
           }
       }

       log.info("더미 데이터 생성이 성공적으로 완료되었습니다.");
   }

   // 데이터 생성 모듈
    private List<Member> createMembers() {
       List<Member> members = new ArrayList<>();
       for(int i = 1; i <= 5; i++){
           Member member = new Member(
                   "tester" + i + "@synctalk.com",
                   "encodedPassword!",
                   "테스터" + i,
                   null, null
           );
           members.add(memberRepository.save(member));
       }
       return members;
    }

    private Resume createResume(Long userId) {
       Resume resume = Resume.builder()
               .userId(userId)
               .title("백엔드 개발자 지원 이력서")
               .content("저는 Java와 Spring을 활용한 프로젝트 경험이 있습니다. ...")
               .s3FileUrl("https://s3.ap-northeast-2.amazonaws.com/synctalk/dummy.pdf")
               .build();
       resume.updateAnalysisStatus(true);
       return resumeRepository.save(resume);
    }

    private JobPosting createJobPosting(Long userId) {
        String jsonSkills = "[\"Java\", \"Spring Boot\", \"JPA\", \"MySQL\"]";
        JobPosting jobPosting = JobPosting.builder()
                .userId(userId)
                .companyName("싱크테크")
                .jobTitle("주니어 백엔드 엔지니어")
                .jobDescription("대용량 트래픽 처리를 경험할 백엔드 개발자를 모십니다.")
                .requiredSkills(jsonSkills)
                .build();
        return jobPostingRepository.save(jobPosting);
    }

    private void createResumeAnalysisReport(Resume resume, JobPosting jobPosting) {
        ResumeAnalysisReport report = ResumeAnalysisReport.builder()
                .resume(resume)
                .jobPostingId(jobPosting)
                .build();

        Map<String, Object> generatedSubtitle = new HashMap<>();
        generatedSubtitle.put("title", "기본기가 탄탄한 스프링 개발자");

        Map<String, Object> keywordAnalysis = new HashMap<>();
        keywordAnalysis.put("matched_keywords", List.of("Java", "Spring Boot", "JPA", "MySQL"));
        keywordAnalysis.put("missing_keywords", List.of("Redis"));

        Map<String, Object> sentenceCorrection = new HashMap<>();
        sentenceCorrection.put("original", "경험이 있습니다.");
        sentenceCorrection.put("corrected", "다양한 실무 프로젝트를 통해 경험을 쌓았습니다.");

        report.startAnalysis();
        report.completeAnalysis(
                85,
                generatedSubtitle,
                keywordAnalysis,
                sentenceCorrection,
                "첨삭이 완료된 전체 이력서 내용입니다. ..."
        );

        resumeAnalysisRepository.save(report);
    }

    // 자바 리플렉션을 통해 생성일자를 강제로 덮어씌움
    private void setCreatedAt(Object entity, LocalDateTime pastDate){
       try{
           Field createdAtField = entity.getClass().getDeclaredField("createdAt");
           createdAtField.setAccessible(true);
           createdAtField.set(entity, pastDate);
       } catch (NoSuchFieldException | IllegalAccessException e) {
           log.error("과거 날짜 주입 중 리플렉션 에러 발생", e);
       }
    }
}
