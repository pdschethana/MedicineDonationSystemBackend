package com.medicinedonation.repository;

import com.medicinedonation.model.ChatbotQA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatbotQARepository
        extends JpaRepository<ChatbotQA, Long> {

    Optional<ChatbotQA> findByKeywordIgnoreCase(String keyword);
    List<ChatbotQA> findAll();
}