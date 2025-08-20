package com.mysite.sbb.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProducerService {

    private final ProducerRepository producerRepository;

//    public List<Producer> getProducers() {
//        return producerRepository.findById();
//    }

    public List<Producer> all(){
        return producerRepository.findAll();              // 정렬 포함
    }

    public List<Producer> allByName(){
        return producerRepository.findAll(Sort.by("name"));
    }

}
