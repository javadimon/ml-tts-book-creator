package com.zubanoff.ml.tts.book.creator.dao;

import com.zubanoff.ml.tts.book.creator.model.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;

public interface BookRepository extends JpaRepository<BookEntity, UUID> {
}
