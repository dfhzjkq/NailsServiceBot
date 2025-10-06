package ru.vilen.NailsServiceBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vilen.NailsServiceBot.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
