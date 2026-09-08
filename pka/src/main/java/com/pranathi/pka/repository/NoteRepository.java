package com.pranathi.pka.repository;

import com.pranathi.pka.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
public interface NoteRepository extends JpaRepository<Note, Long> {
   
List<Note> findByUser_Email(String email);
Optional<Note> findByIdAndUser_Email(Long id, String email);

Page<Note> findByUser_Email(String email, Pageable pageable);

List<Note> findByUser_Email(String email, Sort sort);
void deleteByIdAndUser_Email(Long id, String email);
@Query("""
SELECT n
FROM Note n
WHERE n.user.email = :email
AND (
    LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR
    LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
)
""")
List<Note> searchNotes(@Param("keyword") String keyword,
                       @Param("email") String email);
}