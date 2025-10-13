package ru.vilen.NailsServiceBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vilen.NailsServiceBot.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


}
