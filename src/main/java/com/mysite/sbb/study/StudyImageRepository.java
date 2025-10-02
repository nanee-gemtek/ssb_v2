package com.mysite.sbb.study;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyImageRepository extends JpaRepository<StudyImage, Integer> {
    Page<StudyImage> findAll(Pageable pageable);
    // 1) 파일 삭제를 위해 이미지 경로만 미리 수집
    @Query("select pi.imagePath from StudyImage pi where pi.study.id in :studyIds")
    List<String> findPathsByStudyIdIn(@Param("studyIds") List<Integer> studyIds);

    // 2) 자식 이미지 벌크 삭제
    @Modifying
    @Query("delete from StudyImage pi where pi.study.id in :studyIds")
    void deleteByStudyIdIn(@Param("studyIds") List<Integer> studyIds);
}
