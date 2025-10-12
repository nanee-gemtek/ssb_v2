package com.mysite.sbb.support;

import com.mysite.sbb.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;



@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface SupportMapper {
    SupportMapper INSTANCE = Mappers.getMapper(SupportMapper.class);


    @Mapping(target = "messagePreview", source = "message", qualifiedByName = "preview40")
    SupportDTO toDTO(Support support);




    Support toEntity(SupportDTO dto);

    List<SupportDTO> toDtoList(List<Support> list);

    default Page<SupportDTO> toDTO(Page<Support> supports) {
        return supports.map(this::toDTO);
    }

    @Named("preview40")
    default String preview40(String message) {
        if (message == null) return "";
        int max = 40; // 원하는 길이
        int count = message.codePointCount(0, message.length());
        if (count <= max) return message;
        int end = message.offsetByCodePoints(0, max);
        return message.substring(0, end) + "…";
    }
}
