package com.medicinedonation.repository;

import com.medicinedonation.model.ChatHistory;
import com.medicinedonation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatHistoryRepository
        extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findByUserOrderByTimestampDesc(User user);
    List<ChatHistory> findAllByOrderByTimestampDesc();
}