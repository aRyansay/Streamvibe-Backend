package com.streamvibe.repository;

import com.streamvibe.entity.MessageReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageReadReceiptRepository extends JpaRepository<MessageReadReceipt, Long> {
	Optional<MessageReadReceipt> findByMessageIdAndReaderId(Long messageId, Long readerId);
}
