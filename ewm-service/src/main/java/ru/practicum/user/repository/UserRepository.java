package ru.practicum.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.user.model.User;
import ru.practicum.util.Pagination;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsUserByName(String name);

    Page<User> findAllByIdIn(List<Long> ids, Pagination pagination);

}
