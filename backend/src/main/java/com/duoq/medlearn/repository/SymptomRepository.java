package com.duoq.medlearn.repository;

import com.duoq.medlearn.domain.entity.Symptom;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SymptomRepository extends BaseRepository<Symptom, Long> {

    // Kiểm tra tên đã tồn tại chưa
    boolean existsByName(String name);

    // Tìm nhiều symptom theo id list — dùng khi submit symptom checker
    List<Symptom> findAllByIdIn(List<Long> ids);

}