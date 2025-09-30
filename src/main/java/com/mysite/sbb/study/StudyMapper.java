package com.mysite.sbb.study;

import com.mysite.sbb.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface StudyMapper {
    StudyMapper INSTANCE = Mappers.getMapper(StudyMapper.class);

    /* Entity -> DTO */
    @Mapping(source = "author.username", target = "username")
    @Mapping(target = "imagePaths", expression = "java(mapImagePaths(study))")
    @Mapping(target = "images", ignore = true) // MultipartFile 무시
    StudyDTO toDTO(Study study);

    /* imagePath 추출용 default 메서드 */
    default List<String> mapImagePaths(Study study) {
        if (study == null || study.getImages() == null) return Collections.emptyList();
        return study.getImages().stream()
                .map(StudyImage::getImagePath)
                .collect(Collectors.toList());
    }

    /* DTO -> Entity */
    @Mapping(source = "username", target = "author", qualifiedByName = "nameToSiteUser")
    @Mapping(target = "images", ignore = true) // 이미지 엔티티는 별도 서비스 로직에서 생성/연결
    Study toEntity(StudyDTO dto);

    /* 리스트/페이지 변환 */
    List<StudyDTO> toDtoList(List<Study> list);

    default Page<StudyDTO> toDTO(Page<Study> studies) {
        return studies.map(this::toDTO);
    }
}
